package com.company.clinicportal.entity;

import com.company.clinicportal.enumentity.TinhTheoGia;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "chi_tiet_dich_vu", indexes = {
        @Index(name = "IDX_CHI_TIET_DICH_VU_", columnList = "")
})
@Entity
public class ChiTietDichVu {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "\"createdAt\"")
    private LocalDateTime createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @JoinColumn(name = "ID_BAC_SI")
    @ManyToOne(fetch = FetchType.LAZY)
    private NhanSu idBacSi;

    @JoinColumn(name = "ID_CHI_TIET_PHIEU_DIEU_TRI")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChiTietDieuTri idChiTietPhieuDieuTri;

    @JoinColumn(name = "ID_DICH_VU")
    @ManyToOne(fetch = FetchType.LAZY)
    private DmDichVu idDichVu;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "tinh_theo_gia")
    private String tinhTheoGia;

    @Column(name = "khoang_cach_buoi_dieu_tri")
    private Long khoangCachBuoiDieuTri;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_bat_dau")
    private Date ngayBatDau;

    @Column(name = "so_luong")
    private Long soLuong;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"updatedAt\"")
    private Date updatedAt;

    @Column(name = "\"updatedById\"")
    private Long updatedById;

    public TinhTheoGia getTinhTheoGia() {
        return tinhTheoGia == null ?  null : TinhTheoGia.fromId(tinhTheoGia);
    }

    public void setTinhTheoGia(TinhTheoGia tinhTheoGia) {
        this.tinhTheoGia = tinhTheoGia == null ? null : tinhTheoGia.getId();
    }

    public void setIdBacSi(NhanSu idBacSi) {
        this.idBacSi = idBacSi;
    }

    public NhanSu getIdBacSi() {
        return idBacSi;
    }

    public void setIdDichVu(DmDichVu idDichVu) {
        this.idDichVu = idDichVu;
    }

    public DmDichVu getIdDichVu() {
        return idDichVu;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public void setIdChiTietPhieuDieuTri(ChiTietDieuTri idChiTietPhieuDieuTri) {
        this.idChiTietPhieuDieuTri = idChiTietPhieuDieuTri;
    }

    public ChiTietDieuTri getIdChiTietPhieuDieuTri() {
        return idChiTietPhieuDieuTri;
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

    public Long getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Long soLuong) {
        this.soLuong = soLuong;
    }

    public Date getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(Date ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public Long getKhoangCachBuoiDieuTri() {
        return khoangCachBuoiDieuTri;
    }

    public void setKhoangCachBuoiDieuTri(Long khoangCachBuoiDieuTri) {
        this.khoangCachBuoiDieuTri = khoangCachBuoiDieuTri;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}