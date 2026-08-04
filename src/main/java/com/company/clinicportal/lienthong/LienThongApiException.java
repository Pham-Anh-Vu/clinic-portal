package com.company.clinicportal.lienthong;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Exception chuẩn hoá cho mọi lỗi gọi API liên thông.
 * Tránh để exception nguyên bản của RestClient rò rỉ ra tầng UI/service.
 */
public class LienThongApiException extends RuntimeException {

    private final int httpStatus;
    private final String correlationId;
    private final List<String> serverErrors;
    private final boolean retryable;

    public LienThongApiException(String message, int httpStatus, String correlationId, List<String> serverErrors) {
        super(message);
        this.httpStatus = httpStatus;
        this.correlationId = correlationId;
        this.serverErrors = serverErrors == null ? List.of() : serverErrors;
        this.retryable = (httpStatus == 0 || httpStatus == 408 || httpStatus == 429
                || (httpStatus >= 500 && httpStatus < 600));
    }

    /** Constructor mở rộng: chỉ định tường minh {@code retryable} + body string. */
    public LienThongApiException(String message, int httpStatus, String correlationId, String body, boolean retryable) {
        super(message);
        this.httpStatus = httpStatus;
        this.correlationId = correlationId;
        this.serverErrors = body == null || body.isBlank() ? List.of() : List.of(body);
        this.retryable = retryable;
    }

    public LienThongApiException(String message, RestClientException cause) {
        super(message, cause);
        HttpStatusCode status = (cause instanceof org.springframework.web.client.RestClientResponseException rcre)
                ? rcre.getStatusCode() : null;
        this.httpStatus = status == null ? 0 : status.value();
        this.correlationId = null;
        this.serverErrors = List.of();
        this.retryable = (this.httpStatus == 0 || this.httpStatus == 408 || this.httpStatus == 429
                || (this.httpStatus >= 500 && this.httpStatus < 600));
    }

    public int getHttpStatus() { return httpStatus; }
    public String getCorrelationId() { return correlationId; }
    public List<String> getServerErrors() { return serverErrors; }

    /** Phân loại để chọn retry policy trên outbox. */
    public boolean isRetryable() {
        return retryable;
    }

    public boolean isAuthError() {
        return httpStatus == 401 || httpStatus == 403;
    }
}
