package com.company.clinicportal.lienthong;

import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.lienthong.entity.LienThongDonThuocLog;
import com.company.clinicportal.lienthong.entity.LienThongDonThuocOutbox;
import com.company.clinicportal.service.DonThuocService;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.security.SystemAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbox/queue bền vững cho việc gửi đơn thuốc.
 *
 * <p>Lifecycle của outbox row:</p>
 * <pre>
 *   PENDING → IN_PROGRESS → DONE (success)
 *                          → FAILED (4xx lỗi validation, không retry)
 *                          → RETRY_SCHEDULED (5xx/timeout/429; thử lại sau backoff)
 * </pre>
 *
 * <p>Idempotency: cột idempotency_key UNIQUE. Khi enqueue với key đã tồn tại,
 * không tạo row mới.</p>
 *
 * <p>Worker chạy qua {@link OutboxWorkerBridge} để tránh self-call bypass proxy.</p>
 */
@Service
public class LienThongOutboxService {

    private static final Logger log = LoggerFactory.getLogger(LienThongOutboxService.class);

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_RETRY = "RETRY_SCHEDULED";

    /** Tên entity trong Jmix metadata cho JPQL/security policy. */
    public static final String ENTITY_NAME = "ltcs_LienThongDonThuocOutbox";

    private final LienThongProperties properties;

    @Autowired
    private DataManager dataManager;
    @Autowired
    private ObjectProvider<DonThuocService> donThuocServiceProvider;
    @Autowired
    private OutboxWorkerBridge bridge;
    @Autowired
    private SystemAuthenticator systemAuthenticator;

    public LienThongOutboxService(LienThongProperties properties) {
        this.properties = properties;
    }

    // Scheduler đã bị tắt - dùng nút "Đồng bộ" thủ công thay vì quét tự động mỗi 30s
    // @PostConstruct
    // void start() {
    //     long periodSec = 30;
    //     scheduler.scheduleWithFixedDelay(this::tick, periodSec, periodSec, TimeUnit.SECONDS);
    //     log.info("LienThongOutboxService worker started, period={}s", periodSec);
    // }

    // @PreDestroy
    // void stop() {
    //     if (scheduler != null) {
    //         scheduler.shutdownNow();
    //     }
    // }

