package com.company.clinicportal.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "lich_hen", indexes = {
        @Index(name = "IDX_LICH_HEN_ID_BENH_NHAN", columnList = "ID_BENH_NHAN")
})
@Entity
public class LichHen {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @Column(name = "hinh_thuc")
    private String hinhThuc;

    @Column(name = "id_bac_si")
    private Long idBacSi;

    @JoinColumn(name = "ID_BENH_NHAN")
    @ManyToOne(fetch = FetchType.LAZY)
    private BenhNhan idBenhNhan;

    @Column(name = "luu_y")
    private String luuY;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_hen")
    private Date ngayHen;

    @Column(name = "nhu_cau_khach_hang")
    private String nhuCauKhachHang;

    @Temporal(TemporalType.TIME)
    @Column(name = "thoi_gian_hen")
    private Date thoiGianHen;

    @Column(name = "tinh_trang_benh")
    private String tinhTrangBenh;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"updatedAt\"")
    private Date updatedAt;

    @Column(name = "\"updatedById\"")
    private Long updatedById;

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

    public String getTinhTrangBenh() {
        return tinhTrangBenh;
    }

    public void setTinhTrangBenh(String tinhTrangBenh) {
        this.tinhTrangBenh = tinhTrangBenh;
    }

    public Date getThoiGianHen() {
        return thoiGianHen;
    }

    public void setThoiGianHen(Date thoiGianHen) {
        this.thoiGianHen = thoiGianHen;
    }

    public String getNhuCauKhachHang() {
        return nhuCauKhachHang;
    }

    public void setNhuCauKhachHang(String nhuCauKhachHang) {
        this.nhuCauKhachHang = nhuCauKhachHang;
    }

    public Date getNgayHen() {
        return ngayHen;
    }

    public void setNgayHen(Date ngayHen) {
        this.ngayHen = ngayHen;
    }

    public String getLuuY() {
        return luuY;
    }

    public void setLuuY(String luuY) {
        this.luuY = luuY;
    }

    public Long getIdBacSi() {
        return idBacSi;
    }

    public void setIdBacSi(Long idBacSi) {
        this.idBacSi = idBacSi;
    }

    public String getHinhThuc() {
        return hinhThuc;
    }

    public void setHinhThuc(String hinhThuc) {
        this.hinhThuc = hinhThuc;
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