package com.company.clinicportal.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "trong_so_kpi_dich_vu")
@Entity
public class TrongSoKpiDichVu {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @Column(name = "id_phieu_dieu_tri")
    private Long idPhieuDieuTri;

    @Column(name = "trong_so_dien_tri_lieu")
    private Double trongSoDienTriLieu;

    @Column(name = "trong_so_keo_gian")
    private Double trongSoKeoGian;

    @Column(name = "trong_so_tap_phcn")
    private Double trongSoTapPhcn;

    @Column(name = "trong_so_van_dong_tri_lieu")
    private Double trongSoVanDongTriLieu;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"updatedAt\"")
    private Date updatedAt;

    @Column(name = "\"updatedById\"")
    private Long updatedById;

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

    public Double getTrongSoVanDongTriLieu() {
        return trongSoVanDongTriLieu;
    }

    public void setTrongSoVanDongTriLieu(Double trongSoVanDongTriLieu) {
        this.trongSoVanDongTriLieu = trongSoVanDongTriLieu;
    }

    public Double getTrongSoTapPhcn() {
        return trongSoTapPhcn;
    }

    public void setTrongSoTapPhcn(Double trongSoTapPhcn) {
        this.trongSoTapPhcn = trongSoTapPhcn;
    }

    public Double getTrongSoKeoGian() {
        return trongSoKeoGian;
    }

    public void setTrongSoKeoGian(Double trongSoKeoGian) {
        this.trongSoKeoGian = trongSoKeoGian;
    }

    public Double getTrongSoDienTriLieu() {
        return trongSoDienTriLieu;
    }

    public void setTrongSoDienTriLieu(Double trongSoDienTriLieu) {
        this.trongSoDienTriLieu = trongSoDienTriLieu;
    }

    public Long getIdPhieuDieuTri() {
        return idPhieuDieuTri;
    }

    public void setIdPhieuDieuTri(Long idPhieuDieuTri) {
        this.idPhieuDieuTri = idPhieuDieuTri;
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