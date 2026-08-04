package com.company.clinicportal.lienthong;

import com.company.clinicportal.lienthong.dto.BacSiDtos;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.core.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service quản lý bác sĩ/nhân viên liên thông:
 * <ul>
 *     <li>Thêm bác sĩ mới (POST /api/auth/dang-ky-bac-si)</li>
 *     <li>Xoá bác sĩ (DELETE /api/auth/xoa-bac-si)</li>
 *     <li>Đăng nhập bác sĩ theo cơ sở (POST /api/auth/dang-nhap-bac-si)</li>
 *     <li>Đăng nhập nhân viên y tế (POST /api/auth/dang-nhap-nhan-vien-y-te)</li>
 * </ul>
 *
 * <p>Ghi log qua {@link DonThuocAuditService} nhưng mask token/password.</p>
 */
@Service
public class DonThuocDoctorAdminService {

    private static final Logger log = LoggerFactory.getLogger(DonThuocDoctorAdminService.class);

    private final LienThongProperties properties;
    private final LienThongHttpClient httpClient;
    private final DonThuocAuditService audit;
    private final DataManager dataManager;
    private final ObjectMapper objectMapper;

    public DonThuocDoctorAdminService(LienThongProperties properties,
                                      LienThongHttpClient httpClient,
                                      DonThuocAuditService audit,
                                      DataManager dataManager,
                                      @Qualifier("lienThongObjectMapper") ObjectMapper objectMapper) {
        this.properties = properties;
        this.httpClient = httpClient;
        this.audit = audit;
        this.dataManager = dataManager;
        this.objectMapper = objectMapper;
    }

    /**
     * Đăng ký bác sĩ - idempotent: nếu server trả 409 conflict, trả về true.
     */
    public boolean addDoctor(BacSiDtos.AddBacSiRequest req, String appName, String appKey) {
        ensureEnabled();
        String corrId = DonThuocAuditService.newCorrelationId();
        long start = DonThuocAuditService.startTimer();
        Map<String, Object> masked = maskAdd(req);
        try {
            BacSiDtos.AddBacSiResponse resp = httpClient.post(
                    "/api/auth/dang-ky-bac-si", req, BacSiDtos.AddBacSiResponse.class, appName, appKey);
            long ms = DonThuocAuditService.stopTimer(start);
            audit.recordSuccess("dang-ky-bac-si", corrId, 200,
                    masked, respAsMap(resp), ms, null);
            return resp != null && resp.success;
        } catch (HttpStatusCodeException ex) {
            long ms = DonThuocAuditService.stopTimer(start);
            int status = ex.getStatusCode().value();
            String body = ex.getResponseBodyAsString();
            boolean retryable = status >= 500 || status == 408 || status == 429;
            audit.recordFailure("dang-ky-bac-si", corrId, status, masked, body, ms, retryable, null);
            // Idempotent: 409 conflict = đã tồn tại → coi như success
            if (status == 409) return true;
            throw new LienThongApiException("dang-ky-bac-si HTTP " + status + ": " + body, status, body, body, retryable);
        } catch (RestClientException ex) {
            long ms = DonThuocAuditService.stopTimer(start);
            audit.recordFailure("dang-ky-bac-si", corrId, 0, masked, ex.getMessage(), ms, true, null);
            throw new LienThongApiException("dang-ky-bac-si: " + ex.getMessage(), 0, null, ex.getMessage(), true);
        }
    }

