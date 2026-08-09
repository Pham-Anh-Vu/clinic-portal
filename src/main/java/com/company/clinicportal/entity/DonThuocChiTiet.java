package com.company.clinicportal.entity;

import com.company.clinicportal.enumentity.DuongDung;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

/**
 * Dòng thuốc chi tiết trong đơn thuốc. Mọi thuộc tính dùng cho mapping là snapshot
 * (maThuoc, tenThuoc, bietDuoc, donViTinh, hamLuong...) - khi danh mục cập nhật,
 * các dòng cũ giữ nguyên để đảm bảo tính bất biến của hồ sơ.
 */
@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "don_thuoc_chi_tiet",
       indexes = {
           @Index(name = "IDX_DTCT_DON_THUOC", columnList = "id_don_thuoc"),
           @Index(name = "IDX_DTCT_MA_THUOC", columnList = "ma_thuoc_snapshot")
       })
@Entity
public class DonThuocChiTiet {

    @Id
    @Column(name = "id", nullable = false)
    @JmixGeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_don_thuoc", nullable = false)
    private DonThuoc donThuoc;

    @Column(name = "stt")
    private Integer stt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dm_thuoc")
    private DmThuoc dmThuoc;

    // Snapshot fields - giữ giá trị tại thời điểm kê
    @Column(name = "ma_thuoc_snapshot", length = 64)
    private String maThuocSnapshot;

    @Column(name = "ten_thuoc_snapshot", length = 512)
    private String tenThuocSnapshot;

    @Column(name = "biet_duoc_snapshot", length = 512)
    private String bietDuocSnapshot;

    @Column(name = "don_vi_tinh_snapshot", length = 32)
    private String donViTinhSnapshot;

    @Column(name = "ham_luong_snapshot", length = 128)
    private String hamLuongSnapshot;

    @Column(name = "dang_bao_che_snapshot", length = 128)
    private String dangBaoCheSnapshot;

    @Column(name = "so_dang_ky_snapshot", length = 64)
    private String soDangKySnapshot;

    // Dữ liệu kê
    @Column(name = "so_luong")
    private java.math.BigDecimal soLuong;

    @Column(name = "lieu_dung", length = 256)
    private String lieuDung;

    @Column(name = "tan_suat", length = 64)
    private String tanSuat;

    @Column(name = "thoi_gian_dung", length = 128)
    private String thoiGianDung;

    @Column(name = "cach_dung", length = 1024)
    private String cachDung;

    @Column(name = "duong_dung", length = 16)
    private String duongDung;

    @Column(name = "ghi_chu", length = 1024)
    private String ghiChu;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdat\"")
    private Date createdAt;

    @Column(name = "\"createdbyid\"")
    private Long createdById;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public DonThuoc getDonThuoc() { return donThuoc; }
    public void setDonThuoc(DonThuoc donThuoc) { this.donThuoc = donThuoc; }
    public Integer getStt() { return stt; }
    public void setStt(Integer stt) { this.stt = stt; }
    public DmThuoc getDmThuoc() { return dmThuoc; }
    public void setDmThuoc(DmThuoc dmThuoc) { this.dmThuoc = dmThuoc; }
    public String getMaThuocSnapshot() { return maThuocSnapshot; }
    public void setMaThuocSnapshot(String maThuocSnapshot) { this.maThuocSnapshot = maThuocSnapshot; }
    public String getTenThuocSnapshot() { return tenThuocSnapshot; }
    public void setTenThuocSnapshot(String tenThuocSnapshot) { this.tenThuocSnapshot = tenThuocSnapshot; }
    public String getBietDuocSnapshot() { return bietDuocSnapshot; }
    public void setBietDuocSnapshot(String bietDuocSnapshot) { this.bietDuocSnapshot = bietDuocSnapshot; }
    public String getDonViTinhSnapshot() { return donViTinhSnapshot; }
    public void setDonViTinhSnapshot(String donViTinhSnapshot) { this.donViTinhSnapshot = donViTinhSnapshot; }
    public String getHamLuongSnapshot() { return hamLuongSnapshot; }
    public void setHamLuongSnapshot(String hamLuongSnapshot) { this.hamLuongSnapshot = hamLuongSnapshot; }
    public String getDangBaoCheSnapshot() { return dangBaoCheSnapshot; }
    public void setDangBaoCheSnapshot(String dangBaoCheSnapshot) { this.dangBaoCheSnapshot = dangBaoCheSnapshot; }
    public String getSoDangKySnapshot() { return soDangKySnapshot; }
    public void setSoDangKySnapshot(String soDangKySnapshot) { this.soDangKySnapshot = soDangKySnapshot; }
    public java.math.BigDecimal getSoLuong() { return soLuong; }
    public void setSoLuong(java.math.BigDecimal soLuong) { this.soLuong = soLuong; }
    public String getLieuDung() { return lieuDung; }
    public void setLieuDung(String lieuDung) { this.lieuDung = lieuDung; }
    public String getTanSuat() { return tanSuat; }
    public void setTanSuat(String tanSuat) { this.tanSuat = tanSuat; }
    public String getThoiGianDung() { return thoiGianDung; }
    public void setThoiGianDung(String thoiGianDung) { this.thoiGianDung = thoiGianDung; }
    public String getCachDung() { return cachDung; }
    public void setCachDung(String cachDung) { this.cachDung = cachDung; }

    public DuongDung getDuongDungEnum() { return DuongDung.fromId(duongDung); }
    public void setDuongDungEnum(DuongDung v) { this.duongDung = v == null ? null : v.getId(); }
    public String getDuongDung() { return duongDung; }
    public void setDuongDung(String duongDung) { this.duongDung = duongDung; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    /** Tự chụp lại snapshot từ DmThuoc (gọi trước save). */
    public void snapshotFrom(DmThuoc dm) {
        if (dm == null) return;
        this.dmThuoc = dm;
        this.maThuocSnapshot = dm.getMaThuoc();
        this.tenThuocSnapshot = dm.getTenThuoc();
        this.bietDuocSnapshot = dm.getBietDuoc();
        this.donViTinhSnapshot = dm.getDonViTinh();
        this.dangBaoCheSnapshot = dm.getDangBaoChe();
        this.soDangKySnapshot = dm.getSoDangKy();
    }
}
