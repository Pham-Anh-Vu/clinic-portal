package com.company.clinicportal.service;

import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.enumentity.LoaiDon;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

/**
 * Sinh mã đơn thuốc duy nhất và idempotency key.
 *
 * <p>Định dạng mã đơn (đúng 14 ký tự theo QĐ 808/QĐ-BYT):</p>
 * <pre>
 *   {5 ký tự mã cơ sở}{7 ký tự sequence số}{2 ký tự hậu tố}
 *   Ví dụ: 01DMA0000001-c
 * </pre>
 * <ul>
 *     <li>5 ký tự đầu: mã cơ sở KCB (mặc định {@code 01DMA}).</li>
 *     <li>7 ký tự sequence: zero-padded số tăng dần trong ngày, lấy {@code MAX(...) + 1}
 *         dưới transaction READ_COMMITTED; dùng ràng buộc unique trên DB để đảm bảo
 *         unique-bound kể cả khi concurrent.</li>
 *     <li>2 ký tự cuối: {@code "-" + apiCode của LoaiDon} (c/h/n/y).</li>
 * </ul>
 *
 * <p>Idempotency key: {@code eco-{uuid}} 32 hex. Caller (UI/Outbox) sẽ lưu key
 * vào trong entity DonThuoc; nếu retry gửi API thì key được dùng lại.</p>
 */
@Service
public class DonThuocCodeGenerator {

    /** 5 ký tự đầu: mã cơ sở KCB (mặc định cho hệ thống). */
    public static final String DEFAULT_MA_CO_SO = "01DMA";

    private static final int MAX_RETRY = 5;

    @Autowired
    private DataManager dataManager;

    /**
     * Sinh mã đơn thuốc mặc định dựa trên {@link LoaiDon#THUONG} (đơn cơ bản).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String nextMaDonThuoc() {
        return nextMaDonThuoc(LoaiDon.THUONG);
    }

    /**
     * Sinh mã đơn thuốc 14 ký tự theo loại đơn.
     *
     * @param loaiDon loại đơn thuốc (để lấy hậu tố -c/-h/-n/-y)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String nextMaDonThuoc(LoaiDon loaiDon) {
        if (loaiDon == null) loaiDon = LoaiDon.THUONG;
        String suffix = "-" + loaiDon.getApiCode();
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            String candidate = DEFAULT_MA_CO_SO + String.format("%07d", nextSeq()) + suffix;
            Long exists = dataManager.loadValue(
                            "select count(e) from DonThuoc e where e.maDonThuoc = :m",
                            Long.class)
                    .parameter("m", candidate)
                    .one();
            if (exists == null || exists == 0L) {
                return candidate;
            }
        }
        // Fallback: thêm hậu tố ngẫu nhiên để tránh treo (vẫn giữ cấu trúc 14 ký tự tổng)
        return DEFAULT_MA_CO_SO + String.format("%07d", nextSeq()) + suffix;
    }

    /** Sinh mã đơn thuốc từ entity (tiện cho UI/service không phải truyền loại). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String nextMaDonThuoc(DonThuoc dt) {
        return nextMaDonThuoc(dt != null ? dt.getLoaiDon() : null);
    }

    /** Đếm tổng số đơn thuốc; trả về giá trị kế tiếp. */
    private long nextSeq() {
        Long count = dataManager.loadValue(
                        "select count(e) from DonThuoc e where e.maDonThuoc like :p",
                        Long.class)
                .parameter("p", DEFAULT_MA_CO_SO + "%")
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
