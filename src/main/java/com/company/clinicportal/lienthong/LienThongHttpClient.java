package com.company.clinicportal.lienthong;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
    private final ObjectMapper objectMapper;
    private final HttpClient rawHttpClient;

    public LienThongHttpClient(LienThongProperties properties,
                               RestClient lienThongRestClient,
                               @Qualifier("lienThongObjectMapper") ObjectMapper objectMapper) {
        this.properties = properties;
        this.restClient = lienThongRestClient;
        this.objectMapper = objectMapper;
        // HttpClient native — dùng để đọc raw response body mà KHÔNG bị Jackson converter ép kiểu
        // (BYT 808 trả 200 OK + application/json, RestClient extract String/byte[] fail)
        this.rawHttpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000, properties.getConnectTimeoutMs())))
                .build();
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

    /**
     * POST rồi trả raw String body — dùng cho các endpoint BYT 808/QĐ-BYT trả JSON/plain text
     * (ví dụ: "/api/v1/gui-don-thuoc" trả 200 OK + {@code {"success":"...","checksum":"..."}}).
     *
     * <p><b>Tại sao không dùng RestClient:</b> {@code RestClient.body(String.class)} và
     * {@code RestClient.toEntity(byte[].class)} đều mặc định dùng JacksonHttpMessageConverter.
     * Khi content-type là application/json nhưng target type là String/byte[], converter ném
     * {@code RestClientException: Error while extracting response for type [...] and content
     * type [application/json]} dù HTTP status 200 OK. Điều này khiến đơn thuốc đã được BYT
     * lưu thành công lại bị báo cáo là fail.</p>
     *
     * <p>Cách fix: bỏ qua toàn bộ Jackson extractor, dùng
     * {@link java.net.http.HttpClient} native để đọc raw body (bytes/UTF-8) và chỉ
     * tự throw {@link HttpStatusCodeException} khi status >= 400 (để flow retry/audit
     * hoạt động như cũ).</p>
     */
    public String postForString(String path, Object body, String bearerToken) {
        return executeWithRetry(() -> {
            String correlationId = newCorrelationId();
            String url = properties.getApiBaseUrl() + path;
            byte[] bodyBytes;
            try {
                bodyBytes = objectMapper.writeValueAsBytes(body);
            } catch (JsonProcessingException e) {
                throw new RestClientException("Serialize body failed: " + e.getMessage(), e);
            }

            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(Math.max(1000, properties.getReadTimeoutMs())))
                    .header("Content-Type", "application/json")
                    .header(HDR_CORRELATION, correlationId)
                    .header("Accept", "application/json, text/plain, */*");
            if (bearerToken != null && !bearerToken.isBlank()) {
                b.header(HDR_AUTHORIZATION, "bearer " + bearerToken);
            }
            HttpRequest req = b.POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes)).build();

            HttpResponse<byte[]> resp;
            try {
                resp = rawHttpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
            } catch (java.io.IOException io) {
                // Bọc lại thành RestClientException để retry theo flow cũ
                throw new RestClientException("I/O error calling " + url + ": " + io.getMessage(), io);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RestClientException("Interrupted calling " + url, ie);
            }

            int status = resp.statusCode();
            byte[] payload = resp.body();
            String text = payload == null ? "" : new String(payload, StandardCharsets.UTF_8);

            if (status >= 400) {
                // Tái tạo HttpStatusCodeException để các catch-block phía trên (HttpStatusCodeException
                // + recordFailure) vẫn hoạt động như cũ — dùng HttpClientErrorException cho 4xx,
                // HttpServerErrorException cho 5xx (đều extend HttpStatusCodeException).
                byte[] errBody = text == null ? new byte[0] : payload;
                if (status >= 500) {
                    throw new org.springframework.web.client.HttpServerErrorException(
                            org.springframework.http.HttpStatus.valueOf(status),
                            status + " " + text,
                            org.springframework.http.HttpHeaders.EMPTY,
                            errBody,
                            StandardCharsets.UTF_8);
                }
                throw new org.springframework.web.client.HttpClientErrorException(
                        org.springframework.http.HttpStatus.valueOf(status),
                        status + " " + text,
                        org.springframework.http.HttpHeaders.EMPTY,
                        errBody,
                        StandardCharsets.UTF_8);
            }
            return text;
        }, "POST " + path);
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
                    // Log đầy đủ nguyên nhân parse-fail / non-retryable để debug
                    log.warn("[LienThong] {} attempt={} status={} không retry. Cause: {} {}",
                            label, attempt, status,
                            ex.getClass().getSimpleName(),
                            ex.getMessage() != null ? ex.getMessage().replace('\n', ' ') : "");
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
        if (status == null) return false; // parse fail / unknown — không retry vì có thể request đã tới server
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
