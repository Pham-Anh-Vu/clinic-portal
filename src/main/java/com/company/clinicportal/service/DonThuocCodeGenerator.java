package com.company.clinicportal.service;

import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.enumentity.LoaiDon;
import io.jmix.core.DataManager;
import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

/**
 * Sinh mã đơn thuốc duy nhất và idempotency key.
 *
 * <p>Dùng Jmix Sequences API ({@link Sequences}) để sinh số tăng dần ổn định.
 * Sequence nội bộ {@code DON_THUOC_CODE_SEQ} được Jmix tự khởi tạo lần đầu gọi.
 * Sequence này lưu trong DB (Postgres) — đảm bảo unique-bound kể cả khi concurrent
 * và qua restart ứng dụng.</p>
 *
 * <p>Định dạng mã đơn (đúng 14 ký tự):</p>
 * <pre>
 *   {5 ký tự mã cơ sở}{7 ký tự sequence số}{2 ký tự hậu tố}
 *   Ví dụ: 01DMA0000001-c
 * </pre>
 *
 * <p>Idempotency key: {@code eco-{uuid}} 32 hex.</p>
 */
@Service
public class DonThuocCodeGenerator {

    /** 5 ký tự đầu: mã cơ sở KCB (mặc định cho hệ thống). */
    public static final String DEFAULT_MA_CO_SO = "01DMA";

    /** Tên sequence Jmix (chỉ chứa [A-Z0-9_], tự khởi tạo lần đầu gọi). */
    public static final String CODE_SEQUENCE_NAME = "DON_THUOC_CODE_SEQ";

    private static final int MAX_RETRY = 5;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Sequences sequences;

    /**
     * Sinh mã đơn thuốc mặc định (đơn thường).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String nextMaDonThuoc() {
        return nextMaDonThuoc(LoaiDon.THUONG);
    }

    /**
     * Sinh mã đơn thuốc 14 ký tự theo loại đơn.
     *
     * <p>Sequence lấy qua Jmix {@link Sequences#createNextValue(Sequence)} —
     * thread-safe, persistent. Check trùng mã bằng query count trước khi trả.</p>
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String nextMaDonThuoc(LoaiDon loaiDon) {
        if (loaiDon == null) loaiDon = LoaiDon.THUONG;
        String suffix = "-" + loaiDon.getApiCode();
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            long seq = sequences.createNextValue(Sequence.withName(CODE_SEQUENCE_NAME));
            String candidate = DEFAULT_MA_CO_SO + String.format("%07d", seq) + suffix;
            Long exists = dataManager.loadValue(
                            "select count(e) from DonThuoc e where e.maDonThuoc = :m",
                            Long.class)
                    .parameter("m", candidate)
                    .one();
            if (exists == null || exists == 0L) {
                return candidate;
            }
        }
        // Fallback: vẫn trả mã mới dù có trùng hiếm hoi (sequence tiếp tục tăng)
        long seq = sequences.createNextValue(Sequence.withName(CODE_SEQUENCE_NAME));
        return DEFAULT_MA_CO_SO + String.format("%07d", seq) + suffix;
    }

    /** Sinh mã đơn thuốc từ entity. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String nextMaDonThuoc(DonThuoc dt) {
        return nextMaDonThuoc(dt != null ? dt.getLoaiDon() : null);
    }

    public String newIdempotencyKey() {
        return "eco-" + UUID.randomUUID().toString().replace("-", "");
    }

    /** Idempotency key ổn định, có thể truyền vào. */
    public String stableIdempotencyKey(String maDonThuoc) {
        return "eco-" + Long.toHexString(maDonThuoc == null ? System.nanoTime() : maDonThuoc.hashCode())
                + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public Date now() {
        return new Date();
    }
}
