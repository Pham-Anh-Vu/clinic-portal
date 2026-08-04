package com.company.clinicportal.lienthong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.UUID;

/**
 * HTTP client chung cho liên thông: tự gắn correlation id, retry có giới hạn
 * cho lỗi 5xx/mạng, không retry với lỗi 4xx (đặc biệt 401/422).
 *
 * Caching token, login/logout xử lý ở {@link DonThuocTokenService}.
 */
@Component
public class LienThongHttpClient {

    private static final Logger log = LoggerFactory.getLogger(LienThongHttpClient.class);

    public static final String HDR_CORRELATION = "X-Correlation-Id";
    public static final String HDR_AUTHORIZATION = "Authorization";
    public static final String HDR_APP_NAME = "app-name";
    public static final String HDR_APP_KEY = "app-key";

    private final LienThongProperties properties;
    private final RestClient restClient;

    public LienThongHttpClient(LienThongProperties properties, RestClient lienThongRestClient) {
        this.properties = properties;
        this.restClient = lienThongRestClient;
    }

    public <T> T post(String path, Object body, Class<T> responseType, String bearerToken) {
        return executeWithRetry(() -> {
            var req = restClient.post().uri(path)
                    .header(HDR_CORRELATION, newCorrelationId());
            if (bearerToken != null && !bearerToken.isBlank()) {
                req = req.header(HDR_AUTHORIZATION, "bearer " + bearerToken);
            }
            return req.body(body).retrieve().body(responseType);
        }, "POST " + path);
    }

    public <T> T post(String path, Object body, Class<T> responseType, String appName, String appKey) {
        return executeWithRetry(() -> restClient.post().uri(path)
                .header(HDR_CORRELATION, newCorrelationId())
                .header(HDR_APP_NAME, appName == null ? "" : appName)
                .header(HDR_APP_KEY, appKey == null ? "" : appKey)
                .body(body).retrieve().body(responseType), "POST " + path);
    }

    public <T> T get(String path, Map<String, String> queryParams, Class<T> responseType, String appName, String appKey) {
        return executeWithRetry(() -> {
            var rb = restClient.get().uri(uri -> {
                var u = uri.path(path);
                if (queryParams != null) {
                    queryParams.forEach(u::queryParam);
                }
                return u.build();
            }).header(HDR_CORRELATION, newCorrelationId())
                    .header(HDR_APP_NAME, appName == null ? "" : appName)
                    .header(HDR_APP_KEY, appKey == null ? "" : appKey);
            return rb.retrieve().body(responseType);
        }, "GET " + path);
    }

    private <T> T executeWithRetry(java.util.function.Supplier<T> call, String label) {
        int max = Math.max(0, properties.getMaxRetries());
        long backoff = Math.max(1, properties.getRetryInitialBackoffMs());
        RestClientException last = null;
        for (int attempt = 0; attempt <= max; attempt++) {
            try {
                return call.get();
            } catch (RestClientException ex) {
                last = ex;
                HttpStatusCode status = (ex instanceof org.springframework.web.client.RestClientResponseException rcre)
                        ? rcre.getStatusCode() : null;
                if (!isRetryable(status)) {
                    log.warn("[LienThong] {} attempt={} status={} không retry.", label, attempt, status);
                    throw ex;
                }
                log.warn("[LienThong] {} attempt={} status={} sẽ retry sau {}ms", label, attempt, status, backoff);
                sleep(backoff);
                backoff = Math.min(backoff * 2, 5_000L);
            }
        }
        log.error("[LienThong] {} đã hết retry.", label);
        throw last != null ? last : new IllegalStateException("HTTP call failed");
    }

    private boolean isRetryable(HttpStatusCode status) {
        if (status == null) return true; // network/timeout
        int v = status.value();
        if (v == 408 || v == 429) return true;
        return v >= 500 && v < 600;
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private static String newCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
