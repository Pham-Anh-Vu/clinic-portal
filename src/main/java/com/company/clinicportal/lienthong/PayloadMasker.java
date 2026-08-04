package com.company.clinicportal.lienthong;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tiện ích mask PII/secret trước khi ghi log/audit.
 * Chỉ mask các trường nhạy cảm theo whitelist; các trường khác giữ nguyên.
 */
public final class PayloadMasker {

    private static final String MASK = "***";

    private PayloadMasker() {}

    public static Map<String, Object> mask(Map<String, Object> input) {
        if (input == null) return null;
        Map<String, Object> out = new LinkedHashMap<>(input.size());
        for (Map.Entry<String, Object> e : input.entrySet()) {
            String key = e.getKey() == null ? "" : e.getKey().toLowerCase();
            if (isSecretKey(key)) {
                out.put(e.getKey(), MASK);
            } else {
                out.put(e.getKey(), e.getValue());
            }
        }
        return out;
    }

    private static boolean isSecretKey(String lower) {
        return lower.contains("password")
                || lower.contains("token")
                || lower.contains("secret")
                || lower.contains("signature")
                || lower.contains("app_key")
                || lower.contains("app-key")
                || lower.contains("ma_dinh_danh_cong_dan") // CCCD
                || lower.contains("ma_so_the_bao_hiem_y_te")
                || lower.contains("so_dien_thoai");
    }
}
