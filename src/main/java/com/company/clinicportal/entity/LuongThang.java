package com.company.clinicportal.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "luong_thang")
@Entity
public class LuongThang {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @Column(name = "id_nhan_su")
    private Long idNhanSu;

    @Column(name = "id_so_quy_thang")
    private Long idSoQuyThang;

    @Temporal(TemporalType.DATE)
    @Column(name = "thang")
    private Date thang;

    @Column(name = "tong_luong")
    private Long tongLuong;

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

    public Long getTongLuong() {
        return tongLuong;
    }

    public void setTongLuong(Long tongLuong) {
        this.tongLuong = tongLuong;
    }

    public Date getThang() {
        return thang;
    }

    public void setThang(Date thang) {
        this.thang = thang;
    }

    public Long getIdSoQuyThang() {
        return idSoQuyThang;
    }

    public void setIdSoQuyThang(Long idSoQuyThang) {
        this.idSoQuyThang = idSoQuyThang;
    }

    public Long getIdNhanSu() {
        return idNhanSu;
    }

    public void setIdNhanSu(Long idNhanSu) {
        this.idNhanSu = idNhanSu;
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