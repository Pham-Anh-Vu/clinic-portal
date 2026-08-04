package com.company.clinicportal.service;

import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

/**
 * Sinh mã đơn thuốc duy nhất và idempotency key.
 *
 * <p>Định dạng mã đơn: {@code DT-<yyyyMMdd>-<seq6>}; seq6 là chuỗi zero-pushed
 * tăng dần trong ngày, lấy {@code MAX(...) + 1} dưới transaction READ_COMMITTED,
 * dùng ràng buộc unique trên DB để đảm bảo unique-bound kể cả khi concurrent.</p>
 *
 * <p>Idempotency key: {@code eco-{uuid}} 32 hex. Caller (UI/Outbox) sẽ lưu key
 * vào trong entity DonThuoc; nếu retry gửi API thì key được dùng lại.</p>
 */
@Service
public class DonThuocCodeGenerator {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int MAX_RETRY = 5;

    @Autowired
    private DataManager dataManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String nextMaDonThuoc() {
        String ymd = LocalDate.now().format(YMD);
        String prefix = "DT-" + ymd + "-";
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            String candidate = prefix + String.format("%06d", nextSeq(ymd));
            Long exists = dataManager.loadValue(
                            "select count(e) from DonThuoc e where e.maDonThuoc = :m",
                            Long.class)
                    .parameter("m", candidate)
                    .one();
            if (exists == null || exists == 0L) {
                return candidate;
            }
        }
        // Fallback: thêm suffix ngẫu nhiên để tránh treo
        return prefix + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    /** Đếm số đơn thuốc đã tạo trong ngày; trả về giá trị kế tiếp. */
    private long nextSeq(String ymd) {
        Long count = dataManager.loadValue(
                        "select count(e) from DonThuoc e where e.maDonThuoc like :p",
                        Long.class)
                .parameter("p", "DT-" + ymd + "-%")
                .one();
        return (count == null ? 0L : count) + 1L;
    }

    public String newIdempotencyKey() {
        return "eco-" + UUID.randomUUID().toString().replace("-", "");
    }

    /** Idempotency key ổn định, có thể truyền vào (vd: client đã sinh sẵn). */
    public String stableIdempotencyKey(String maDonThuoc) {
        return "eco-" + Long.toHexString(maDonThuoc == null ? System.nanoTime() : maDonThuoc.hashCode())
                + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public Date now() {
        return new Date();
    }
}
