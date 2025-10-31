package com.company.clinicportal.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

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

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @Column(name = "id_bac_si")
    private Long idBacSi;

    @JoinColumn(name = "ID_CHI_TIET_PHIEU_DIEU_TRI")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChiTietDieuTri idChiTietPhieuDieuTri;

    @JoinColumn(name = "ID_DICH_VU")
    @ManyToOne(fetch = FetchType.LAZY)
    private DmDichVu idDichVu;

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

    public void setIdDichVu(DmDichVu idDichVu) {
        this.idDichVu = idDichVu;
    }

    public DmDichVu getIdDichVu() {
        return idDichVu;
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

    public Long getIdBacSi() {
        return idBacSi;
    }

    public void setIdBacSi(Long idBacSi) {
        this.idBacSi = idBacSi;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}