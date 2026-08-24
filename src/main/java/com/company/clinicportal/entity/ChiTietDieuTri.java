package com.company.clinicportal.entity;

import io.jmix.core.FileRef;
import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "chi_tiet_dieu_tri", indexes = {
        @Index(name = "IDX_CHI_TIET_DIEU_TRI_ID_PHIEU_DIEU_TRI", columnList = "ID_PHIEU_DIEU_TRI")
})
@Entity
public class ChiTietDieuTri {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Composition
    @OneToMany(mappedBy = "idChiTietPhieuDieuTri")
    private List<ChiTietDichVu> chiTietDichVu;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "chiTietDieuTri", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietDieuTriFileDinhKem> fileDinhKem;

    @Composition
    @OneToMany(mappedBy = "chiTietDieuTri")
    private List<ToDieuTri> toDieuTri;

    /**
     * Danh sách chuẩn đoán ICD-10 gắn với chi tiết điều trị (cho phép chọn nhiều).
     * Quan hệ ManyToMany qua bảng trung gian tự động sinh bởi JPA (chi_tiet_dieu_tri_icd_link).
     */
    @JoinTable(name = "chi_tiet_dieu_tri_icd_link",
            joinColumns = @JoinColumn(name = "ID_CHI_TIET_DIEU_TRI"),
            inverseJoinColumns = @JoinColumn(name = "ID_ICD10"))
    @ManyToMany
    private Set<Icd10> dsChanDoanIcd = new LinkedHashSet<>();

    @Column(name = "chuan_doan")
    @Lob
    private String chuanDoan;

    @Column(name = "chuan_doan_ra_vien")
    private String chuanDoanRaVien;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @Column(name = "da_thanh_toan")
    private Long daThanhToan;

    @Column(name = "da_xu_ly")
    private String daXuLy;

    @Column(name = "link_gg_drive", length = 2048)
    private String linkGgDrive;

    @Column(name = "dien_bien_benh")
    @Lob
    private String dienBienBenh;

    @Column(name = "file_so_da_ky", length = 1024)
    private FileRef fileSoDaKy;

    @Column(name = "ten_file_so_da_ky")
    private String tenFileSoDaKy;

    @Column(name = "giam_truc_tiep")
    private Long giamTrucTiep;

    @Temporal(TemporalType.TIME)
    @Column(name = "gio_hen_mac_dinh")
    private Date gioHenMacDinh;

    @Column(name = "huong_dieu_tri")
    private String huongDieuTri;

    @JoinColumn(name = "ID_BENH_NHAN")
    @ManyToOne(fetch = FetchType.LAZY)
    private BenhNhan idBenhNhan;

    @JoinColumn(name = "ID_NHAN_SU")
    @ManyToOne(fetch = FetchType.LAZY)
    private NhanSu idNhanSu;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PHIEU_DIEU_TRI")
    private PhieuDieuTri idPhieuDieuTri;

    @Column(name = "kb_bo_phan")
    @Lob
    private String kbBoPhan;

    @Column(name = "ket_qua_can_lam_sang")
    @Lob
    private String ketQuaCanLamSang;

    @Column(name = "ket_qua_xet_nghiem")
    @Lob
    private String ketQuaXetNghiem;

    @Column(name = "kham_benh_qua_trinh_benh")
    @Lob
    private String khamBenhQuaTrinhBenh;

    @Column(name = "kham_benh_tien_su_benh")
    @Lob
    private String khamBenhTienSuBenh;

    @Column(name = "kham_benh_toan_than")
    private String khamBenhToanThan;

    @Column(name = "mach")
    private Double mach;

    @Column(name = "nhiet_do")
    private Double nhietDo;

    @Column(name = "huyet_ap", length = 16)
    private String huyetAp;

    @Column(name = "nhip_tho")
    private Double nhipTho;

    @Column(name = "can_nang")
    private Double canNang;

    @Column(name = "chieu_cao")
    private Double chieuCao;

    @Transient
    @DependsOnProperties({"canNang", "chieuCao"})
    public Double getBmi() {
        if (canNang == null || chieuCao == null || chieuCao <= 0) {
            return null;
        }
        double chieuCaoMet = chieuCao / 100.0;
        double bmi = canNang / (chieuCaoMet * chieuCaoMet);
        return Math.round(bmi * 100.0) / 100.0;
    }

    @Column(name = "khuyen_mai")
    private Double khuyenMai;

    @Column(name = "ly_do_vao_vien")
    private String lyDoVaoVien;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_bat_dau")
    private Date ngayBatDau;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngay_chi_dinh")
    private Date ngayChiDinh;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_ket_thuc")
    private Date ngayKetThuc;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngay_thanh_toan")
    private Date ngayThanhToan;

