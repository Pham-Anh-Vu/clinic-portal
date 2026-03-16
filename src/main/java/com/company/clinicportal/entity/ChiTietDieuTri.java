package com.company.clinicportal.entity;

import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

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

    @Column(name = "dien_bien_benh")
    @Lob
    private String dienBienBenh;

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

    @Column(name = "trong_so_keo_nan_tri_lieu")
    private Double trongSoKeoNanTriLieu;

    @Column(name = "trong_so_kpi")
    private Double trongSoKpi;

    @Column(name = "trong_so_xoa_bop_tri_lieu")
    private Double trongSoXoaBopTriLieu;

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

    public Double getTrongSoVatLyTriLieu() {
        return trongSoVatLyTriLieu;
    }

    public void setTrongSoVatLyTriLieu(Double trongSoVatLyTriLieu) {
        this.trongSoVatLyTriLieu = trongSoVatLyTriLieu;
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

    public String getDaXuLy() {
        return daXuLy;
    }

    public void setDaXuLy(String daXuLy) {
        this.daXuLy = daXuLy;
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