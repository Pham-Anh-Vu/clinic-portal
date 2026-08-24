package com.company.clinicportal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Loadảnh chữ ký bác sĩ dưới dạng base64 cho các service inấn (DOCX/HTML).
 * Tái sử dụng để tránh duplicate codeở ToDieuTriPrintService và SoBenhAnDetailView.
 *
 * Ảnh mặc định được đọc từ classpath {@code /reports/anh-chu-ky.jpg}.
 * Cache kết quả lần đầu đọc thành công; nếu file không có/không đọc được
 * thì log đúng 1 lần và trả về null (các nơi gọi sẽ bỏ qua việc chèn ảnh).
 */
@Service
public class ImageResourceLoader {

    private static final Logger log = LoggerFactory.getLogger(ImageResourceLoader.class);

    /** Classpath resource ảnh chữ ký mặc định (BS chỉ định). */
    private static final String DEFAULT_CHU_KY_RESOURCE = "/reports/anh-chu-ky.jpg";

    private final AtomicReference<String> chuKyBase64Cache = new AtomicReference<>();
    private final AtomicReference<Boolean> chuKyLoadAttempted = new AtomicReference<>(false);

    /**
     * Trả về base64 của ảnh chữ ký mặc định, hoặc null nếu không load được.
     */
    public String getDefaultChuKyBase64() {
        String cached = chuKyBase64Cache.get();
        if (cached != null) {
            return cached;
        }
        if (Boolean.TRUE.equals(chuKyLoadAttempted.get())) {
            return null;
        }
        synchronized (chuKyLoadAttempted) {
            if (Boolean.TRUE.equals(chuKyLoadAttempted.get())) {
                return chuKyBase64Cache.get();
            }
            try (InputStream in = getClass().getResourceAsStream(DEFAULT_CHU_KY_RESOURCE)) {
                if (in == null) {
                    log.warn("Không tìm thấy ảnh chữ ký trong classpath tại {}. Cột BS chỉ định sẽ chỉ hiển thị tên.",
                            DEFAULT_CHU_KY_RESOURCE);
                    chuKyLoadAttempted.set(true);
                    return null;
                }
                byte[] bytes = in.readAllBytes();
                String b64 = Base64.getEncoder().encodeToString(bytes);
                chuKyBase64Cache.set(b64);
                chuKyLoadAttempted.set(true);
                log.info("Đã load ảnh chữ ký mặc định ({} bytes) từ classpath {}.",
                        bytes.length, DEFAULT_CHU_KY_RESOURCE);
                return b64;
            } catch (IOException ex) {
                log.error("Không đọc được ảnh chữ ký tại {}. Cột BS chỉ định sẽ ch� hiển thị tên.",
                        DEFAULT_CHU_KY_RESOURCE, ex);
                chuKyLoadAttempted.set(true);
                return null;
            }
        }
    }
}
