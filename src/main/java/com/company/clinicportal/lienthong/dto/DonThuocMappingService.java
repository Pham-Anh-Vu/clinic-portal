package com.company.clinicportal.lienthong.dto;

import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.entity.DonThuocChanDoan;
import com.company.clinicportal.entity.DonThuocChiTiet;
import com.company.clinicportal.entity.DonThuocDotDung;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Chuyển đổi {@link DonThuoc} (JPA entity) sang {@link GuiDonThuocRequest}
 * đÚNG theo spec JSON của BYT QĐ 808 Phần V.
 *
 * <p>Chỉ map các trường nằm trong spec, bỏ qua trường thừa.
 * Format ngày:
 *   - ngay_sinh_benh_nhan, dot_dung_thuoc.*.tu_ngay/den_ngay = d/m/Y
 *   - ngay_gio_ke_don = Y-m-d H:i:s
 *   - ngay_tai_kham = số ngày (String)</p>
 */
public final class DonThuocMappingService {

    private DonThuocMappingService() {}

    private static final DateTimeFormatter NGAY_DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter NGAY_GIO_YMD_HMS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static GuiDonThuocRequest fromEntity(DonThuoc dt) {
        GuiDonThuocRequest r = new GuiDonThuocRequest();
        if (dt == null) return r;

        // === Bắt buộc ===
        r.loai_don_thuoc      = dt.getLoaiDon() == null ? null : dt.getLoaiDon().getApiCode();
        r.ma_don_thuoc        = dt.getMaDonThuoc();
        r.ho_ten_benh_nhan    = dt.getHoVaTenBenhNhan();
        r.ngay_sinh_benh_nhan = formatDmy(dt.getNgaySinh());
        // FIX 1: GioiTinh enum (1=Nam,2=Nu,3=ChuaXacDinh) → String "1","2","3"
        r.gioi_tinh           = mapGioiTinh(dt.getGioiTinh());
        r.dia_chi             = dt.getDiaChi();
        // FIX 4: Format Y-m-d H:i:s cho ngay_gio_ke_don
        r.ngay_gio_ke_don     = formatYmdHms(dt.getNgayKe());

        // === Tùy chọn ===
        r.ma_dinh_danh_y_te           = dt.getMaDinhDanhYTe();
        r.ma_dinh_danh_cong_dan       = dt.getMaDinhDanhCongDan();
        r.can_nang                     = dt.getCanNang() == null ? null : dt.getCanNang().toString();
        // ma_so_the_bao_hiem_y_te: entity chưa có
        r.thong_tin_nguoi_giam_ho     = buildNguoiGiamHo(dt);
        // luu_y: entity chưa có field riêng
        r.hinh_thuc_dieu_tri           = dt.getHinhThucDieuTri() == null ? null
                : dt.getHinhThucDieuTri().getId();
        r.loi_dan                      = dt.getLoiDan();
        r.so_dien_thoai_nguoi_kham_benh = dt.getSoDienThoai();
        // FIX 3: ngay_tai_kham phải là SỐ ngày (String), không phải ngày
        r.ngay_tai_kham = buildNgayTaiKham(dt);

        // === Danh sách thuốc (thong_tin_don_thuoc) ===
        List<GuiDonThuocRequest.ThuocDto> thuocs = new ArrayList<>();
        if (dt.getChiTiets() != null) {
            for (DonThuocChiTiet ct : dt.getChiTiets()) {
                GuiDonThuocRequest.ThuocDto dto = new GuiDonThuocRequest.ThuocDto();
                dto.ma_thuoc    = ct.getMaThuocSnapshot();
                // biet_duoc: BYT yêu cầu không được trống → fallback: tenThuoc > maThuoc > "Không rõ"
                String bietDuoc = ct.getBietDuocSnapshot();
                if (nullOrBlank(bietDuoc)) bietDuoc = ct.getTenThuocSnapshot();
                if (nullOrBlank(bietDuoc)) bietDuoc = ct.getMaThuocSnapshot();
                if (nullOrBlank(bietDuoc)) bietDuoc = "Không rõ";
                dto.biet_duoc   = bietDuoc;
                // ten_thuoc: fallback: maThuoc > "Không rõ"
                String tenThuoc = ct.getTenThuocSnapshot();
                if (nullOrBlank(tenThuoc)) tenThuoc = ct.getMaThuocSnapshot();
                if (nullOrBlank(tenThuoc)) tenThuoc = "Không rõ";
                dto.ten_thuoc   = tenThuoc;
                dto.don_vi_tinh = ct.getDonViTinhSnapshot();
                dto.so_luong    = ct.getSoLuong() == null ? null : ct.getSoLuong().toPlainString();
                dto.cach_dung   = buildCachDung(ct);
                thuocs.add(dto);
            }
        }
        r.thong_tin_don_thuoc = thuocs;

        // === Chẩn đoán (chan_doan) ===
        List<GuiDonThuocRequest.ChanDoanDto> cds = new ArrayList<>();
        if (dt.getChanDoans() != null) {
            for (DonThuocChanDoan cd : dt.getChanDoans()) {
                GuiDonThuocRequest.ChanDoanDto dto = new GuiDonThuocRequest.ChanDoanDto();
                dto.ma_chan_doan  = cd.getMaIcdSnapshot();
                dto.ten_chan_doan = cd.getTenIcdSnapshot();
                dto.ket_luan      = cd.getKetLuan();
                cds.add(dto);
            }
        }
        r.chan_doan = cds;

        // === Đợt dùng thuốc (dot_dung_thuoc) ===
        // FIX 2: BYT spec — 1 đợt trả object, nhiều hơn 1 mới trả list
        r.dot_dung_thuoc = buildDotDungThuoc(dt);

        // === Signature: bỏ qua ở MVP ===
        r.signature = null;

        return r;
    }

