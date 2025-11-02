package com.company.clinicportal.entity;

import com.company.clinicportal.enumentity.TrangThaiPhieuDT;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "phieu_dieu_tri")
@Entity
public class PhieuDieuTri {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @JoinColumn(name = "ID_BENH_NHAN")
    @ManyToOne(fetch = FetchType.LAZY)
    private BenhNhan idBenhNhan;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ngay_kham")
    private Date ngayKham;

    @Temporal(TemporalType.DATE)
    @Column(name = "thoi_gian_tai_kham")
    private Date thoiGianTaiKham;

    @Column(name = "trang_thai")
    private String trangThai;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"updatedAt\"")
    private Date updatedAt;

    @Column(name = "\"updatedById\"")
    private Long updatedById;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "idPhieuDieuTri")
    private ChiTietDieuTri chiTietDieuTri;

    public ChiTietDieuTri getChiTietDieuTri() {
        return chiTietDieuTri;
    }

    public void setChiTietDieuTri(ChiTietDieuTri chiTietDieuTri) {
        this.chiTietDieuTri = chiTietDieuTri;
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

    public TrangThaiPhieuDT getTrangThai() {
        return trangThai == null ? null : TrangThaiPhieuDT.fromId(trangThai);
    }

    public void setTrangThai(TrangThaiPhieuDT trangThai) {
        this.trangThai = trangThai == null ? null : trangThai.getId();
    }

    public Date getThoiGianTaiKham() {
        return thoiGianTaiKham;
    }

    public void setThoiGianTaiKham(Date thoiGianTaiKham) {
        this.thoiGianTaiKham = thoiGianTaiKham;
    }

    public Date getNgayKham() {
        return ngayKham;
    }

    public void setNgayKham(Date ngayKham) {
        this.ngayKham = ngayKham;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
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