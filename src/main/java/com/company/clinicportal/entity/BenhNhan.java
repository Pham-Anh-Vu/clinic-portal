package com.company.clinicportal.entity;

import com.company.clinicportal.enumentity.GioiTinh;
import com.company.clinicportal.enumentity.NguonBenhNhan;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "benh_nhan")
@Entity
public class BenhNhan {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @Column(name = "dia_chi")
    @Lob
    private String diaChi;

    @Column(name = "dien_thoai")
    private String dienThoai;

    @Column(name = "gioi_tinh")
    private String gioiTinh;

    @Column(name = "ho_ten_nguoi_than")
    private String hoTenNguoiThan;

    @Column(name = "ho_va_ten")
    private String hoVaTen;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_sinh")
    private Date ngaySinh;

    @Column(name = "nguoi_gioi_thieu")
    private String nguoiGioiThieu;

    @Column(name = "nguon_benh_nhan")
    private String nguonBenhNhan;

    @Column(name = "quan_he_voi_benh_nhan")
    private String quanHeVoiBenhNhan;

    @Column(name = "sdt_nguoi_than")
    private String sdtNguoiThan;

    @Column(name = "tuoi")
    private String tuoi;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"updatedAt\"")
    private Date updatedAt;

    @Column(name = "\"updatedById\"")
    private Long updatedById;

    @Composition
    @OneToMany(mappedBy = "idBenhNhan")
    private List<PhieuDieuTri> phieuDieuTri;

    public List<PhieuDieuTri> getPhieuDieuTri() {
        return phieuDieuTri;
    }

    public void setPhieuDieuTri(List<PhieuDieuTri> phieuDieuTri) {
        this.phieuDieuTri = phieuDieuTri;
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

    public String getTuoi() {
        return tuoi;
    }

    public void setTuoi(String tuoi) {
        this.tuoi = tuoi;
    }

    public String getSdtNguoiThan() {
        return sdtNguoiThan;
    }

    public void setSdtNguoiThan(String sdtNguoiThan) {
        this.sdtNguoiThan = sdtNguoiThan;
    }

    public String getQuanHeVoiBenhNhan() {
        return quanHeVoiBenhNhan;
    }

    public void setQuanHeVoiBenhNhan(String quanHeVoiBenhNhan) {
        this.quanHeVoiBenhNhan = quanHeVoiBenhNhan;
    }

    public NguonBenhNhan getNguonBenhNhan() {
        return nguonBenhNhan == null ? null : NguonBenhNhan.fromId(nguonBenhNhan);
    }

    public void setNguonBenhNhan(NguonBenhNhan nguonBenhNhan) {
        this.nguonBenhNhan = nguonBenhNhan == null ? null : nguonBenhNhan.getId();
    }

    public String getNguoiGioiThieu() {
        return nguoiGioiThieu;
    }

    public void setNguoiGioiThieu(String nguoiGioiThieu) {
        this.nguoiGioiThieu = nguoiGioiThieu;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getHoVaTen() {
        return hoVaTen;
    }

    public void setHoVaTen(String hoVaTen) {
        this.hoVaTen = hoVaTen;
    }

    public String getHoTenNguoiThan() {
        return hoTenNguoiThan;
    }

    public void setHoTenNguoiThan(String hoTenNguoiThan) {
        this.hoTenNguoiThan = hoTenNguoiThan;
    }

    public GioiTinh getGioiTinh() {
        return gioiTinh == null ? null : GioiTinh.fromId(gioiTinh);
    }

    public void setGioiTinh(GioiTinh gioiTinh) {
        this.gioiTinh = gioiTinh == null ? null : gioiTinh.getId();
    }

    public String getDienThoai() {
        return dienThoai;
    }

    public void setDienThoai(String dienThoai) {
        this.dienThoai = dienThoai;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
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

    @InstanceName
    @DependsOnProperties({"hoVaTen", "id"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s - %s",id ,hoVaTen);
    }
}