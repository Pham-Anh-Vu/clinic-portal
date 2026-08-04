package com.company.clinicportal.lienthong.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Payload gửi đơn thuốc theo QĐ 808/QĐ-BYT.
 * Tên trường theo snake_case theo yêu cầu JSON của BYT.
 */
public class GuiDonThuocRequest {

    public String ten_don_vi;
    public String ma_co_so_kham_chua_benh;
    public String ma_lien_thong_bac_si;
    public String so_cchn_bac_si;
    public String ho_va_ten_bac_si;
    /** YYYY-MM-DD. */
    public LocalDate ngay_ke_don;
    public String so_vao_vien;
    public String ho_va_ten_benh_nhan;
    public LocalDate ngay_sinh_benh_nhan;
    public String gioi_tinh_benh_nhan;
    public String so_dinh_danh_y_te;
    public String so_cmnd_cccd;
    public String so_dien_thoai;
    public String dia_chi;
    public String can_nang;
    public String chieu_cao;
    public String ma_quoc_tich;
    public String nghe_nghiep;
    public String nguoi_giam_ho_ho_ten;
    public String nguoi_giam_ho_dien_thoai;
    public String nguoi_giam_ho_dia_chi;
    public String loai_don;
    public String hinh_thuc_dieu_tri;
    public String chan_doan_text;
    public String ly_do_kham;
    public String loi_dan;
    public LocalDate ngay_tai_kham;
    public String chu_ky_bac_si;
    public String chu_ky_benh_nhan;
    public List<DonThuocChiTietDto> don_thuoc_chi_tiet;
    public List<ChanDoanDto> chan_doan;
    public List<DotDungDto> dot_dung;

    public static class DonThuocChiTietDto {
        public Integer stt;
        public String ma_thuoc;
        public String ten_thuoc;
        public String biet_duoc;
        public String dang_bao_che;
        public String ham_luong;
        public String don_vi_tinh;
        public String so_dang_ky;
        public String duong_dung;
        public String lieu_dung;
        public String tan_suat;
        public String thoi_gian_dung;
        public String cach_dung;
        public String so_luong;
        public String ghi_chu;
    }

    public static class ChanDoanDto {
        public Integer stt;
        public String ma_icd;
        public String ten_benh;
        public String ket_luan;
    }

    public static class DotDungDto {
        public Integer so_dot;
        public LocalDate tu_ngay;
        public LocalDate den_ngay;
        public Integer so_thang_thuoc;
    }
}
