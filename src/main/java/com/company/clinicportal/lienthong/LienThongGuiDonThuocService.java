package com.company.clinicportal.lienthong;

import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.lienthong.dto.DonThuocMappingService;
import com.company.clinicportal.lienthong.dto.GuiDonThuocRequest;
import com.company.clinicportal.lienthong.dto.GuiDonThuocResponse;
import com.company.clinicportal.enumentity.TrangThaiDonThuoc;
import com.company.clinicportal.entity.DonThuoc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service gọi API gửi đơn thuốc.
 *
 * <p>Pipeline:</p>
 * <ol>
 *     <li>Login lấy token cơ sở (cache qua {@link DonThuocTokenService}).</li>
 *     <li>Build payload bằng {@link DonThuocMappingService#fromEntity} (JPA entity → snake-case DTO).</li>
 *     <li>POST /api/v1/gui-don-thuoc với bearer token + correlation id.</li>
 *     <li>Audit log request/response (mask).</li>
 *     <li>Update entity {@code DonThuoc} (trangThai, payloadResp, maDonThuocQg,...).</li>
 * </ol>
 *
 * <p>Exceptions:</p>
 * <ul>
 *     <li>Validation 4xx (422) → không retry, đánh dấu FAILED trên outbox.</li>
 *     <li>401 → invalidate token cache, lần sau tự refresh.</li>
 *     <li>5xx/timeout → retry qua {@link LienThongHttpClient}.</li>
 * </ul>
 */
@Service
public class LienThongGuiDonThuocService {

    private static final Logger log = LoggerFactory.getLogger(LienThongGuiDonThuocService.class);

    /** Trạng thái liên thông — lưu vào cột don_thuoc.lien_thong_status. */
    public static final String LT_STATUS_PENDING   = "PENDING";
    public static final String LT_STATUS_SENT      = "SENT";
    public static final String LT_STATUS_FAILED    = "FAILED";
    public static final String LT_STATUS_GIVEN_UP  = "GIVEN_UP";

    private final LienThongProperties properties;
    private final LienThongHttpClient httpClient;
    private final DonThuocTokenService tokenService;
    private final DonThuocAuditService audit;
    private final DataManager dataManager;
    private final ObjectMapper objectMapper;

    public LienThongGuiDonThuocService(LienThongProperties properties,
                                       LienThongHttpClient httpClient,
                                       DonThuocTokenService tokenService,
                                       DonThuocAuditService audit,
                                       DataManager dataManager,
                                       @Qualifier("lienThongObjectMapper") ObjectMapper objectMapper) {
        this.properties = properties;
        this.httpClient = httpClient;
        this.tokenService = tokenService;
        this.audit = audit;
        this.dataManager = dataManager;
        this.objectMapper = objectMapper;
    }

    public GuiDonThuocResult send(DonThuoc dt, String idempotencyKey,
                                  String maLienThongCoSo, String passwordCoSo,
                                  String maLienThongBacSi, String passwordBacSi) {
        if (!properties.isEnabled()) {
            throw new LienThongApiException("Liên thông đang tắt.", 0, null, null);
        }
        if (dt == null) throw new LienThongApiException("DonThuoc null", 0, null, null);

        // Audit guard: bỏ qua đơn đã vượt ngưỡng gửi liên thông
        // (tránh spam server BYT, thường do lỗi config dữ liệu / payload sai nghiêm trọng).
        if (dt.isLienThongGivenUp()) {
            log.warn("[LienThong] BỎ QUA gửi maDonThuoc={} — đã thử {} lần >= {} (status={}). " +
                    "Không gửi tiếp để tránh spam BYT.",
                    dt.getMaDonThuoc(),
                    dt.getSoLanThuLienThong(),
                    DonThuoc.MAX_LIEN_THONG_ATTEMPTS,
                    dt.getLienThongStatus());
            return new GuiDonThuocResult(false, 0, null, null, "GIVEN_UP-" + dt.getMaDonThuoc());
        }

        // Lấy doctor token theo FSD: token gửi đơn thuốc phải lấy từ /api/auth/dang-nhap-bac-si
        String token = tokenService.getDoctorToken(maLienThongCoSo, maLienThongBacSi, passwordBacSi);

        GuiDonThuocRequest request = DonThuocMappingService.fromEntity(dt);
        String correlationId = DonThuocAuditService.newCorrelationId();
        long start = DonThuocAuditService.startTimer();

        String reqJson = safeStringify(request);
        Map<String, Object> reqMasked = maskRequest(request);

        try {
            // Gọi POST, nhận raw body vì BYT có thể trả plain string thay vì JSON
            String rawBody = httpClient.postForString(
                    "/api/v1/gui-don-thuoc", request, token);

            // Parse response: BYT có thể trả chuỗi đơn thuần hoặc JSON
            GuiDonThuocResponse resp = parseResponse(rawBody, 200);
            long ms = DonThuocAuditService.stopTimer(start);
            audit.recordSuccess("gui-don-thuoc", correlationId, 200,
                    reqMasked, responseAsMap(resp), ms, dt.getMaDonThuoc());
            applyResponse(dt, resp, idempotencyKey, null);
            return new GuiDonThuocResult(true, 200, resp, reqJson, correlationId);

        } catch (HttpStatusCodeException ex) {
            HttpStatusCode status = ex.getStatusCode();
            long ms = DonThuocAuditService.stopTimer(start);
            String body = ex.getResponseBodyAsString();
            int code = status == null ? 0 : status.value();
            boolean retryable = code >= 500 || code == 408 || code == 429;
            if (code == 401) {
                tokenService.invalidateDoctor(maLienThongCoSo, maLienThongBacSi);
            }
            GuiDonThuocResponse resp = parseResponse(body, code);
            Map<String, Object> respMap = responseAsMap(resp);
            audit.recordFailure("gui-don-thuoc", correlationId, code, reqMasked,
                    body == null ? ex.getMessage() : body, ms, retryable, dt.getMaDonThuoc());
            applyResponse(dt, resp, idempotencyKey, "HTTP " + code + ": " + body);
            return new GuiDonThuocResult(false, code, resp, reqJson, correlationId);
        } catch (RestClientException ex) {
            long ms = DonThuocAuditService.stopTimer(start);
            String msg = ex.getMessage();
            audit.recordFailure("gui-don-thuoc", correlationId, 0, reqMasked, msg, ms, true, dt.getMaDonThuoc());
            applyResponse(dt, null, idempotencyKey, msg);
            return new GuiDonThuocResult(false, 0, null, reqJson, correlationId);
        }
    }

    /**
     * Phiên bản DEBUG: chỉ log JSON, không gọi API thật. Dùng để verify payload.
     */
    public GuiDonThuocResult sendDebug(DonThuoc dt, String idempotencyKey) {
        GuiDonThuocRequest request = DonThuocMappingService.fromEntity(dt);
        String correlationId = DonThuocAuditService.newCorrelationId();
        String reqJson = safeStringify(request);

        log.warn("╔══════════════════════════════════════════════════════════════╗");
        log.warn("║            DEBUG: JSON SẼ GỬI ĐI (API ĐANG BỊ TẮT)         ║");
        log.warn("╠══════════════════════════════════════════════════════════════╣");
        log.warn("║ maDonThuoc    : {}", dt.getMaDonThuoc());
        log.warn("║ idempotencyKey: {}", idempotencyKey);
        log.warn("╠══════════════════════════════════════════════════════════════╣");
        log.warn("║                         FULL JSON                            ║");
        log.warn("╠══════════════════════════════════════════════════════════════╣");
        log.warn("{}", reqJson);
        log.warn("╚══════════════════════════════════════════════════════════════╝");

        return new GuiDonThuocResult(false, 0, null, reqJson, correlationId + "-DEBUG");
    }

    /**
     * Parse raw body từ BYT.
     *
     * <p>Thực tế BYT 808/QĐ-BYT trả về JSON object (không phải plain text):
     * <pre>
     *   200 OK: { "success": "Gửi đơn thuốc thành công", "checksum": "..." }
     *   422:    { "success": "Đơn thuốc đã được sử dụng...", "danh_sach_cac_loi":[...] }
     * </pre>
     * Đôi khi server cũng trả body không phải JSON (chuỗi thuần) — cũng xử lý.</p>
     */
    private GuiDonThuocResponse parseResponse(String body, int httpStatus) {
        if (body == null || body.isBlank()) {
            if (httpStatus == 200) {
                return GuiDonThuocResponse.ok("(không có nội dung, coi như thành công)");
            }
            return GuiDonThuocResponse.error("(response rỗng, HTTP " + httpStatus + ")");
        }
        body = body.trim();
        // Thử parse JSON
        if (body.startsWith("{") || body.startsWith("[")) {
            try {
                GuiDonThuocResponse resp = objectMapper.readValue(body, GuiDonThuocResponse.class);
                resp.httpStatus = httpStatus;

                // Map BYT's `success` field (String) → response.message / successFlag.
                // Field `success` không có trong DTO nên Jackson bỏ qua; lấy từ JSON
                // thô bằng cách parse lại qua Map.
                try {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> raw = objectMapper.readValue(body, java.util.Map.class);
                    Object successObj = raw.get("success");
                    if (successObj != null) {
                        resp.message = String.valueOf(successObj);
                        // success=True ↔ BYT báo "thành công" (không phân biệt hoa/thường).
                        resp.successFlag = containsIgnoreCase(resp.message, "thành công");
                    }
                    Object checksumObj = raw.get("checksum");
                    if (checksumObj != null) {
                        resp.checksum = String.valueOf(checksumObj);
                    }
                    // Lấy danh_sach_cac_loi nếu có
                    Object dsLoiObj = raw.get("danh_sach_cac_loi");
                    if (dsLoiObj instanceof java.util.List<?> dsList && !dsList.isEmpty()) {
                        resp.danh_sach_cac_loi = new java.util.ArrayList<>();
                        for (Object item : dsList) {
                            if (item instanceof java.util.Map<?, ?> m) {
                                GuiDonThuocResponse.DanhSachLoi d = new GuiDonThuocResponse.DanhSachLoi();
                                Object truong = m.get("truong");
                                Object maLoi = m.get("ma_loi");
                                Object tb = m.get("thong_bao");
                                d.truong = truong == null ? null : String.valueOf(truong);
                                d.ma_loi = maLoi == null ? null : String.valueOf(maLoi);
                                d.thong_bao = tb == null ? null : String.valueOf(tb);
                                resp.danh_sach_cac_loi.add(d);
                            }
                        }
                    }
                } catch (Exception ignore) {
                    // raw parse fail → giữ nguyên giá trị từ DTO mapping
                }
                return resp;
            } catch (Exception jsonEx) {
                // Không phải JSON object → fall through xử lý chuỗi
            }
        }
        // BYT trả chuỗi thuần (không phải JSON)
        if (httpStatus == 200) {
            return GuiDonThuocResponse.ok(body);
        }
        return GuiDonThuocResponse.error(body);
    }

    private static boolean containsIgnoreCase(String haystack, String needle) {
        if (haystack == null || needle == null) return false;
        return haystack.toLowerCase().contains(needle.toLowerCase());
    }

    private void applyResponse(DonThuoc dt, GuiDonThuocResponse resp, String idempotencyKey, String error) {
        dt.setIdempotencyKey(idempotencyKey);
        // Tăng số lần đã THỬ (mỗi lần send() gọi API = 1 attempt).
        int previous = dt.getSoLanThuLienThong() == null ? 0 : dt.getSoLanThuLienThong();
        int soLanThu = previous + 1;
        dt.setSoLanThuLienThong(soLanThu);
        dt.setLanGuiLienThong((dt.getLanGuiLienThong() == null ? 0 : dt.getLanGuiLienThong()) + 1);
        Date now = Date.from(Instant.now());
        dt.setLanGuiCuoiAt(now);
        dt.setLienThongLastAttemptAt(now);
        if (resp != null && resp.isSuccess()) {
            dt.setTrangThaiEnum(TrangThaiDonThuoc.PHAT_HANH);
            dt.setLastError(null);
            // BYT không trả mã đơn quốc gia trong body thành công → dùng mã local
            dt.setMaDonThuocQg(dt.getMaDonThuoc());
            dt.setNgayDongBoCuoiAt(now);
            dt.setPhanHoiCuoi(resp.message);
            dt.setLienThongStatus(LT_STATUS_SENT);
            if (resp.checksum != null && !resp.checksum.isBlank()) {
                dt.setLienThongChecksum(resp.checksum);
            }
        } else {
            // Thất bại: phân biệt FAILED (còn retry) vs GIVEN_UP (đã đạt ngưỡng)
            if (error != null) {
                dt.setLastError(truncate(error, 1000));
            }
            if (soLanThu >= DonThuoc.MAX_LIEN_THONG_ATTEMPTS) {
                dt.setLienThongStatus(LT_STATUS_GIVEN_UP);
                log.warn("[LienThong] Đơn {} đã thử {}/{} lần — đánh dấu GIVEN_UP, " +
                        "không gửi nữa ở đợt sau.", dt.getMaDonThuoc(),
                        soLanThu, DonThuoc.MAX_LIEN_THONG_ATTEMPTS);
            } else {
                dt.setLienThongStatus(LT_STATUS_FAILED);
            }
        }
        dataManager.save(new SaveContext().saving(dt));
    }

    private Map<String, Object> maskRequest(GuiDonThuocRequest req) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("loai_don_thuoc", req.loai_don_thuoc);
        m.put("ma_don_thuoc", req.ma_don_thuoc);
        m.put("ho_ten_benh_nhan", req.ho_ten_benh_nhan);
        m.put("ma_dinh_danh_cong_dan", req.ma_dinh_danh_cong_dan == null ? null : maskText(req.ma_dinh_danh_cong_dan));
        m.put("so_dien_thoai_nguoi_kham_benh", req.so_dien_thoai_nguoi_kham_benh == null ? null : maskText(req.so_dien_thoai_nguoi_kham_benh));
        m.put("ma_dinh_danh_y_te", req.ma_dinh_danh_y_te == null ? null : maskText(req.ma_dinh_danh_y_te));
        m.put("count_thong_tin_don_thuoc", req.thong_tin_don_thuoc == null ? 0 : req.thong_tin_don_thuoc.size());
        m.put("count_chan_doan", req.chan_doan == null ? 0 : req.chan_doan.size());
        m.put("count_dot_dung_thuoc", req.dot_dung_thuoc == null ? 0 : 1);
        m.put("ngay_gio_ke_don", req.ngay_gio_ke_don);
        return m;
    }

    private Map<String, Object> responseAsMap(GuiDonThuocResponse resp) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (resp == null) return m;
        m.put("status", resp.status);
        m.put("message", resp.message);
        m.put("httpStatus", resp.httpStatus);
        m.put("danh_sach_cac_loi", resp.danh_sach_cac_loi);
        return m;
    }

    private static String maskText(String s) {
        if (s == null || s.length() <= 2) return "***";
        return s.substring(0, 2) + "***" + s.substring(s.length() - 1);
    }

    private String safeStringify(Object o) {
        try {
            String s = objectMapper.writeValueAsString(o);
            return s == null ? null : (s.length() > 4000 ? s.substring(0, 4000) : s);
        } catch (JsonProcessingException e) {
            return String.valueOf(o);
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    public static class GuiDonThuocResult {
        public final boolean success;
        public final int httpStatus;
        public final GuiDonThuocResponse response;
        public final String requestJson;
        public final String correlationId;

        public GuiDonThuocResult(boolean success, int httpStatus, GuiDonThuocResponse response,
                                 String requestJson, String correlationId) {
            this.success = success;
            this.httpStatus = httpStatus;
            this.response = response;
            this.requestJson = requestJson;
            this.correlationId = correlationId;
        }
    }
}
