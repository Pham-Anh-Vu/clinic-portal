package com.company.clinicportal.entity;

import com.company.clinicportal.enumentity.PhanLoaiThuoc;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;

/**
 * Danh mục thuốc cho kê đơn và liên thông 808/QĐ-BYT.
 * Tên/biệt dược/đơn vị tính sẽ được snapshot trên dòng đơn thuốc khi phát hành
 * để lịch sử không đổi khi danh mục cập nhật.
 */
@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "dm_thuoc",
       indexes = {
           @Index(name = "IDX_DM_THUOC_MA", columnList = "ma_thuoc", unique = true),
           @Index(name = "IDX_DM_THUOC_HOAT_CHAT", columnList = "hoat_chat"),
           @Index(name = "IDX_DM_THUOC_NHOM", columnList = "nhom_thuoc")
       })
@Entity
public class DmThuoc {

    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_thuoc", nullable = false, length = 64)
    private String maThuoc;

    @Column(name = "ten_thuoc", nullable = false, length = 512)
    private String tenThuoc;

    @Column(name = "biet_duoc", length = 512)
    private String bietDuoc;

    /**
     * Phân loại danh mục (Thuốc / Mỹ phẩm / TPCN / VTYT).
     * Lưu trực tiếp {@code id} của enum {@link PhanLoaiThuoc} dưới dạng VARCHAR.
     */
    @Column(name = "phan_loai", length = 32)
    private String phanLoai;

    @Column(name = "nhom_thuoc", length = 128)
    private String nhomThuoc;

    @Column(name = "don_vi_tinh", length = 32)
    private String donViTinh;

    @Column(name = "dang_bao_che", length = 128)
    private String dangBaoChe;

    @Column(name = "sdk_sgbh", length = 64)
    private String sdkSgbh;

    @Column(name = "nuoc_san_xuat", length = 128)
    private String nuocSanXuat;

    @Column(name = "hang_san_xuat", length = 256)
    private String hangSanXuat;

    @Column(name = "so_dang_ky", length = 64)
    private String soDangKy;

    @Column(name = "la_gay_nghien")
    private Boolean laGayNghien = Boolean.FALSE;

    @Column(name = "la_huong_than")
    private Boolean laHuongThan = Boolean.FALSE;

    @Column(name = "la_thuoc_yhct")
    private Boolean laThuocYhct = Boolean.FALSE;

    @Column(name = "la_thuoc_ke_don")
    private Boolean laThuocKeDon = Boolean.FALSE;

    @Column(name = "gia_bhyt")
    private Long giaBhyt;

    @Column(name = "gia_thi_truong")
    private Long giaThiTruong;

    @Column(name = "ghi_chu", length = 1024)
    private String ghiChu;

    @Column(name = "phien_ban")
    private Integer phienBan;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "imported_at")
    private Date importedAt;

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

    @InstanceName
    @DependsOnProperties({"maThuoc", "tenThuoc"})
    public String getInstanceName() {
        return (maThuoc == null ? "" : maThuoc) + " - " + (tenThuoc == null ? "" : tenThuoc);
    }

    @PrePersist
    void onPrePersist() {
        Date now = new Date();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (createdById == null) createdById = 0L;
        if (updatedById == null) updatedById = createdById;
        if (phienBan == null) phienBan = 1;
    }

    @PreUpdate
    void onPreUpdate() {
        updatedAt = new Date();
        if (updatedById == null) updatedById = createdById != null ? createdById : 0L;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaThuoc() { return maThuoc; }
    public void setMaThuoc(String maThuoc) { this.maThuoc = maThuoc; }
    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }
    public String getBietDuoc() { return bietDuoc; }
    public void setBietDuoc(String bietDuoc) { this.bietDuoc = bietDuoc; }
    public PhanLoaiThuoc getPhanLoai() {
        return phanLoai == null ? null : PhanLoaiThuoc.fromId(phanLoai);
    }
    public void setPhanLoai(PhanLoaiThuoc phanLoai) {
        this.phanLoai = phanLoai == null ? null : phanLoai.getId();
    }
    public String getNhomThuoc() { return nhomThuoc; }
    public void setNhomThuoc(String nhomThuoc) { this.nhomThuoc = nhomThuoc; }
    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }
    public String getDangBaoChe() { return dangBaoChe; }
    public void setDangBaoChe(String dangBaoChe) { this.dangBaoChe = dangBaoChe; }
    public String getSdkSgbh() { return sdkSgbh; }
    public void setSdkSgbh(String sdkSgbh) { this.sdkSgbh = sdkSgbh; }
    public String getNuocSanXuat() { return nuocSanXuat; }
    public void setNuocSanXuat(String nuocSanXuat) { this.nuocSanXuat = nuocSanXuat; }
    public String getHangSanXuat() { return hangSanXuat; }
    public void setHangSanXuat(String hangSanXuat) { this.hangSanXuat = hangSanXuat; }
    public String getSoDangKy() { return soDangKy; }
    public void setSoDangKy(String soDangKy) { this.soDangKy = soDangKy; }
    public Boolean getLaGayNghien() { return laGayNghien; }
    public void setLaGayNghien(Boolean laGayNghien) { this.laGayNghien = laGayNghien; }
    public Boolean getLaHuongThan() { return laHuongThan; }
    public void setLaHuongThan(Boolean laHuongThan) { this.laHuongThan = laHuongThan; }
    public Boolean getLaThuocYhct() { return laThuocYhct; }
    public void setLaThuocYhct(Boolean laThuocYhct) { this.laThuocYhct = laThuocYhct; }
    public Boolean getLaThuocKeDon() { return laThuocKeDon; }
    public void setLaThuocKeDon(Boolean laThuocKeDon) { this.laThuocKeDon = laThuocKeDon; }
    public Long getGiaBhyt() { return giaBhyt; }
    public void setGiaBhyt(Long giaBhyt) { this.giaBhyt = giaBhyt; }
    public Long getGiaThiTruong() { return giaThiTruong; }
    public void setGiaThiTruong(Long giaThiTruong) { this.giaThiTruong = giaThiTruong; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public Integer getPhienBan() { return phienBan; }
    public void setPhienBan(Integer phienBan) { this.phienBan = phienBan; }
    public Date getImportedAt() { return importedAt; }
    public void setImportedAt(Date importedAt) { this.importedAt = importedAt; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public Long getUpdatedById() { return updatedById; }
    public void setUpdatedById(Long updatedById) { this.updatedById = updatedById; }
}