    @Column(name = "phai_dong")
    private Integer phaiDong;

    @Column(name = "ten_phieu_dieu_tri")
    private String tenPhieuDieuTri;

    @Column(name = "tinh_kpi")
    private Long tinhKpi;

    @Column(name = "tinh_trang_benh_nhan")
    private String tinhTrangBenhNhan;

    @Column(name = "tinh_trang_bn")
    private String tinhTrangBn;

    @Column(name = "tong_tien")
    private Long tongTien;

    @Column(name = "tong_tien_sau_khuyen_mai")
    private Long tongTienSauKhuyenMai;

    @Column(name = "trieu_chung")
    @Lob
    private String trieuChung;

    @Column(name = "trong_so_vat_ly_tri_lieu")
    private Double trongSoVatLyTriLieu;

    @Column(name = "trong_so_dien_tri_lieu")
    private Double trongSoDienTriLieu;

    @Column(name = "trong_so_keo_nan_tri_lieu")
    private Double trongSoKeoNanTriLieu;

    @Column(name = "trong_so_keo_gian")
    private Double trongSoKeoGian;

    @Column(name = "trong_so_kpi")
    private Double trongSoKpi;

    @Column(name = "trong_so_xoa_bop_tri_lieu")
    private Double trongSoXoaBopTriLieu;

    @Column(name = "trong_so_tap_phcn")
    private Double trongSoTapPhcn;

    @Column(name = "trong_so_van_dong_tri_lieu")
    private Double trongSoVanDongTriLieu;