    /**
     * GioiTinh đã được lưu là mã API "1"/"2"/"3" (xem snapshotBenhNhan).
     * BYT spec: 1=ChuaXacDinh, 2=Nam, 3=Nu.
     */
    private static String mapGioiTinh(String gt) {
        // gt đã là "1","2","3" — BYT spec trực tiếp, không cần convert
        return gt;
    }

    /**
     * FIX 3: ngay_tai_kham = số ngày tái khám (tính từ ngày kê đơn).
     * Nếu không có ngayTaiKham → null.
     */
    private static String buildNgayTaiKham(DonThuoc dt) {
        if (dt.getNgayKe() == null || dt.getNgayTaiKham() == null) {
            return null;
        }
        java.time.LocalDate ngayKe = dt.getNgayKe()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        java.time.LocalDate taiKham = dt.getNgayTaiKham()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        long days = java.time.temporal.ChronoUnit.DAYS.between(ngayKe, taiKham);
        return days >= 0 ? String.valueOf(days) : null;
    }

    /**
     * BYT yêu cầu dot_dung_thuoc là SINGLE OBJECT:
     *   "dot_dung_thuoc": {"dot":"1","tu_ngay":"...","den_ngay":"...",...}
     * - Bắt buộc với loại h/n/y.
     * - dot/tu_ngay/den_ngay không được trống (BYT validate).
     * - Nếu entity chưa có DonThuocDotDung → tạo default đợt 1 từ ngayKe/ngayTaiKham.
     */
    private static GuiDonThuocRequest.DotDungThuocDto buildDotDungThuoc(DonThuoc dt) {
        GuiDonThuocRequest.DotDungThuocDto dto = new GuiDonThuocRequest.DotDungThuocDto();
        if (dt.getDotDungs() != null && !dt.getDotDungs().isEmpty()) {
            DonThuocDotDung dd = dt.getDotDungs().get(0);
            dto.dot             = dd.getSoDot() == null ? "1" : String.valueOf(dd.getSoDot());
            dto.tu_ngay         = formatDmy(dd.getTuNgay());
            dto.den_ngay        = formatDmy(dd.getDenNgay());
            dto.so_thang_thuoc  = dd.getSoThangThuoc() == null ? null
                    : String.valueOf(dd.getSoThangThuoc());
        } else {
            // Default: đợt 1 từ ngayKe → ngayTaiKham
            dto.dot = "1";
            dto.tu_ngay = formatDmy(dt.getNgayKe());
            dto.den_ngay = formatDmy(dt.getNgayTaiKham());
            dto.so_thang_thuoc = null;
        }
        // Last-resort fallback: BYT bắt buộc dot/tu_ngay/den_ngay không trống
        if (dto.dot == null || dto.dot.isBlank()) dto.dot = "1";
        if (dto.tu_ngay == null) dto.tu_ngay = formatDmy(new java.util.Date()); // hôm nay
        if (dto.den_ngay == null) dto.den_ngay = dto.tu_ngay;
        return dto;
    }

    /** Format d/m/Y. */
    private static String formatDmy(java.util.Date d) {
        if (d == null) return null;
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(NGAY_DMY);
    }

    /** Format Y-m-d H:i:s cho ngay_gio_ke_don. */
    private static String formatYmdHms(java.util.Date d) {
        if (d == null) return null;
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(NGAY_GIO_YMD_HMS);
    }

    private static boolean nullOrBlank(String s) {
        return s == null || s.isBlank();
    }

    /**
     * Build chuỗi thông tin người giám hộ: "Họ tên, SĐT, địa chỉ".
     */
    private static String buildNguoiGiamHo(DonThuoc dt) {
        String ten = dt.getNguoiGiamHoHoTen();
        String sdt = dt.getNguoiGiamHoSoDienThoai();
        String dc  = dt.getNguoiGiamHoDiaChi();
        if (nullOrBlank(ten) && nullOrBlank(sdt) && nullOrBlank(dc)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        appendPart(sb, ten);
        appendPart(sb, sdt);
        appendPart(sb, dc);
        return sb.toString();
    }

    private static void appendPart(StringBuilder sb, String part) {
        if (!nullOrBlank(part)) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(part);
        }
    }

    /**
     * Build chuỗi cách dùng: gộp đường dùng, liều, tần suất, thời gian, cách dùng.
     */
    private static String buildCachDung(DonThuocChiTiet ct) {
        StringBuilder sb = new StringBuilder();
        appendPart(sb, ct.getDuongDung());
        appendPart(sb, ct.getLieuDung());
        appendPart(sb, ct.getTanSuat());
        appendPart(sb, ct.getThoiGianDung());
        appendPart(sb, ct.getCachDung());
        return sb.length() == 0 ? null : sb.toString();
    }
}
