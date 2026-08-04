package com.company.clinicportal.lienthong.dto;

import java.util.List;

/**
 * Response từ /api/v1/gui-don-thuoc theo QĐ 808/QĐ-BYT Phần V.
 *
 * <p>BYT trả về JSON object có dạng:
 * <pre>
 * Thành công (HTTP 200): { "success": "Gửi đơn thuốc thành công", "checksum": "..." }
 * Lỗi validation (HTTP 422): { "success": "Đơn thuốc đã được sử dụng trước đó...",
 *                              "danh_sach_cac_loi": [...], "checksum": "..." }
 * </pre>
 *
 * <p>Lưu ý: {@code success} là STRING không phải boolean. Chuỗi mang nội dung
 * thông báo - nếu chứa "thành công" (không phân biệt hoa/thường) thì API đã nhận đơn.
 * Ngược lại coi như thất bại.</p>
 *
 * <p>Việc check HTTP 200 + nội dung "thành công" dùng làm cơ sở duy nhất để set
 * {@code trangThai=PHAT_HANH}, tránh gửi lại đơn (BYT sẽ từ chối vì đã sử dụng).</p>
 */
public class GuiDonThuocResponse {

    /**
     * Mã trạng thái nội bộ (do service set, không từ BYT).
     * - "OK" = gửi thành công
     * - "ERROR" = lỗi
     */
    public String status;

    /**
     * Thông điệp từ BYT (chính là giá trị {@code success} trong JSON).
     * Thành công: "Gửi đơn thuốc thành công".
     * Lỗi: thông điệp lỗi, ví dụ "Đơn thuốc đã được sử dụng trước đó...".
     */
    public String message;

    /** Checksum BYT trả về (lưu lại để đối chiếu, chưa dùng trong idempotency). */
    public String checksum;

    /** True nếu {@code success} chứa cụm "thành công" (set từ JSON). */
    public Boolean successFlag;

    /**
     * Danh sách lỗi validation. Có giá trị khi HTTP 422.
     */
    public List<DanhSachLoi> danh_sach_cac_loi;

    /**
     * HTTP status code gốc từ BYT.
     */
    public transient int httpStatus;

    public GuiDonThuocResponse() {}

    /**
     * Factory: tạo response thành công.
     */
    public static GuiDonThuocResponse ok(String message) {
        GuiDonThuocResponse r = new GuiDonThuocResponse();
        r.status = "OK";
        r.successFlag = Boolean.TRUE;
        r.message = message;
        return r;
    }

    /**
     * Factory: tạo response lỗi.
     */
    public static GuiDonThuocResponse error(String message) {
        GuiDonThuocResponse r = new GuiDonThuocResponse();
        r.status = "ERROR";
        r.successFlag = Boolean.FALSE;
        r.message = message;
        return r;
    }

    /**
     * Check thành công theo logic BYT 808:
     * <ul>
     *     <li>flag nội bộ {@link #successFlag} = true → OK.</li>
     *     <li>message chứa "thành công" (case-insensitive) → OK.</li>
     *     <li>HTTP 200 + body không có từ khoá lỗi → OK (BYT trả chuỗi thuần).</li>
     * </ul>
     */
    public boolean isSuccess() {
        if (Boolean.TRUE.equals(successFlag)) return true;
        if (status != null && "OK".equals(status)) return true;
        if (message != null && containsIgnoreCase(message, "thành công")) return true;
        // BYT đôi khi trả thẳng HTTP 200 với chuỗi "Gửi đơn thuốc thành công" (không phải JSON)
        if (httpStatus == 200 && message != null
                && !containsIgnoreCase(message, "lỗi")
                && !containsIgnoreCase(message, "đã được sử dụng")
                && !containsIgnoreCase(message, "thất bại")) {
            return true;
        }
        return false;
    }

    private static boolean containsIgnoreCase(String haystack, String needle) {
        if (haystack == null || needle == null) return false;
        return haystack.toLowerCase().contains(needle.toLowerCase());
    }

    /**
     * Lỗi validation từ BYT (HTTP 422).
     */
    public static class DanhSachLoi {
        /** Tên trường bị lỗi. */
        public String truong;

        /** Mã lỗi. */
        public String ma_loi;

        /** Thông báo lỗi. */
        public String thong_bao;

        public DanhSachLoi() {}

        public DanhSachLoi(String truong, String ma_loi, String thong_bao) {
            this.truong = truong;
            this.ma_loi = ma_loi;
            this.thong_bao = thong_bao;
        }

        @Override
        public String toString() {
            return (truong != null ? truong + ": " : "")
                    + (ma_loi != null ? "[" + ma_loi + "] " : "")
                    + (thong_bao != null ? thong_bao : "");
        }
    }
}
