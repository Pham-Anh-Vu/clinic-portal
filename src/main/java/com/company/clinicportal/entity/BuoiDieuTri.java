package com.company.clinicportal.entity;

import com.company.clinicportal.enumentity.CaLamViec;
import com.company.clinicportal.enumentity.TrangThaiBuoiDieuTri;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "buoi_dieu_tri", indexes = {
        @Index(name = "IDX_BUOI_DIEU_TRI_", columnList = "")
})
@Entity
public class BuoiDieuTri {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ca")
    private String ca;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @Column(name = "f_s6gkc3y0vfj")
    private Long fS6gkc3y0vfj;

    @Temporal(TemporalType.TIME)
    @Column(name = "gio_bat_dau")
    private Date gioBatDau;

    @Temporal(TemporalType.TIME)
    @Column(name = "gio_bat_dau_staging")
    private Date gioBatDauStaging;

    @Temporal(TemporalType.TIME)
    @Column(name = "gio_ket_thuc")
    private Date gioKetThuc;

    @JoinColumn(name = "ID_BENH_NHAN")
    @ManyToOne(fetch = FetchType.LAZY)
    private BenhNhan idBenhNhan;

    @JoinColumn(name = "ID_CHI_TIET_DICH_VU")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChiTietDichVu idChiTietDichVu;

    @JoinColumn(name = "ID_CHI_TIET_DIEU_TRI")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChiTietDieuTri idChiTietDieuTri;

    @Column(name = "id_ngay_dieu_tri")
    private Long idNgayDieuTri;

    @Column(name = "id_nhan_su_2")
    private Long idNhanSu2;

    @JoinColumn(name = "ID_NHAN_SU_2_STAGING")
    @ManyToOne(fetch = FetchType.LAZY)
    private NhanSu idNhanSu2Staging;

    @JoinColumn(name = "ID_NHAN_SU_STAGING")
    @ManyToOne(fetch = FetchType.LAZY)
    private NhanSu idNhanSuStaging;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_thuc_hien")
    private Date ngayThucHien;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_thuc_hien_staging")
    private Date ngayThucHienStaging;

    @Column(name = "trang_thai")
    private String trangThai;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"updatedAt\"")
    private Date updatedAt;

    @Column(name = "\"updatedById\"")
    private Long updatedById;

    public Date getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(Date gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }

    public void setIdNhanSu2Staging(NhanSu idNhanSu2Staging) {
        this.idNhanSu2Staging = idNhanSu2Staging;
    }

    public NhanSu getIdNhanSu2Staging() {
        return idNhanSu2Staging;
    }

    public void setIdBenhNhan(BenhNhan idBenhNhan) {
        this.idBenhNhan = idBenhNhan;
    }

    public BenhNhan getIdBenhNhan() {
        return idBenhNhan;
    }

    public void setIdNhanSuStaging(NhanSu idNhanSuStaging) {
        this.idNhanSuStaging = idNhanSuStaging;
    }

    public NhanSu getIdNhanSuStaging() {
        return idNhanSuStaging;
    }

    public void setIdChiTietDichVu(ChiTietDichVu idChiTietDichVu) {
        this.idChiTietDichVu = idChiTietDichVu;
    }

    public ChiTietDichVu getIdChiTietDichVu() {
        return idChiTietDichVu;
    }

    public ChiTietDieuTri getIdChiTietDieuTri() {
        return idChiTietDieuTri;
    }

    public void setIdChiTietDieuTri(ChiTietDieuTri idChiTietDieuTri) {
        this.idChiTietDieuTri = idChiTietDieuTri;
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

    public TrangThaiBuoiDieuTri getTrangThai() {
        return trangThai == null ? null : TrangThaiBuoiDieuTri.fromId(trangThai);
    }

    public void setTrangThai(TrangThaiBuoiDieuTri trangThai) {
        this.trangThai = trangThai == null ? null : trangThai.getId();
    }

    public Date getNgayThucHienStaging() {
        return ngayThucHienStaging;
    }

    public void setNgayThucHienStaging(Date ngayThucHienStaging) {
        this.ngayThucHienStaging = ngayThucHienStaging;
    }

    public Date getNgayThucHien() {
        return ngayThucHien;
    }

    public void setNgayThucHien(Date ngayThucHien) {
        this.ngayThucHien = ngayThucHien;
    }

    public Long getIdNhanSu2() {
        return idNhanSu2;
    }

    public void setIdNhanSu2(Long idNhanSu2) {
        this.idNhanSu2 = idNhanSu2;
    }

    public Long getIdNgayDieuTri() {
        return idNgayDieuTri;
    }

    public void setIdNgayDieuTri(Long idNgayDieuTri) {
        this.idNgayDieuTri = idNgayDieuTri;
    }



    public Date getGioBatDauStaging() {
        return gioBatDauStaging;
    }

    public void setGioBatDauStaging(Date gioBatDauStaging) {
        this.gioBatDauStaging = gioBatDauStaging;
    }

    public Date getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(Date gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public Long getFS6gkc3y0vfj() {
        return fS6gkc3y0vfj;
    }

    public void setFS6gkc3y0vfj(Long fS6gkc3y0vfj) {
        this.fS6gkc3y0vfj = fS6gkc3y0vfj;
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

    public CaLamViec getCa() {
        return ca == null ? null : CaLamViec.fromId(ca);
    }

    public void setCa(CaLamViec ca) {
        this.ca = ca == null ? null : ca.getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}