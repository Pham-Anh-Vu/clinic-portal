package com.company.clinicportal.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "ngay_dieu_tri", indexes = {
        @Index(name = "IDX_NGAY_DIEU_TRI_", columnList = ""),
        @Index(name = "IDX_NGAY_DIEU_TRI_ID_BENH_NHAN", columnList = "ID_BENH_NHAN")
})
@Entity
public class NgayDieuTri {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @Temporal(TemporalType.TIME)
    @Column(name = "gio_hen")
    private Date gioHen;

    @JoinColumn(name = "ID_BENH_NHAN")
    @ManyToOne(fetch = FetchType.LAZY)
    private BenhNhan idBenhNhan;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay")
    private Date ngay;

    @Column(name = "so_buoi")
    private Long soBuoi;

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

    public Long getSoBuoi() {
        return soBuoi;
    }

    public void setSoBuoi(Long soBuoi) {
        this.soBuoi = soBuoi;
    }

    public Date getNgay() {
        return ngay;
    }

    public void setNgay(Date ngay) {
        this.ngay = ngay;
    }

    public Date getGioHen() {
        return gioHen;
    }

    public void setGioHen(Date gioHen) {
        this.gioHen = gioHen;
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