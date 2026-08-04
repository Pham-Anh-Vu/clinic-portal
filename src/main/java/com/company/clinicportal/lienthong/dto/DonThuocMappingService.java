package com.company.clinicportal.lienthong.dto;

import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.entity.DonThuocChanDoan;
import com.company.clinicportal.entity.DonThuocChiTiet;
import com.company.clinicportal.entity.DonThuocDotDung;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Chuyển đổi {@link DonThuoc} (JPA entity) sang {@link GuiDonThuocRequest}
 * (snake_case JSON DTO) cho QĐ 808/QĐ-BYT.
 *
 * <p>Quy tắc:</p>
 * <ul>
 *     <li>Luôn ưu tiên các trường snapshot (đã có sẵn trong entity) để đảm bảo
 *         đơn thuốc ổn định khi danh mục cập nhật.</li>
 *     <li>Các trường ngày (LocalDate) chuyển sang YYYY-MM-DD.</li>
 *     <li>Không trả về plaintext token / CCCD thô ra ngoài JSON cuối cùng.</li>
 *     <li>Số lượng, hàm lượng giữ dưới dạng String để tránh tràn precision.</li>
 * </ul>
 */
public final class DonThuocMappingService {

    private DonThuocMappingService() {}

    public static GuiDonThuocRequest fromEntity(DonThuoc dt) {
        GuiDonThuocRequest r = new GuiDonThuocRequest();
        if (dt == null) return r;
        r.ten_don_vi = dt.getTenDonVi();
        r.ma_co_so_kham_chua_benh = dt.getMaCoSoKcb();
        r.ma_lien_thong_bac_si = dt.getMaLienThongBacSi();
        r.so_cchn_bac_si = dt.getSoCchnBacSi();
        r.ho_va_ten_bac_si = dt.getTenBacSi();
        r.ngay_ke_don = toLocalDate(dt.getNgayKe());
        r.so_vao_vien = dt.getSoVaoVien();
        r.ho_va_ten_benh_nhan = dt.getHoVaTenBenhNhan();
        r.ngay_sinh_benh_nhan = toLocalDate(dt.getNgaySinh());
        r.gioi_tinh_benh_nhan = dt.getGioiTinh();
        r.so_dinh_danh_y_te = dt.getMaDinhDanhYTe();
        r.so_cmnd_cccd = dt.getMaDinhDanhCongDan();
        r.so_dien_thoai = dt.getSoDienThoai();
        r.dia_chi = dt.getDiaChi();
        r.can_nang = dt.getCanNang() == null ? null : dt.getCanNang().toString();
        r.chieu_cao = dt.getChieuCao() == null ? null : dt.getChieuCao().toString();
        r.ma_quoc_tich = dt.getMaQuocTich();
        r.nghe_nghiep = dt.getNgheNghiep();
        r.nguoi_giam_ho_ho_ten = dt.getNguoiGiamHoHoTen();
        r.nguoi_giam_ho_dien_thoai = dt.getNguoiGiamHoSoDienThoai();
        r.nguoi_giam_ho_dia_chi = dt.getNguoiGiamHoDiaChi();
        r.loai_don = dt.getLoaiDon();
        r.hinh_thuc_dieu_tri = dt.getHinhThucDieuTri();
        r.chan_doan_text = dt.getChanDoanText();
        r.ly_do_kham = dt.getLyDoKham();
        r.loi_dan = dt.getLoiDan();
        r.ngay_tai_kham = toLocalDate(dt.getNgayTaiKham());
        r.chu_ky_bac_si = dt.getChuKyBacSi();
        r.chu_ky_benh_nhan = dt.getChuKyBenhNhan();

        List<GuiDonThuocRequest.DonThuocChiTietDto> lines = new ArrayList<>();
        if (dt.getChiTiets() != null) {
            for (DonThuocChiTiet ct : dt.getChiTiets()) {
                GuiDonThuocRequest.DonThuocChiTietDto line = new GuiDonThuocRequest.DonThuocChiTietDto();
                line.stt = ct.getStt();
                line.ma_thuoc = ct.getMaThuocSnapshot();
                line.ten_thuoc = ct.getTenThuocSnapshot();
                line.biet_duoc = ct.getBietDuocSnapshot();
                line.dang_bao_che = ct.getDangBaoCheSnapshot();
                line.ham_luong = ct.getHamLuongSnapshot();
                line.don_vi_tinh = ct.getDonViTinhSnapshot();
                line.duong_dung = ct.getDuongDung();
                line.lieu_dung = ct.getLieuDung();
                line.tan_suat = ct.getTanSuat();
                line.thoi_gian_dung = ct.getThoiGianDung();
                line.cach_dung = ct.getCachDung();
                line.so_luong = ct.getSoLuong() == null ? null : ct.getSoLuong().toPlainString();
                line.ghi_chu = ct.getGhiChu();
                lines.add(line);
            }
        }
        r.don_thuoc_chi_tiet = lines;

        List<GuiDonThuocRequest.ChanDoanDto> cds = new ArrayList<>();
        if (dt.getChanDoans() != null) {
            for (DonThuocChanDoan cd : dt.getChanDoans()) {
                GuiDonThuocRequest.ChanDoanDto dto = new GuiDonThuocRequest.ChanDoanDto();
                dto.stt = cd.getStt();
                dto.ma_icd = cd.getMaIcdSnapshot();
                dto.ten_benh = cd.getTenIcdSnapshot();
                dto.ket_luan = cd.getKetLuan();
                cds.add(dto);
            }
        }
        r.chan_doan = cds;

        List<GuiDonThuocRequest.DotDungDto> dots = new ArrayList<>();
        if (dt.getDotDungs() != null) {
            for (DonThuocDotDung dd : dt.getDotDungs()) {
                GuiDonThuocRequest.DotDungDto dto = new GuiDonThuocRequest.DotDungDto();
                dto.so_dot = dd.getSoDot();
                dto.tu_ngay = toLocalDate(dd.getTuNgay());
                dto.den_ngay = toLocalDate(dd.getDenNgay());
                dto.so_thang_thuoc = dd.getSoThangThuoc();
                dots.add(dto);
            }
        }
        r.dot_dung = dots;
        return r;
    }

    private static LocalDate toLocalDate(java.util.Date d) {
        return d == null ? null : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