    /**
     * Worker tick được gọi bởi scheduler. Gọi qua bridge để proxy AOP hoạt động.
     */
    public void tick() {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            systemAuthenticator.begin("admin");
            try {
                bridge.sweepAndPick(this);
            } finally {
                systemAuthenticator.end();
            }
        } catch (Exception e) {
            log.error("Outbox tick error", e);
        }
    }

    /**
     * Đưa 1 job gửi đơn vào outbox. Idempotent: nếu {@code idempotencyKey} đã có
     * (và chưa DONE) thì trả về row cũ, không tạo row mới.
     */
    @Transactional
    public LienThongDonThuocOutbox enqueueDonThuoc(DonThuoc dt, String idempotencyKey) {
        if (dt == null || idempotencyKey == null) return null;
        Optional<LienThongDonThuocOutbox> exist = dataManager.load(LienThongDonThuocOutbox.class)
                .query("select e from " + ENTITY_NAME + " e where e.idempotencyKey = :k")
                .parameter("k", idempotencyKey)
                .optional();
        if (exist.isPresent()) {
            log.info("Outbox idempotency hit: key={} maDonThuoc={} status={}",
                    idempotencyKey, exist.get().getMaDonThuoc(), exist.get().getStatus());
            return exist.get();
        }
        LienThongDonThuocOutbox row = dataManager.create(LienThongDonThuocOutbox.class);
        row.setApi("gui-don-thuoc");
        row.setMaDonThuoc(dt.getMaDonThuoc());
        row.setIdempotencyKey(idempotencyKey);
        row.setStatus(STATUS_PENDING);
        row.setAttempt(0);
        row.setMaxAttempt(properties.getMaxRetries());
        row.setNextAttemptAt(Date.from(Instant.now()));
        row.setCreatedAt(Date.from(Instant.now()));
        row.setUpdatedAt(Date.from(Instant.now()));
        dataManager.save(new SaveContext().saving(row));
        log.info("Outbox enqueue: maDonThuoc={} key={}", dt.getMaDonThuoc(), idempotencyKey);
        return row;
    }

    /**
     * Forward sang {@link OutboxWorkerBridge#sweepStaleLocks()}.
     */
    public void sweepStaleLocks() {
        bridge.sweepStaleLocks();
    }

    /**
     * Forward sang {@link OutboxWorkerBridge#pickPending()}.
     */
    public Optional<LienThongDonThuocOutbox> pickPending() {
        return bridge.pickPending();
    }

    /**
     * Public entry từ bridge: xử lý 1 job. Bean {@code LienThongGuiDonThuocService}
     * có thể null (skeleton mode).
     */
    public void processJobViaBridge(LienThongDonThuocOutbox row,
                                    LienThongGuiDonThuocService svc) {
        processJobInternal(row, svc);
    }

    /**
     * API call thực sự — Giai đoạn 4 hook {@link LienThongGuiDonThuocService}.
     * Nếu chưa có bean {@code LienThongGuiDonThuocService} (skeleton mode), mô phỏng DONE.
     */
    private void processJobInternal(LienThongDonThuocOutbox row,
                                    LienThongGuiDonThuocService svc) {
        if (row == null) return;
        try {
            DonThuoc dt = dataManager.load(DonThuoc.class)
                    .query("select e from DonThuoc e where e.maDonThuoc = :m")
                    .parameter("m", row.getMaDonThuoc())
                    .optional().orElse(null);
            if (dt == null) {
                markFailed(row, "DonThuoc không tồn tại");
                logApi(row, "FAILED", 0, "DonThuoc không tồn tại", null, false);
                return;
            }
            if (svc != null) {
                // ma_lien_thong_bac_si + password lấy từ application.properties
                // ma_lien_thong_co_so + passwordCoSo lấy từ DB CoSoKhamChuaBenhLienThong
                String doctorMa = properties.getBacSi() == null ? null : properties.getBacSi().getMaLienThongBacSi();
                String doctorPw = properties.getBacSi() == null ? null : properties.getBacSi().getPassword();
                if (doctorMa == null || doctorMa.isBlank() || doctorPw == null || doctorPw.isBlank()) {
                    log.warn("[Outbox] Thiếu clinicportal.lienthong.bac-si.* trong application.properties; job={} → FAILED.",
                            row.getMaDonThuoc());
                    markFailed(row, "Thiếu cấu hình ma_lien_thong_bac_si/password trong application.properties.");
                    return;
                }
                LienThongGuiDonThuocService.GuiDonThuocResult result =
                        svc.send(dt, row.getIdempotencyKey(),
                                dt.getMaCoSoKcb(),
                                resolveFacilityPassword(dt),
                                doctorMa,
                                doctorPw);
                if (result.success) {
                    markDone(row);
                } else if (result.httpStatus >= 500 || result.httpStatus == 0) {
                    scheduleRetryOrFail(row, "HTTP " + result.httpStatus);
                } else {
                    markFailed(row, "HTTP " + result.httpStatus);
                }
                return;
            }
            // Fallback skeleton (GĐ3) - mark DONE
            DonThuocService donThuocService = donThuocServiceProvider.getIfAvailable();
            if (donThuocService != null) {
                donThuocService.markSent(dt, row.getIdempotencyKey(),
                        "{\"mock\":true,\"attempt\":" + row.getAttempt() + "}");
            }
            markDone(row);
            logApi(row, "DONE", 200, "{\"mock\":true}", null, false);
        } catch (Exception ex) {
            log.error("processJob error", ex);
            scheduleRetryOrFail(row, ex.getMessage());
        }
    }

    private String resolveFacilityPassword(DonThuoc dt) {
        // Khi trong entity DonThuoc chưa có password cache, lookup từ CoSoKhamChuaBenhLienThong.
        // MVP: trả null — caller (submitForSending) truyền password từ UI.
        return null;
    }

    @Transactional
    public void markDone(LienThongDonThuocOutbox row) {
        row.setStatus(STATUS_DONE);
        row.setLockedAt(null);
        row.setLockedBy(null);
        row.setUpdatedAt(Date.from(Instant.now()));
        dataManager.save(new SaveContext().saving(row));
    }

    @Transactional
    public void scheduleRetryOrFail(LienThongDonThuocOutbox row, String error) {
        int att = row.getAttempt() == null ? 1 : row.getAttempt();
        int max = row.getMaxAttempt() == null ? 5 : row.getMaxAttempt();
        if (att >= max) {
            markFailed(row, error);
            logApi(row, "FAILED", 0, error, null, false);
            return;
        }
        // Exponential backoff: 1m, 2m, 4m, 8m, 16m
        long backoffSec = (long) Math.pow(2, att) * 60L;
        row.setStatus(STATUS_RETRY);
        row.setNextAttemptAt(Date.from(Instant.now().plusSeconds(backoffSec)));
        row.setLockedAt(null);
        row.setLockedBy(null);
        row.setLastError(error);
        row.setUpdatedAt(Date.from(Instant.now()));
        dataManager.save(new SaveContext().saving(row));
        logApi(row, "RETRY_SCHEDULED", 0, error, null, true);
    }

    @Transactional
    public void markFailed(LienThongDonThuocOutbox row, String error) {
        row.setStatus(STATUS_FAILED);
        row.setLastError(error);
        row.setLockedAt(null);
        row.setLockedBy(null);
        row.setUpdatedAt(Date.from(Instant.now()));
        dataManager.save(new SaveContext().saving(row));
    }

    private void logApi(LienThongDonThuocOutbox row, String outcome, int httpStatus,
                        String responseText, String reqMasked, boolean retryable) {
        LienThongDonThuocLog log0 = dataManager.create(LienThongDonThuocLog.class);
        log0.setApi(row.getApi());
        log0.setCorrelationId(row.getIdempotencyKey());
        log0.setMaDonThuoc(row.getMaDonThuoc());
        log0.setHttpStatus(httpStatus);
        log0.setLatencyMs(0L);
        log0.setOutcome(outcome);
        log0.setRequestMasked(reqMasked);
        log0.setResponseMasked(responseText == null ? null
                : responseText.length() > 4000 ? responseText.substring(0, 4000) : responseText);
        log0.setRetryable(retryable);
        log0.setCreatedAt(Date.from(Instant.now()));
        dataManager.save(new SaveContext().saving(log0));
    }

    private String workerId() {
        return System.getProperty("lienthong.outbox.workerId",
                UUID.randomUUID().toString().substring(0, 8));
    }
}
