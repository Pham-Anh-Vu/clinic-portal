package com.company.clinicportal.entity;

import com.company.clinicportal.enumentity.HinhThucDieuTri;
import com.company.clinicportal.enumentity.LoaiDon;
import com.company.clinicportal.enumentity.TrangThaiDonThuoc;
import io.jmix.core.DeletePolicy;
import io.jmix.core.annotation.Secret;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Đơn thuốc liên thông theo Quyết định 808/QĐ-BYT.
 *
 * <p>Quy tắc snapshot:
 * <ul>
 *     <li>Mã đơn thuốc, ngày kê, tên bác sĩ/cơ sở được sinh/lưu tại thời điểm kê.</li>
 *     <li>Mọi dòng thuốc (DonThuocChiTiet) snapshot mã/tên/biệt dược/đơn vị từ danh mục.</li>
 *     <li>Chẩn đoán snapshot mã ICD + tên.</li>
 * </ul>
 */
@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "don_thuoc",
       indexes = {
           @Index(name = "IDX_DON_THUOC_MA", columnList = "ma_don_thuoc", unique = true),
           @Index(name = "IDX_DON_THUOC_BN", columnList = "id_benh_nhan"),
           @Index(name = "IDX_DON_THUOC_TRANG_THAI", columnList = "trang_thai"),
           @Index(name = "IDX_DON_THUOC_NGAY_KE", columnList = "ngay_ke"),
           @Index(name = "IDX_DON_THUOC_IDEMP", columnList = "idempotency_key", unique = true)
       })
@Entity
public class DonThuoc {

    @Id
    @Column(name = "id", nullable = false)
    @JmixGeneratedValue
    private UUID id;

    @Column(name = "ma_don_thuoc", nullable = false, length = 32)
    private String maDonThuoc;

    /** Idempotency key khi gửi API: tránh tạo trùng đơn khi timeout sau khi server đã nhận. */
    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    /** Cơ sở liên thông (snapshot mã liên thông). */
    @Column(name = "ma_co_so_kcb", length = 16)
    private String maCoSoKcb;

    /** Mã liên thông bác sĩ snapshot tại thời điểm kê. */
    @Column(name = "ma_lien_thong_bac_si", length = 64)
    private String maLienThongBacSi;

    @Column(name = "ten_bac_si", length = 255)
    private String tenBacSi;

    @Column(name = "so_cchn_bac_si", length = 32)
    private String soCchnBacSi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_benh_nhan")
    private BenhNhan benhNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieu_dieu_tri")
    private PhieuDieuTri phieuDieuTri;

    /**
     * Liên kết trực tiếp tới chi tiết phiếu điều trị (n-1).
     * Một ChiTietDieuTri có thể có nhiều đơn thuốc theo các lần tái khám.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chi_tiet_dieu_tri")
    private ChiTietDieuTri chiTietDieuTri;

    @Column(name = "ho_va_ten_benh_nhan", length = 255)
    private String hoVaTenBenhNhan;

    @Column(name = "ma_dinh_danh_y_te", length = 64)
    private String maDinhDanhYTe;

    @Column(name = "ma_dinh_danh_cong_dan", length = 32)
    private String maDinhDanhCongDan;

    @Column(name = "so_dien_thoai", length = 32)
    private String soDienThoai;

    @Column(name = "ngay_sinh")
    @Temporal(TemporalType.DATE)
    private Date ngaySinh;

    @Column(name = "gioi_tinh", length = 16)
    private String gioiTinh;

    @Column(name = "dia_chi", length = 1024)
    private String diaChi;

    @Column(name = "can_nang")
    private Double canNang;

    @Column(name = "so_thang_tuoi")
    private Integer soThangTuoi;

    @Column(name = "nguoi_giam_ho_ho_ten", length = 255)
    private String nguoiGiamHoHoTen;

    @Column(name = "nguoi_giam_ho_quan_he", length = 64)
    private String nguoiGiamHoQuanHe;

    @Column(name = "nguoi_giam_ho_so_dien_thoai", length = 32)
    private String nguoiGiamHoSoDienThoai;

    @Column(name = "nguoi_giam_ho_dia_chi", length = 512)
    private String nguoiGiamHoDiaChi;

    @Column(name = "loai_don", length = 32)
    private String loaiDon;

    @Column(name = "hinh_thuc_dieu_tri", length = 16)
    private String hinhThucDieuTri;

    @Column(name = "so_vao_vien", length = 64)
    private String soVaoVien;

    @Column(name = "chan_doan_text", length = 4000)
    private String chanDoanText;

    @Column(name = "ly_do_kham", length = 1024)
    private String lyDoKham;

    @Column(name = "ten_don_vi", length = 255)
    private String tenDonVi;

    @Column(name = "chieu_cao")
    private Double chieuCao;

    @Column(name = "ma_quoc_tich", length = 16)
    private String maQuocTich;

    @Column(name = "nghe_nghiep", length = 128)
    private String ngheNghiep;

    @Column(name = "chu_ky_bac_si", length = 4000)
    @Lob
    private String chuKyBacSi;

    @Column(name = "chu_ky_benh_nhan", length = 4000)
    @Lob
    private String chuKyBenhNhan;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_ke")
    private Date ngayKe;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_tai_kham")
    private Date ngayTaiKham;

    @Column(name = "loi_dan", length = 4000)
    @Lob
    private String loiDan;

    @Column(name = "trang_thai", length = 32)
    private String trangThai;

    @Column(name = "so_luong_thuoc_trong_don")
    private Integer soLuongThuocTrongDon;

    @Column(name = "so_dot_dung")
    private Integer soDotDung;

    /** Signature ký trên payload - skeleton; MVP có thể để trống/placeholder. */
    @Secret
    @SystemLevel
    @Column(name = "signature", length = 2048)
    private String signature;

