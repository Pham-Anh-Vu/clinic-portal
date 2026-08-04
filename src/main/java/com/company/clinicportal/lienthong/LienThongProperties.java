package com.company.clinicportal.lienthong;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình liên thông đơn thuốc quốc gia (Quyết định 808/QĐ-BYT).
 * Không chứa credential: secret/password lưu riêng và mã hoá trong DB.
 */
@ConfigurationProperties(prefix = "clinicportal.lienthong")
public class LienThongProperties {

    /** sandbox | production */
    private String environment = "sandbox";

    /** URL gốc của API, ví dụ https://api.donthuocquocgia.vn */
    private String apiBaseUrl = "https://api.donthuocquocgia.vn";

    private int connectTimeoutMs = 10_000;
    private int readTimeoutMs = 30_000;
    private int maxRetries = 3;
    private int retryInitialBackoffMs = 500;

    /** Số giây "an toàn" trước khi token hết hạn thật sự để chủ động refresh. */
    private int tokenRefreshSkewSeconds = 600;

    /** EMPTY | PLACEHOLDER | OMIT — MVP khi chưa có chữ ký số. */
    private String signatureMode = "PLACEHOLDER";

    /** Bật/tắt liên thông ở cấp env (feature flag). */
    private boolean enabled = true;

    /** Có cho phép gọi API khi đang ở môi trường sandbox không. */
    private boolean allowSandboxCalls = true;

    /** Mã cơ sở dùng cho tiền tố mã đơn thuốc (5 ký tự đầu). Có thể override trong DB. */
    private String maCoSoKhamChuaBenh = "";

    private Crypto crypto = new Crypto();

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getApiBaseUrl() { return apiBaseUrl; }
    public void setApiBaseUrl(String apiBaseUrl) { this.apiBaseUrl = apiBaseUrl; }

    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public int getRetryInitialBackoffMs() { return retryInitialBackoffMs; }
    public void setRetryInitialBackoffMs(int retryInitialBackoffMs) { this.retryInitialBackoffMs = retryInitialBackoffMs; }

    public int getTokenRefreshSkewSeconds() { return tokenRefreshSkewSeconds; }
    public void setTokenRefreshSkewSeconds(int tokenRefreshSkewSeconds) { this.tokenRefreshSkewSeconds = tokenRefreshSkewSeconds; }

    public String getSignatureMode() { return signatureMode; }
    public void setSignatureMode(String signatureMode) { this.signatureMode = signatureMode; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isAllowSandboxCalls() { return allowSandboxCalls; }
    public void setAllowSandboxCalls(boolean allowSandboxCalls) { this.allowSandboxCalls = allowSandboxCalls; }

    public String getMaCoSoKhamChuaBenh() { return maCoSoKhamChuaBenh; }
    public void setMaCoSoKhamChuaBenh(String maCoSoKhamChuaBenh) { this.maCoSoKhamChuaBenh = maCoSoKhamChuaBenh; }

    public Crypto getCrypto() { return crypto; }
    public void setCrypto(Crypto crypto) { this.crypto = crypto; }

    public boolean isProduction() { return "production".equalsIgnoreCase(environment); }

    public static class Crypto {
        /** Base64 của 32 byte (AES-256). Bắt buộc ở production. */
        private String key = "";
        private String keyVersion = "dev-1";

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public String getKeyVersion() { return keyVersion; }
        public void setKeyVersion(String keyVersion) { this.keyVersion = keyVersion; }
    }
}
