package com.company.clinicportal.lienthong;

import com.company.clinicportal.lienthong.entity.LienThongDonThuocLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.core.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Ghi audit cho mỗi request/response liên thông đơn thuốc vào bảng {@code lt_don_thuoc_log}.
 * Fail-safe: lỗi persist được log lại nhưng không làm hỏng flow nghiệp vụ.
 */
@Service
public class DonThuocAuditService {

    private static final Logger log = LoggerFactory.getLogger(DonThuocAuditService.class);

    private static final int MAX_JSON_CHARS = 3900;

    private final DataManager dataManager;
    private final ObjectMapper objectMapper;

    public DonThuocAuditService(DataManager dataManager,
                                @Qualifier("lienThongObjectMapper") ObjectMapper objectMapper) {
        this.dataManager = dataManager;
        this.objectMapper = objectMapper;
    }

    public void recordSuccess(String api, String correlationId, int httpStatus,
                              Map<String, Object> maskedRequest, Map<String, Object> maskedResponse,
                              long latencyMs, String maDonThuoc) {
        persist("SUCCESS", api, correlationId, httpStatus, maskedRequest, maskedResponse, latencyMs, null, maDonThuoc);
    }

    public void recordFailure(String api, String correlationId, int httpStatus,
                              Map<String, Object> maskedRequest, String errorMessage,
                              long latencyMs, boolean retryable, String maDonThuoc) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("error", errorMessage);
        resp.put("retryable", retryable);
        persist("FAILURE", api, correlationId, httpStatus, maskedRequest, resp, latencyMs, retryable, maDonThuoc);
    }

    private void persist(String outcome, String api, String correlationId, int httpStatus,
                         Map<String, Object> maskedRequest, Map<String, Object> maskedResponse,
                         long latencyMs, Boolean retryable, String maDonThuoc) {
        String reqId = correlationId == null ? UUID.randomUUID().toString() : correlationId;
        LienThongDonThuocLog entity = dataManager.create(LienThongDonThuocLog.class);
        entity.setApi(api);
        entity.setCorrelationId(reqId);
        entity.setMaDonThuoc(maDonThuoc);
        entity.setHttpStatus(httpStatus);
        entity.setLatencyMs(latencyMs);
        entity.setOutcome(outcome);
        entity.setRequestMasked(toJson(maskedRequest));
        entity.setResponseMasked(toJson(maskedResponse));
        entity.setRetryable(retryable);
        entity.setCreatedAt(Date.from(Instant.now()));
        try {
            dataManager.save(entity);
        } catch (Exception ex) {
            // fail-safe: audit không được phá vỡ nghiệp vụ
            log.error("Audit persist failed (api={} corr={}): {}", api, reqId, ex.getMessage(), ex);
        }
    }

    private String toJson(Map<String, Object> map) {
        if (map == null) return null;
        try {
            String s = objectMapper.writeValueAsString(map);
            if (s.length() > MAX_JSON_CHARS) {
                return s.substring(0, MAX_JSON_CHARS);
            }
            return s;
        } catch (JsonProcessingException e) {
            return map.toString();
        }
    }

    /** Helper tạo correlation id nếu cần. */
    public static String newCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /** Helper trả về latency. */
    public static long startTimer() {
        return System.nanoTime();
    }

    public static long stopTimer(long startNanos) {
        return Math.max(0, (System.nanoTime() - startNanos) / 1_000_000L);
    }

    public static Instant now() {
        return Instant.now();
    }
}
