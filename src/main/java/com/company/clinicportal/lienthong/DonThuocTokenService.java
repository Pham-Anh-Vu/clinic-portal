package com.company.clinicportal.lienthong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Quản lý token cơ sở/bác sĩ. Token lưu trong cache in-memory; chỉ persist phiên bản mã hoá khi cần khôi phục.
 *
 * Chiến lược:
 * - Login trước khi cần dùng; cache token theo (coSoKey, bacSiKey).
 * - Refresh chủ động trước khi hết hạn theo skew (mặc định 600s).
 * - Single-flight refresh: nhiều luồng cùng gọi chỉ thực hiện 1 lần login.
 *
 * Đây là skeleton cho Giai đoạn 1. Tích hợp với entity {@code CoSoKhamChuaBenhLienThong}
 * và {@code NhanSu} sẽ làm ở Giai đoạn 2/4.
 */
@Service
public class DonThuocTokenService {

    private static final Logger log = LoggerFactory.getLogger(DonThuocTokenService.class);
    private static final long TOKEN_TTL_SECONDS_DEFAULT = 7L * 24 * 3600;

    private final LienThongProperties properties;
    private final LienThongHttpClient httpClient;

    private final ConcurrentHashMap<String, CachedToken> facilityTokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> facilityLocks = new ConcurrentHashMap<>();

    public DonThuocTokenService(LienThongProperties properties, LienThongHttpClient httpClient) {
        this.properties = properties;
        this.httpClient = httpClient;
    }

    public String getFacilityToken(String maLienThongCoSo, String password) {
        String key = maLienThongCoSo;
        CachedToken cached = facilityTokens.get(key);
        if (cached != null && !cached.isExpiringSoon(properties.getTokenRefreshSkewSeconds())) {
            return cached.token;
        }
        ReentrantLock lock = facilityLocks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            cached = facilityTokens.get(key);
            if (cached != null && !cached.isExpiringSoon(properties.getTokenRefreshSkewSeconds())) {
                return cached.token;
            }
            if (!properties.isEnabled()) {
                throw new LienThongApiException("Liên thông đang tắt (feature flag)", 0, null, null);
            }
            if (!properties.isProduction() && !properties.isAllowSandboxCalls()) {
                throw new LienThongApiException("Môi trường sandbox không được gọi API thật.", 0, null, null);
            }
            TokenResponse resp = httpClient.post(
                    "/api/auth/dang-nhap-co-so-kham-chua-benh",
                    new FacilityLoginRequest(maLienThongCoSo, password),
                    TokenResponse.class,
                    null);
            if (resp == null || resp.token == null) {
                throw new LienThongApiException("Phản hồi đăng nhập cơ sở không có token.", 0, null, null);
            }
            Instant exp = Instant.now().plusSeconds(TOKEN_TTL_SECONDS_DEFAULT);
            facilityTokens.put(key, new CachedToken(resp.token, exp));
            log.info("Đăng nhập cơ sở thành công; key={}, exp={}", key, exp);
            return resp.token;
        } finally {
            lock.unlock();
        }
    }

    public void invalidateFacility(String maLienThongCoSo) {
        facilityTokens.remove(maLienThongCoSo);
    }

    private static class CachedToken {
        final String token;
        final Instant expiresAt;
        CachedToken(String token, Instant expiresAt) {
            this.token = token;
            this.expiresAt = expiresAt;
        }
        boolean isExpiringSoon(int skewSeconds) {
            return expiresAt.minusSeconds(skewSeconds).isBefore(Instant.now());
        }
    }

    public static class FacilityLoginRequest {
        public String ma_lien_thong_co_so_kham_chua_benh;
        public String password;
        public FacilityLoginRequest() {}
        public FacilityLoginRequest(String ma, String pw) {
            this.ma_lien_thong_co_so_kham_chua_benh = ma;
            this.password = pw;
        }
    }

    public static class TokenResponse {
        public String token;
        public String token_type;
    }
}