    /** Response từ API khi gửi thành công (mask). */
    @Column(name = "api_response", length = 4000)
    private String apiResponse;

    @Column(name = "api_request_id", length = 64)
    private String apiRequestId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sent_at")
    private Date sentAt;

    @Column(name = "last_error", length = 1024)
    private String lastError;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Composition
    @OneToMany(mappedBy = "donThuoc", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stt ASC")
    private List<DonThuocChiTiet> chiTiets = new ArrayList<>();

    @Composition
    @OneToMany(mappedBy = "donThuoc", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stt ASC")
    private List<DonThuocChanDoan> chanDoans = new ArrayList<>();

    @Composition
    @OneToMany(mappedBy = "donThuoc", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("soDot ASC")
    private List<DonThuocDotDung> dotDungs = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdat\"")
    private Date createdAt;

    @Column(name = "\"createdbyid\"")
    private Long createdById;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"updatedat\"")
    private Date updatedAt;

    @Column(name = "\"updatedbyid\"")
    private Long updatedById;

    @Version
    @Column(name = "version")
    private Integer version;

    @InstanceName
    @DependsOnProperties({"maDonThuoc"})
    public String getInstanceName() {
        return maDonThuoc == null ? "" : maDonThuoc;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) { this.maDonThuoc = maDonThuoc; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getMaCoSoKcb() { return maCoSoKcb; }
    public void setMaCoSoKcb(String maCoSoKcb) { this.maCoSoKcb = maCoSoKcb; }
    public String getMaLienThongBacSi() { return maLienThongBacSi; }
    public void setMaLienThongBacSi(String maLienThongBacSi) { this.maLienThongBacSi = maLienThongBacSi; }
    public String getTenBacSi() { return tenBacSi; }
    public void setTenBacSi(String tenBacSi) { this.tenBacSi = tenBacSi; }
    public String getSoCchnBacSi() { return soCchnBacSi; }
    public void setSoCchnBacSi(String soCchnBacSi) { this.soCchnBacSi = soCchnBacSi; }
    public BenhNhan getBenhNhan() { return benhNhan; }
    public void setBenhNhan(BenhNhan benhNhan) { this.benhNhan = benhNhan; }
    public PhieuDieuTri getPhieuDieuTri() { return phieuDieuTri; }
    public void setPhieuDieuTri(PhieuDieuTri phieuDieuTri) { this.phieuDieuTri = phieuDieuTri; }

    public ChiTietDieuTri getChiTietDieuTri() { return chiTietDieuTri; }
    public void setChiTietDieuTri(ChiTietDieuTri chiTietDieuTri) { this.chiTietDieuTri = chiTietDieuTri; }
    public String getHoVaTenBenhNhan() { return hoVaTenBenhNhan; }
    public void setHoVaTenBenhNhan(String hoVaTenBenhNhan) { this.hoVaTenBenhNhan = hoVaTenBenhNhan; }
    public String getMaDinhDanhYTe() { return maDinhDanhYTe; }
    public void setMaDinhDanhYTe(String maDinhDanhYTe) { this.maDinhDanhYTe = maDinhDanhYTe; }
    public String getMaDinhDanhCongDan() { return maDinhDanhCongDan; }
    public void setMaDinhDanhCongDan(String maDinhDanhCongDan) { this.maDinhDanhCongDan = maDinhDanhCongDan; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public Date getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(Date ngaySinh) { this.ngaySinh = ngaySinh; }
    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public Double getCanNang() { return canNang; }
    public void setCanNang(Double canNang) { this.canNang = canNang; }
    public Integer getSoThangTuoi() { return soThangTuoi; }
    public void setSoThangTuoi(Integer soThangTuoi) { this.soThangTuoi = soThangTuoi; }
    public String getNguoiGiamHoHoTen() { return nguoiGiamHoHoTen; }
    public void setNguoiGiamHoHoTen(String nguoiGiamHoHoTen) { this.nguoiGiamHoHoTen = nguoiGiamHoHoTen; }
    public String getNguoiGiamHoQuanHe() { return nguoiGiamHoQuanHe; }
    public void setNguoiGiamHoQuanHe(String nguoiGiamHoQuanHe) { this.nguoiGiamHoQuanHe = nguoiGiamHoQuanHe; }
    public String getNguoiGiamHoSoDienThoai() { return nguoiGiamHoSoDienThoai; }
    public void setNguoiGiamHoSoDienThoai(String nguoiGiamHoSoDienThoai) { this.nguoiGiamHoSoDienThoai = nguoiGiamHoSoDienThoai; }
    public String getNguoiGiamHoDiaChi() { return nguoiGiamHoDiaChi; }
    public void setNguoiGiamHoDiaChi(String nguoiGiamHoDiaChi) { this.nguoiGiamHoDiaChi = nguoiGiamHoDiaChi; }
    public String getChanDoanText() { return chanDoanText; }
    public void setChanDoanText(String chanDoanText) { this.chanDoanText = chanDoanText; }
    public String getLyDoKham() { return lyDoKham; }
    public void setLyDoKham(String lyDoKham) { this.lyDoKham = lyDoKham; }
    public String getSoVaoVien() { return soVaoVien; }
    public void setSoVaoVien(String soVaoVien) { this.soVaoVien = soVaoVien; }
    public String getTenDonVi() { return tenDonVi; }
    public void setTenDonVi(String tenDonVi) { this.tenDonVi = tenDonVi; }
    public Double getChieuCao() { return chieuCao; }
    public void setChieuCao(Double chieuCao) { this.chieuCao = chieuCao; }
    public String getMaQuocTich() { return maQuocTich; }
    public void setMaQuocTich(String maQuocTich) { this.maQuocTich = maQuocTich; }
    public String getNgheNghiep() { return ngheNghiep; }
    public void setNgheNghiep(String ngheNghiep) { this.ngheNghiep = ngheNghiep; }
    public String getChuKyBacSi() { return chuKyBacSi; }
    public void setChuKyBacSi(String chuKyBacSi) { this.chuKyBacSi = chuKyBacSi; }
    public String getChuKyBenhNhan() { return chuKyBenhNhan; }
    public void setChuKyBenhNhan(String chuKyBenhNhan) { this.chuKyBenhNhan = chuKyBenhNhan; }

    public LoaiDon getLoaiDon() { return LoaiDon.fromId(loaiDon); }
    public void setLoaiDon(LoaiDon v) { this.loaiDon = v == null ? null : v.getId(); }

    public HinhThucDieuTri getHinhThucDieuTri() { return HinhThucDieuTri.fromId(hinhThucDieuTri); }
    public void setHinhThucDieuTri(HinhThucDieuTri v) { this.hinhThucDieuTri = v == null ? null : v.getId(); }

    public Date getNgayKe() { return ngayKe; }
    public void setNgayKe(Date ngayKe) { this.ngayKe = ngayKe; }
    public Date getNgayTaiKham() { return ngayTaiKham; }
    public void setNgayTaiKham(Date ngayTaiKham) { this.ngayTaiKham = ngayTaiKham; }
    public String getLoiDan() { return loiDan; }
    public void setLoiDan(String loiDan) { this.loiDan = loiDan; }

    public TrangThaiDonThuoc getTrangThaiEnum() { return TrangThaiDonThuoc.fromId(trangThai); }
    public void setTrangThaiEnum(TrangThaiDonThuoc v) { this.trangThai = v == null ? null : v.getId(); }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Integer getSoLuongThuocTrongDon() { return soLuongThuocTrongDon; }
    public void setSoLuongThuocTrongDon(Integer soLuongThuocTrongDon) { this.soLuongThuocTrongDon = soLuongThuocTrongDon; }
    public Integer getSoDotDung() { return soDotDung; }
    public void setSoDotDung(Integer soDotDung) { this.soDotDung = soDotDung; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    public String getApiResponse() { return apiResponse; }
    public void setApiResponse(String apiResponse) { this.apiResponse = apiResponse; }
    public String getApiRequestId() { return apiRequestId; }
    public void setApiRequestId(String apiRequestId) { this.apiRequestId = apiRequestId; }
    public Date getSentAt() { return sentAt; }
    public void setSentAt(Date sentAt) { this.sentAt = sentAt; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    /** Số lần đã gửi lên liên thông (để thống kê, không reset khi retry). */
    @Column(name = "lan_gui_lien_thong")
    private Integer lanGuiLienThong;

    @Column(name = "ma_don_thuoc_qg", length = 64)
    private String maDonThuocQg;

    @Column(name = "don_thuoc_id_qg", length = 64)
    private String donThuocIdQg;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lan_gui_cuoi_at")
    private Date lanGuiCuoiAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngay_dong_bo_cuoi_at")
    private Date ngayDongBoCuoiAt;

    @Column(name = "phan_hoi_cuoi", length = 4000)
    private String phanHoiCuoi;

    public Integer getLanGuiLienThong() { return lanGuiLienThong; }
    public void setLanGuiLienThong(Integer lanGuiLienThong) { this.lanGuiLienThong = lanGuiLienThong; }
    public String getMaDonThuocQg() { return maDonThuocQg; }
    public void setMaDonThuocQg(String maDonThuocQg) { this.maDonThuocQg = maDonThuocQg; }
    public String getDonThuocIdQg() { return donThuocIdQg; }
    public void setDonThuocIdQg(String donThuocIdQg) { this.donThuocIdQg = donThuocIdQg; }
    public Date getLanGuiCuoiAt() { return lanGuiCuoiAt; }
    public void setLanGuiCuoiAt(Date lanGuiCuoiAt) { this.lanGuiCuoiAt = lanGuiCuoiAt; }
    public Date getNgayDongBoCuoiAt() { return ngayDongBoCuoiAt; }
    public void setNgayDongBoCuoiAt(Date ngayDongBoCuoiAt) { this.ngayDongBoCuoiAt = ngayDongBoCuoiAt; }
    public String getPhanHoiCuoi() { return phanHoiCuoi; }
    public void setPhanHoiCuoi(String phanHoiCuoi) { this.phanHoiCuoi = phanHoiCuoi; }

    public List<DonThuocChiTiet> getChiTiets() { return chiTiets; }
    public void setChiTiets(List<DonThuocChiTiet> chiTiets) { this.chiTiets = chiTiets; }
    public List<DonThuocChanDoan> getChanDoans() { return chanDoans; }
    public void setChanDoans(List<DonThuocChanDoan> chanDoans) { this.chanDoans = chanDoans; }
    public List<DonThuocDotDung> getDotDungs() { return dotDungs; }
    public void setDotDungs(List<DonThuocDotDung> dotDungs) { this.dotDungs = dotDungs; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public Long getUpdatedById() { return updatedById; }
    public void setUpdatedById(Long updatedById) { this.updatedById = updatedById; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @PrePersist
    void onPrePersist() {
        Date now = new Date();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (createdById == null) createdById = 0L;
        if (updatedById == null) updatedById = createdById;
        if (trangThai == null) trangThai = TrangThaiDonThuoc.NHAP.getId();
        if (soLuongThuocTrongDon == null) soLuongThuocTrongDon = chiTiets == null ? 0 : chiTiets.size();
    }

    @PreUpdate
    void onPreUpdate() {
        updatedAt = new Date();
        if (updatedById == null) updatedById = createdById != null ? createdById : 0L;
    }
}
