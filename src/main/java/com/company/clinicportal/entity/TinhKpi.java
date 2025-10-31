package com.company.clinicportal.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "tinh_kpi")
@Entity
public class TinhKpi {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @Column(name = "id_luong_thang")
    private Long idLuongThang;

    @JoinColumn(name = "ID_NHAN_SU")
    @ManyToOne(fetch = FetchType.LAZY)
    private NhanSu idNhanSu;

    @Column(name = "kpi_sang")
    private Double kpiSang;

    @Column(name = "kpi_toi")
    private Double kpiToi;

    @Column(name = "kpi_tong")
    private Double kpiTong;

    @Temporal(TemporalType.DATE)
    @Column(name = "thang")
    private Date thang;

    @Column(name = "thanh_tien")
    private Long thanhTien;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"updatedAt\"")
    private Date updatedAt;

    @Column(name = "\"updatedById\"")
    private Long updatedById;

    public void setIdNhanSu(NhanSu idNhanSu) {
        this.idNhanSu = idNhanSu;
    }

    public NhanSu getIdNhanSu() {
        return idNhanSu;
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

    public Long getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(Long thanhTien) {
        this.thanhTien = thanhTien;
    }

    public Date getThang() {
        return thang;
    }

    public void setThang(Date thang) {
        this.thang = thang;
    }

    public Double getKpiTong() {
        return kpiTong;
    }

    public void setKpiTong(Double kpiTong) {
        this.kpiTong = kpiTong;
    }

    public Double getKpiToi() {
        return kpiToi;
    }

    public void setKpiToi(Double kpiToi) {
        this.kpiToi = kpiToi;
    }

    public Double getKpiSang() {
        return kpiSang;
    }

    public void setKpiSang(Double kpiSang) {
        this.kpiSang = kpiSang;
    }

    public Long getIdLuongThang() {
        return idLuongThang;
    }

    public void setIdLuongThang(Long idLuongThang) {
        this.idLuongThang = idLuongThang;
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