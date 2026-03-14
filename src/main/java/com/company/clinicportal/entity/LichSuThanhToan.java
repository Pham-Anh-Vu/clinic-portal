package com.company.clinicportal.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "lich_su_thanh_toan", indexes = {
        @Index(name = "IDX_LICH_SU_THANH_TOAN_", columnList = "")
})
@Entity
public class LichSuThanhToan {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @Column(name = "da_thanh_toan")
    private Long daThanhToan;

    @Column(name = "ghi_chu")
    @Lob
    private String ghiChu;

    @Column(name = "ho_ten_nguoi_nop_tien")
    private String hoTenNguoiNopTien;

    @JoinColumn(name = "ID_CHI_TIET_DIEU_TRI")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChiTietDieuTri idChiTietDieuTri;

    @Column(name = "id_so_quy_thang")
    private Long idSoQuyThang;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "thanh_toan_luc")
    private Date thanhToanLuc;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "thanh_toan_luc_staging")
    private Date thanhToanLucStaging;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"updatedAt\"")
    private Date updatedAt;

    @Column(name = "\"updatedById\"")
    private Long updatedById;

    public void setIdChiTietDieuTri(ChiTietDieuTri idChiTietDieuTri) {
        this.idChiTietDieuTri = idChiTietDieuTri;
    }

    public ChiTietDieuTri getIdChiTietDieuTri() {
        return idChiTietDieuTri;
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

    public Date getThanhToanLucStaging() {
        return thanhToanLucStaging;
    }

    public void setThanhToanLucStaging(Date thanhToanLucStaging) {
        this.thanhToanLucStaging = thanhToanLucStaging;
    }

    public Date getThanhToanLuc() {
        return thanhToanLuc;
    }

    public void setThanhToanLuc(Date thanhToanLuc) {
        this.thanhToanLuc = thanhToanLuc;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public Long getIdSoQuyThang() {
        return idSoQuyThang;
    }

    public void setIdSoQuyThang(Long idSoQuyThang) {
        this.idSoQuyThang = idSoQuyThang;
    }

    public String getHoTenNguoiNopTien() {
        return hoTenNguoiNopTien;
    }

    public void setHoTenNguoiNopTien(String hoTenNguoiNopTien) {
        this.hoTenNguoiNopTien = hoTenNguoiNopTien;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}