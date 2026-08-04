package com.company.clinicportal.lienthong;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Mã hoá AES-GCM 256 cho secret (password, token) lưu trong DB.
 * Có hỗ trợ key rotation: ciphertext gắn version để đọc đúng key.
 *
 * Quy ước: chuỗi mã hoá = "<version>:<base64(iv|ciphertext|tag)>".
 */
@Component
public class SecretCipher {

    private static final Logger log = LoggerFactory.getLogger(SecretCipher.class);
    private static final String VERSION_PREFIX = "v1";
    private static final int GCM_IV_LEN = 12;
    private static final int GCM_TAG_BITS = 128;
    /** Fallback dev — chỉ dùng khi không cấu hình key; KHÔNG dùng cho production. */
    private static final byte[] DEV_FALLBACK_KEY = padKey("clinic-portal-dev-key-do-not-use-in-prod");

    private final LienThongProperties properties;
    private final SecureRandom random = new SecureRandom();
    private SecretKey activeKey;
    private String activeVersion;

    public SecretCipher(LienThongProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        String b64 = properties.getCrypto().getKey();
        String version = properties.getCrypto().getKeyVersion();
        if (b64 != null && !b64.isBlank()) {
            try {
                byte[] raw = Base64.getDecoder().decode(b64.trim());
                if (raw.length != 16 && raw.length != 24 && raw.length != 32) {
                    throw new IllegalStateException("Crypto key length must be 16/24/32 bytes (AES-128/192/256).");
                }
                this.activeKey = new SecretKeySpec(raw, "AES");
                this.activeVersion = (version != null && !version.isBlank()) ? version : VERSION_PREFIX;
                log.info("SecretCipher initialized with configured key, version={}", activeVersion);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Crypto key is not valid Base64.", e);
            }
        } else if (properties.isProduction()) {
            throw new IllegalStateException("Production yêu cầu cấu hình clinicportal.lienthong.crypto.key (Base64 AES key).");
        } else {
            this.activeKey = new SecretKeySpec(DEV_FALLBACK_KEY, "AES");
            this.activeVersion = "dev-fallback";
            log.warn("SecretCipher đang dùng DEV FALLBACK KEY. KHÔNG được dùng cho production.");
        }
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[GCM_IV_LEN];
            random.nextBytes(iv);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, activeKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ct = c.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return activeVersion + ":" + Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("Encrypt failed", e);
        }
    }

    public String decrypt(String value) {
        if (value == null || value.isBlank()) return value;
        int idx = value.indexOf(':');
        if (idx <= 0) {
            log.warn("Ciphertext không có version prefix; không giải mã.");
            return null;
        }
        String version = value.substring(0, idx);
        String payload = value.substring(idx + 1);
        // MVP: chỉ hỗ trợ key hiện tại. Khi xoay key, cần map version → SecretKey.
        if (!version.equals(activeVersion)) {
            log.warn("Ciphertext version={} không khớp activeVersion={}; không giải mã.", version, activeVersion);
            return null;
        }
        try {
            byte[] raw = Base64.getDecoder().decode(payload);
            byte[] iv = new byte[GCM_IV_LEN];
            System.arraycopy(raw, 0, iv, 0, GCM_IV_LEN);
            byte[] ct = new byte[raw.length - GCM_IV_LEN];
            System.arraycopy(raw, GCM_IV_LEN, ct, 0, ct.length);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, activeKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(c.doFinal(ct), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Decrypt failed: {}", e.getMessage());
            return null;
        }
    }

    private static byte[] padKey(String seed) {
        try {
            byte[] src = seed.getBytes(StandardCharsets.UTF_8);
            byte[] out = new byte[32];
            for (int i = 0; i < out.length; i++) {
                out[i] = src[i % src.length];
            }
            return out;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
