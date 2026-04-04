package com.company.clinicportal.entity;

import com.company.clinicportal.enumentity.NhomDichVu;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "dm_dich_vu")
@Entity
public class DmDichVu {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @Column(name = "don_vi_tinh")
    private String donViTinh;

    @Column(name = "f_nanlovbcevi")
    private Long fNanlovbcevi;

    @Column(name = "gia")
    private Long gia;

    @Column(name = "gia_buoi_le")
    private Long giaBuoiLe;

    @Column(name = "gia_kpi_sang")
    private Long giaKpiSang;

    @Column(name = "gia_kpi_toi")
    private Long giaKpiToi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"Id_chi_tiet_dich_vu\"")
    private ChiTietDichVu idChiTietDichVu;

    @Column(name = "khuyen_mai")
    private Double khuyenMai;

    @Column(name = "mo_ta")
    @Lob
    private String moTa;

    @Column(name = "nhom_dich_vu")
    private String nhomDichVu;

    @Column(name = "ten_dich_vu")
    private String tenDichVu;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"updatedAt\"")
    private Date updatedAt;

    @Column(name = "\"updatedById\"")
    private Long updatedById;

    public Long getGiaBuoiLe() {
        return giaBuoiLe;
    }

    public void setGiaBuoiLe(Long giaBuoiLe) {
        this.giaBuoiLe = giaBuoiLe;
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

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public NhomDichVu getNhomDichVu() {
        return nhomDichVu == null ? null : NhomDichVu.fromId(nhomDichVu);
    }

    public void setNhomDichVu(NhomDichVu nhomDichVu) {
        this.nhomDichVu = nhomDichVu == null ? null : nhomDichVu.getId();
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Double getKhuyenMai() {
        return khuyenMai;
    }

    public void setKhuyenMai(Double khuyenMai) {
        this.khuyenMai = khuyenMai;
    }

    public ChiTietDichVu getIdChiTietDichVu() {
        return idChiTietDichVu;
    }

    public void setIdChiTietDichVu(ChiTietDichVu idChiTietDichVu) {
        this.idChiTietDichVu = idChiTietDichVu;
    }

    public Long getGiaKpiToi() {
        return giaKpiToi;
    }

    public void setGiaKpiToi(Long giaKpiToi) {
        this.giaKpiToi = giaKpiToi;
    }

    public Long getGiaKpiSang() {
        return giaKpiSang;
    }

    public void setGiaKpiSang(Long giaKpiSang) {
        this.giaKpiSang = giaKpiSang;
    }

    public Long getGia() {
        return gia;
    }

    public void setGia(Long gia) {
        this.gia = gia;
    }

    public Long getFNanlovbcevi() {
        return fNanlovbcevi;
    }

    public void setFNanlovbcevi(Long fNanlovbcevi) {
        this.fNanlovbcevi = fNanlovbcevi;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
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
    @DependsOnProperties({"tenDichVu"})
    public String getDisplayName() {
        return String.format("%s", tenDichVu);
    }
}