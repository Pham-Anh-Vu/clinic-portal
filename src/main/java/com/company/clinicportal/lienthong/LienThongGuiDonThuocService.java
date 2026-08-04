package com.company.clinicportal.lienthong;

import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.lienthong.dto.DonThuocMappingService;
import com.company.clinicportal.lienthong.dto.GuiDonThuocRequest;
import com.company.clinicportal.lienthong.dto.GuiDonThuocResponse;
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

    public GuiDonThuocResult send(DonThuoc dt, String idempotencyKey, String maLienThongCoSo, String password) {
        if (!properties.isEnabled()) {
            throw new LienThongApiException("Liên thông đang tắt.", 0, null, null);
        }
        if (dt == null) throw new LienThongApiException("DonThuoc null", 0, null, null);

        String token = tokenService.getFacilityToken(maLienThongCoSo, password);
        GuiDonThuocRequest request = DonThuocMappingService.fromEntity(dt);
        String correlationId = DonThuocAuditService.newCorrelationId();
        long start = DonThuocAuditService.startTimer();

        String reqJson = safeStringify(request);
        Map<String, Object> reqMasked = maskRequest(request);
        try {
            GuiDonThuocResponse resp = httpClient.post(
                    "/api/v1/gui-don-thuoc",
                    request,
                    GuiDonThuocResponse.class,
                    token);
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
                tokenService.invalidateFacility(maLienThongCoSo);
            }
            audit.recordFailure("gui-don-thuoc", correlationId, code, reqMasked,
                    body == null ? ex.getMessage() : body, ms, retryable, dt.getMaDonThuoc());
            applyResponse(dt, null, idempotencyKey, "HTTP " + code + ": " + body);
            return new GuiDonThuocResult(false, code, null, reqJson, correlationId);
        } catch (RestClientException ex) {
            long ms = DonThuocAuditService.stopTimer(start);
            String msg = ex.getMessage();
            audit.recordFailure("gui-don-thuoc", correlationId, 0, reqMasked, msg, ms, true, dt.getMaDonThuoc());
            applyResponse(dt, null, idempotencyKey, msg);
            return new GuiDonThuocResult(false, 0, null, reqJson, correlationId);
        }
    }

    private void applyResponse(DonThuoc dt, GuiDonThuocResponse resp, String idempotencyKey, String error) {
        dt.setIdempotencyKey(idempotencyKey);
        dt.setLanGuiLienThong((dt.getLanGuiLienThong() == null ? 0 : dt.getLanGuiLienThong()) + 1);
        dt.setLanGuiCuoiAt(Date.from(Instant.now()));
        if (resp != null && resp.success) {
            dt.setTrangThaiEnum(com.company.clinicportal.enumentity.TrangThaiDonThuoc.PHAT_HANH);
            dt.setLastError(null);
            dt.setMaDonThuocQg(resp.ma_don_thuoc_qg);
            dt.setDonThuocIdQg(resp.don_thuoc_id);
            dt.setNgayDongBoCuoiAt(Date.from(Instant.now()));
            dt.setPhanHoiCuoi(safeStringify(resp));
        } else if (error != null) {
            dt.setLastError(truncate(error, 1000));
            // không đổi trạng thái đơn từ DA_GUI → vẫn cho retry
        }
        dataManager.save(new SaveContext().saving(dt));
    }

    private Map<String, Object> maskRequest(GuiDonThuocRequest req) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ma_don_thuoc", req.so_vao_vien); // chỉ log thông tin không nhạy cảm
        m.put("ho_va_ten_benh_nhan", req.ho_va_ten_benh_nhan);
        m.put("so_cmnd_cccd", req.so_cmnd_cccd == null ? null : maskText(req.so_cmnd_cccd));
        m.put("so_dien_thoai", req.so_dien_thoai == null ? null : maskText(req.so_dien_thoai));
        m.put("so_dinh_danh_y_te", req.so_dinh_danh_y_te == null ? null : maskText(req.so_dinh_danh_y_te));
        m.put("ho_va_ten_bac_si", req.ho_va_ten_bac_si);
        m.put("ma_lien_thong_bac_si", req.ma_lien_thong_bac_si);
        m.put("so_cchn_bac_si", req.so_cchn_bac_si == null ? null : maskText(req.so_cchn_bac_si));
        m.put("count_don_thuoc_chi_tiet", req.don_thuoc_chi_tiet == null ? 0 : req.don_thuoc_chi_tiet.size());
        m.put("count_chan_doan", req.chan_doan == null ? 0 : req.chan_doan.size());
        m.put("count_dot_dung", req.dot_dung == null ? 0 : req.dot_dung.size());
        m.put("ngay_ke_don", req.ngay_ke_don == null ? null : req.ngay_ke_don.toString());
        return m;
    }

    private Map<String, Object> responseAsMap(GuiDonThuocResponse resp) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (resp == null) return m;
        m.put("success", resp.success);
        m.put("error_code", resp.error_code);
        m.put("message", resp.message);
        m.put("ma_don_thuoc_qg", resp.ma_don_thuoc_qg);
        m.put("don_thuoc_id", resp.don_thuoc_id);
        m.put("errors", resp.errors);
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
