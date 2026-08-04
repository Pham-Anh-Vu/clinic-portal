package com.company.clinicportal.lienthong.dto;

import java.util.List;

/**
 * Payload gửi đơn thuốc đÚNG theo spec BYT QĐ 808 Phần V.
 * Chỉ chứa các trường nằm trong JSON spec, không thừa trường nào.
 *
 * <p>Spec JSON:
 * <pre>
 * {
 *   "loai_don_thuoc": ...,
 *   "ma_don_thuoc": ...,
 *   "ho_ten_benh_nhan": ...,
 *   "ma_dinh_danh_y_te": ...,
 *   "ma_dinh_danh_cong_dan": ...,
 *   "ngay_sinh_benh_nhan": ...,
 *   "can_nang": ...,
 *   "gioi_tinh": ...,
 *   "ma_so_the_bao_hiem_y_te": ...,
 *   "thong_tin_nguoi_giam_ho": ...,
 *   "dia_chi": ...,
 *   "chan_doan": [...],
 *   "luu_y": ...,
 *   "hinh_thuc_dieu_tri": ...,
 *   "dot_dung_thuoc": [...],
 *   "thong_tin_don_thuoc": [...],
 *   "loi_dan": ...,
 *   "so_dien_thoai_nguoi_kham_benh": ...,
 *   "ngay_tai_kham": ...,
 *   "ngay_gio_ke_don": ...,
 *   "signature": ...
 * }
 * </pre>
 */
public class GuiDonThuocRequest {

    // === Bắt buộc ===
    /** Loại đơn: c (thường), h (hướng thần), n (gây nghiện), y (YHCT). */
    public String loai_don_thuoc;

    /** Mã đơn thuốc. 14 ký tự: 5 ký tự mã cơ sở + 7 ký tự tự sinh + hậu tố. */
    public String ma_don_thuoc;

    /** Họ tên bệnh nhân. */
    public String ho_ten_benh_nhan;

    /** Ngày sinh. Format: d/m/Y. */
    public String ngay_sinh_benh_nhan;

    /** Giới tính: 1 (chưa xác định), 2 (Nam), 3 (Nữ). */
    public String gioi_tinh;

    /** Địa chỉ bệnh nhân. */
    public String dia_chi;

    /** Danh sách chẩn đoán ICD-10. */
    public List<ChanDoanDto> chan_doan;

    /** Danh sách thuốc được kê. */
    public List<ThuocDto> thong_tin_don_thuoc;

    /** Ngày giờ kê đơn. Format: Y-m-d H:m:s. */
    public String ngay_gio_ke_don;

    // === Tùy chọn ===
    /** Mã định danh y tế. */
    public String ma_dinh_danh_y_te;

    /** Mã định danh công dân (CMND/CCCD). 12 số. */
    public String ma_dinh_danh_cong_dan;

    /** Cân nặng (kg). */
    public String can_nang;

    /** Mã số bảo hiểm y tế. */
    public String ma_so_the_bao_hiem_y_te;

    /** Thông tin người giám hộ. Bắt buộc khi BN < 72 tháng tuổi.
     *  Format: "Họ tên, SĐT, địa chỉ". */
    public String thong_tin_nguoi_giam_ho;

    /** Lưu ý của bác sĩ. */
    public String luu_y;

    /** Hình thức điều trị (số theo danh mục BYT). */
    public String hinh_thuc_dieu_tri;

    /** Đợt dùng thuốc - BYT yêu cầu SINGLE OBJECT (không phải list).
     *  Keys: dot, tu_ngay (d/m/Y), den_ngay (d/m/Y), so_thang_thuoc. */
    public DotDungThuocDto dot_dung_thuoc;

    /** Lời dặn của bác sĩ. */
    public String loi_dan;

    /** Số điện thoại người khám bệnh. */
    public String so_dien_thoai_nguoi_kham_benh;

    /** Số ngày tái khám (tính từ ngày kê đơn). */
    public String ngay_tai_kham;

    /** Chữ ký số. Bỏ qua ở MVP. */
    public String signature;

    // ============================================================
    // Nested DTOs
    // ============================================================

    /**
     * Chẩn đoán ICD-10.
     */
    public static class ChanDoanDto {
        public String ma_chan_doan;
        public String ten_chan_doan;
        public String ket_luan;
    }

    /**
     * Dòng thuốc trong đơn.
     */
    public static class ThuocDto {
        public String ma_thuoc;
        public String biet_duoc;
        public String ten_thuoc;
        public String don_vi_tinh;
        public String so_luong;
        public String cach_dung;
    }

    /**
     * Đợt dùng thuốc.
     */
    public static class DotDungThuocDto {
        /** Số đợt. */
        public String dot;
        /** Từ ngày. Format: d/m/Y. */
        public String tu_ngay;
        /** Đến ngày. Format: d/m/Y. */
        public String den_ngay;
        /** Số thang thuốc (YHCT). */
        public String so_thang_thuoc;
    }
}