    @Column(name = "trong_so_kham_luong_gia")
    private Double trongSoKhamLuongGia;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"updatedAt\"")
    private Date updatedAt;

    @Column(name = "\"updatedById\"")
    private Long updatedById;

    public List<ChiTietDichVu> getChiTietDichVu() {
        return chiTietDichVu;
    }

    public void setChiTietDichVu(List<ChiTietDichVu> chiTietDichVu) {
        this.chiTietDichVu = chiTietDichVu;
    }

    public List<ChiTietDieuTriFileDinhKem> getFileDinhKem() {
        return fileDinhKem;
    }

    public void setFileDinhKem(List<ChiTietDieuTriFileDinhKem> fileDinhKem) {
        this.fileDinhKem = fileDinhKem;
    }

    public List<ToDieuTri> getToDieuTri() {
        return toDieuTri;
    }

    public void setToDieuTri(List<ToDieuTri> toDieuTri) {
        this.toDieuTri = toDieuTri;
    }

    public Set<Icd10> getDsChanDoanIcd() {
        return dsChanDoanIcd;
    }

    public void setDsChanDoanIcd(Set<Icd10> dsChanDoanIcd) {
        this.dsChanDoanIcd = dsChanDoanIcd;
    }

    /**
     * Helper: chuỗi "Mã - Tên" ghép cho từng ICD, ngăn cách bằng ", ".
     * Sử dụng khi in báo cáo / hiển thị text, nếu không có ICD thì trả về chuỗi rỗng.
     */
    public String getDsChanDoanIcdText() {
        if (dsChanDoanIcd == null || dsChanDoanIcd.isEmpty()) {
            return "";
        }
        return dsChanDoanIcd.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(icd -> icd.getMaIcd() == null ? "" : icd.getMaIcd()))
                .map(icd -> {
                    String ma = icd.getMaIcd();
                    String ten = icd.getTenBenh();
                    if (ma != null && !ma.isBlank() && ten != null && !ten.isBlank()) {
                        return ma + " - " + ten;
                    }
                    if (ma != null && !ma.isBlank()) {
                        return ma;
                    }
                    return ten != null ? ten : "";
                })
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    public void setIdNhanSu(NhanSu idNhanSu) {
        this.idNhanSu = idNhanSu;
    }

    public NhanSu getIdNhanSu() {
        return idNhanSu;
    }

    public void setIdPhieuDieuTri(PhieuDieuTri idPhieuDieuTri) {
        this.idPhieuDieuTri = idPhieuDieuTri;
    }

    public PhieuDieuTri getIdPhieuDieuTri() {
        return idPhieuDieuTri;
    }

    public void setIdBenhNhan(BenhNhan idBenhNhan) {
        this.idBenhNhan = idBenhNhan;
    }

    public BenhNhan getIdBenhNhan() {
        return idBenhNhan;
    }

    public Long getUpdatedById() {
        return updatedById;
    }

    public void setUpdatedById(Long updatedById) {
        this.updatedById = updatedById;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getTrongSoVanDongTriLieu() {
        return trongSoVanDongTriLieu;
    }

    public void setTrongSoVanDongTriLieu(Double trongSoVanDongTriLieu) {
        this.trongSoVanDongTriLieu = trongSoVanDongTriLieu;
    }

    public Double getTrongSoKhamLuongGia() {
        return trongSoKhamLuongGia;
    }

    public void setTrongSoKhamLuongGia(Double trongSoKhamLuongGia) {
        this.trongSoKhamLuongGia = trongSoKhamLuongGia;
    }

    public Double getTrongSoTapPhcn() {
        return trongSoTapPhcn;
    }

    public void setTrongSoTapPhcn(Double trongSoTapPhcn) {
        this.trongSoTapPhcn = trongSoTapPhcn;
    }

    public Double getTrongSoKpi() {
        return trongSoKpi;
    }

    public void setTrongSoKpi(Double trongSoKpi) {
        this.trongSoKpi = trongSoKpi;
    }

    public Double getTrongSoXoaBopTriLieu() {
        return trongSoXoaBopTriLieu;
    }

    public void setTrongSoXoaBopTriLieu(Double trongSoXoaBopTriLieu) {
        this.trongSoXoaBopTriLieu = trongSoXoaBopTriLieu;
    }

    public Double getTrongSoKeoNanTriLieu() {
        return trongSoKeoNanTriLieu;
    }

    public void setTrongSoKeoNanTriLieu(Double trongSoKeoNanTriLieu) {
        this.trongSoKeoNanTriLieu = trongSoKeoNanTriLieu;
    }

    public Double getTrongSoKeoGian() {
        return trongSoKeoGian;
    }

    public void setTrongSoKeoGian(Double trongSoKeoGian) {
        this.trongSoKeoGian = trongSoKeoGian;
    }

    public Double getTrongSoVatLyTriLieu() {
        return trongSoVatLyTriLieu;
    }

    public void setTrongSoVatLyTriLieu(Double trongSoVatLyTriLieu) {
        this.trongSoVatLyTriLieu = trongSoVatLyTriLieu;
    }

    public Double getTrongSoDienTriLieu() {
        return trongSoDienTriLieu;
    }

    public void setTrongSoDienTriLieu(Double trongSoDienTriLieu) {
        this.trongSoDienTriLieu = trongSoDienTriLieu;
    }

    public String getTrieuChung() {
        return trieuChung;
    }

    public void setTrieuChung(String trieuChung) {
        this.trieuChung = trieuChung;
    }

    public Long getTongTienSauKhuyenMai() {
        return tongTienSauKhuyenMai;
    }

    public void setTongTienSauKhuyenMai(Long tongTienSauKhuyenMai) {
        this.tongTienSauKhuyenMai = tongTienSauKhuyenMai;
    }

    public Long getTongTien() {
        return tongTien;
    }

    public void setTongTien(Long tongTien) {
        this.tongTien = tongTien;
    }

    public String getTinhTrangBn() {
        return tinhTrangBn;
    }

    public void setTinhTrangBn(String tinhTrangBn) {
        this.tinhTrangBn = tinhTrangBn;
    }

    public String getTinhTrangBenhNhan() {
        return tinhTrangBenhNhan;
    }

    public void setTinhTrangBenhNhan(String tinhTrangBenhNhan) {
        this.tinhTrangBenhNhan = tinhTrangBenhNhan;
    }

    public Long getTinhKpi() {
        return tinhKpi;
    }

    public void setTinhKpi(Long tinhKpi) {
        this.tinhKpi = tinhKpi;
    }

    public String getTenPhieuDieuTri() {
        return tenPhieuDieuTri;
    }

    public void setTenPhieuDieuTri(String tenPhieuDieuTri) {
        this.tenPhieuDieuTri = tenPhieuDieuTri;
    }

    public Integer getPhaiDong() {
        return phaiDong;
    }

    public void setPhaiDong(Integer phaiDong) {
        this.phaiDong = phaiDong;
    }

    public Date getNgayThanhToan() {
        return ngayThanhToan;
    }

    public void setNgayThanhToan(Date ngayThanhToan) {
        this.ngayThanhToan = ngayThanhToan;
    }

    public Date getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(Date ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public Date getNgayChiDinh() {
        return ngayChiDinh;
    }

    public void setNgayChiDinh(Date ngayChiDinh) {
        this.ngayChiDinh = ngayChiDinh;
    }

    public Date getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(Date ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public String getLyDoVaoVien() {
        return lyDoVaoVien;
    }

    public void setLyDoVaoVien(String lyDoVaoVien) {
        this.lyDoVaoVien = lyDoVaoVien;
    }

    public Double getKhuyenMai() {
        return khuyenMai;
    }

    public void setKhuyenMai(Double khuyenMai) {
        this.khuyenMai = khuyenMai;
    }

    public String getKhamBenhToanThan() {
        return khamBenhToanThan;
    }

    public void setKhamBenhToanThan(String khamBenhToanThan) {
        this.khamBenhToanThan = khamBenhToanThan;
    }

    public String getKhamBenhTienSuBenh() {
        return khamBenhTienSuBenh;
    }

    public void setKhamBenhTienSuBenh(String khamBenhTienSuBenh) {
        this.khamBenhTienSuBenh = khamBenhTienSuBenh;
    }

    public String getKhamBenhQuaTrinhBenh() {
        return khamBenhQuaTrinhBenh;
    }

    public void setKhamBenhQuaTrinhBenh(String khamBenhQuaTrinhBenh) {
        this.khamBenhQuaTrinhBenh = khamBenhQuaTrinhBenh;
    }

    public String getKetQuaXetNghiem() {
        return ketQuaXetNghiem;
    }

    public void setKetQuaXetNghiem(String ketQuaXetNghiem) {
        this.ketQuaXetNghiem = ketQuaXetNghiem;
    }

    public String getKetQuaCanLamSang() {
        return ketQuaCanLamSang;
    }

    public void setKetQuaCanLamSang(String ketQuaCanLamSang) {
        this.ketQuaCanLamSang = ketQuaCanLamSang;
    }

    public String getKbBoPhan() {
        return kbBoPhan;
    }

    public void setKbBoPhan(String kbBoPhan) {
        this.kbBoPhan = kbBoPhan;
    }

    public String getHuongDieuTri() {
        return huongDieuTri;
    }

    public void setHuongDieuTri(String huongDieuTri) {
        this.huongDieuTri = huongDieuTri;
    }

    public Date getGioHenMacDinh() {
        return gioHenMacDinh;
    }

    public void setGioHenMacDinh(Date gioHenMacDinh) {
        this.gioHenMacDinh = gioHenMacDinh;
    }

    public Long getGiamTrucTiep() {
        return giamTrucTiep;
    }

    public void setGiamTrucTiep(Long giamTrucTiep) {
        this.giamTrucTiep = giamTrucTiep;
    }

    public String getDienBienBenh() {
        return dienBienBenh;
    }

    public void setDienBienBenh(String dienBienBenh) {
        this.dienBienBenh = dienBienBenh;
    }

    public FileRef getFileSoDaKy() {
        return fileSoDaKy;
    }

    public void setFileSoDaKy(FileRef fileSoDaKy) {
        this.fileSoDaKy = fileSoDaKy;
    }

    public String getTenFileSoDaKy() {
        return tenFileSoDaKy;
    }

    public void setTenFileSoDaKy(String tenFileSoDaKy) {
        this.tenFileSoDaKy = tenFileSoDaKy;
    }

    public String getDaXuLy() {
        return daXuLy;
    }

    public void setDaXuLy(String daXuLy) {
        this.daXuLy = daXuLy;
    }

    public String getLinkGgDrive() {
        return linkGgDrive;
    }

    public void setLinkGgDrive(String linkGgDrive) {
        this.linkGgDrive = linkGgDrive;
    }

    public Double getMach() {
        return mach;
    }

    public void setMach(Double mach) {
        this.mach = mach;
    }

    public Double getNhietDo() {
        return nhietDo;
    }

    public void setNhietDo(Double nhietDo) {
        this.nhietDo = nhietDo;
    }

    public String getHuyetAp() {
        return huyetAp;
    }

    public void setHuyetAp(String huyetAp) {
        this.huyetAp = huyetAp;
    }

    public Double getNhipTho() {
        return nhipTho;
    }

    public void setNhipTho(Double nhipTho) {
        this.nhipTho = nhipTho;
    }

    public Double getCanNang() {
        return canNang;
    }

    public void setCanNang(Double canNang) {
        this.canNang = canNang;
    }

    public Double getChieuCao() {
        return chieuCao;
    }

    public void setChieuCao(Double chieuCao) {
        this.chieuCao = chieuCao;
    }

    public Long getDaThanhToan() {
        return daThanhToan;
    }

    public void setDaThanhToan(Long daThanhToan) {
        this.daThanhToan = daThanhToan;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getChuanDoanRaVien() {
        return chuanDoanRaVien;
    }

    public void setChuanDoanRaVien(String chuanDoanRaVien) {
        this.chuanDoanRaVien = chuanDoanRaVien;
    }

    public String getChuanDoan() {
        return chuanDoan;
    }

    public void setChuanDoan(String chuanDoan) {
        this.chuanDoan = chuanDoan;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @PrePersist
    private void onCreateAuditFields() {
        Date now = new Date();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (createdById == null) {
            createdById = 0L;
        }
        if (updatedById == null) {
            updatedById = createdById;
        }
    }

    @PreUpdate
    private void onUpdateAuditFields() {
        updatedAt = new Date();
        if (updatedById == null) {
            updatedById = createdById != null ? createdById : 0L;
        }
    }
}