    public BacSiDtos.LoginBacSiResponse loginDoctor(BacSiDtos.LoginBacSiRequest req, String appName, String appKey) {
        ensureEnabled();
        String corrId = DonThuocAuditService.newCorrelationId();
        long start = DonThuocAuditService.startTimer();
        Map<String, Object> masked = maskLogin(req);
        try {
            BacSiDtos.LoginBacSiResponse resp = httpClient.post(
                    "/api/auth/dang-nhap-bac-si", req, BacSiDtos.LoginBacSiResponse.class, appName, appKey);
            long ms = DonThuocAuditService.stopTimer(start);
            Map<String, Object> respMap = respAsMap(resp);
            if (respMap != null && resp.token != null) respMap.put("token", "***");
            audit.recordSuccess("dang-nhap-bac-si", corrId, 200, masked, respMap, ms, null);
            return resp;
        } catch (HttpStatusCodeException ex) {
            long ms = DonThuocAuditService.stopTimer(start);
            int status = ex.getStatusCode().value();
            audit.recordFailure("dang-nhap-bac-si", corrId, status, masked, ex.getResponseBodyAsString(), ms, false, null);
            throw new LienThongApiException("dang-nhap-bac-si HTTP " + status, status, ex.getResponseBodyAsString(), ex.getMessage(), false);
        }
    }

    public BacSiDtos.LoginNhanVienResponse loginNhanVien(BacSiDtos.LoginNhanVienRequest req, String appName, String appKey) {
        ensureEnabled();
        String corrId = DonThuocAuditService.newCorrelationId();
        long start = DonThuocAuditService.startTimer();
        try {
            BacSiDtos.LoginNhanVienResponse resp = httpClient.post(
                    "/api/auth/dang-nhap-nhan-vien-y-te", req, BacSiDtos.LoginNhanVienResponse.class, appName, appKey);
            long ms = DonThuocAuditService.stopTimer(start);
            Map<String, Object> respMap = respAsMap(resp);
            if (respMap != null && resp.token != null) respMap.put("token", "***");
            audit.recordSuccess("dang-nhap-nhan-vien-y-te", corrId, 200,
                    Map.of("username", req.username, "ma_lien_thong_co_so_kham_chua_benh", req.ma_lien_thong_co_so_kham_chua_benh),
                    respMap, ms, null);
            return resp;
        } catch (HttpStatusCodeException ex) {
            long ms = DonThuocAuditService.stopTimer(start);
            int status = ex.getStatusCode().value();
            audit.recordFailure("dang-nhap-nhan-vien-y-te", corrId, status,
                    Map.of("username", req.username),
                    ex.getResponseBodyAsString(), ms, false, null);
            throw new LienThongApiException("dang-nhap-nhan-vien-y-te HTTP " + status, status, ex.getResponseBodyAsString(), ex.getMessage(), false);
        }
    }

    private void ensureEnabled() {
        if (!properties.isEnabled()) {
            throw new LienThongApiException("Liên thông đang tắt.", 0, null, null);
        }
    }

    private Map<String, Object> maskAdd(BacSiDtos.AddBacSiRequest r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ma_lien_thong_bac_si", r.ma_lien_thong_bac_si);
        m.put("ho_va_ten", r.ho_va_ten);
        m.put("so_cchn", r.so_cchn == null ? null : mask(r.so_cchn));
        m.put("chuyen_mon", r.chuyen_mon);
        m.put("so_dien_thoai", r.so_dien_thoai == null ? null : mask(r.so_dien_thoai));
        m.put("email", r.email);
        m.put("ma_lien_thong_co_so_kham_chua_benh", r.ma_lien_thong_co_so_kham_chua_benh);
        return m;
    }

    private Map<String, Object> maskLogin(BacSiDtos.LoginBacSiRequest r) {
        return Map.of(
                "ma_lien_thong_bac_si", r.ma_lien_thong_bac_si == null ? "" : r.ma_lien_thong_bac_si,
                "ma_lien_thong_co_so_kham_chua_benh", r.ma_lien_thong_co_so_kham_chua_benh == null ? "" : r.ma_lien_thong_co_so_kham_chua_benh
        );
    }

    private static String mask(String s) {
        if (s == null || s.length() <= 2) return "***";
        return s.substring(0, 2) + "***";
    }

    private Map<String, Object> respAsMap(Object o) {
        try {
            if (o == null) return null;
            return objectMapper.readValue(objectMapper.writeValueAsString(o), Map.class);
        } catch (Exception ex) {
            return Map.of("message", String.valueOf(o));
        }
    }
}
