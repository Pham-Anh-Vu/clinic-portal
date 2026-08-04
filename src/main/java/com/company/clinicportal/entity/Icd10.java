package com.company.clinicportal.entity;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;

/**
 * Danh mục ICD-10 tối thiểu cho kê đơn điện tử và liên thông.
 * Mã ICD lưu snapshot trên đơn thuốc để tránh thay đổi khi danh mục cập nhật.
 */
@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "icd10",
       indexes = {
           @Index(name = "IDX_ICD10_MA", columnList = "ma_icd", unique = true),
           @Index(name = "IDX_ICD10_CHUONG", columnList = "chuong")
       })
@Entity
public class Icd10 {

    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_icd", nullable = false, length = 16)
    private String maIcd;

    @Column(name = "ten_benh", nullable = false, length = 512)
    private String tenBenh;

    @Column(name = "ten_benh_en", length = 512)
    private String tenBenhEn;

    @Column(name = "chuong", length = 16)
    private String chuong;

    @Column(name = "nhom_chinh", length = 16)
    private String nhomChinh;

    @Column(name = "nhom_phu", length = 16)
    private String nhomPhu;

    @Column(name = "mo_ta", length = 1024)
    private String moTa;

    @Column(name = "phien_ban")
    private Integer phienBan;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "imported_at")
    private Date importedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdat\"")
    private Date createdAt;

    @Column(name = "\"createdbyid\"")
    private Long createdById;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"updatedat\"")
    private Date updatedAt;

    @Column(name = "\"updatedbyid\"")
    private Long updatedById;

    @InstanceName
    @DependsOnProperties({"maIcd", "tenBenh"})
    public String getInstanceName() {
        return (maIcd == null ? "" : maIcd) + " - " + (tenBenh == null ? "" : tenBenh);
    }

    @PrePersist
    void onPrePersist() {
        Date now = new Date();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (createdById == null) createdById = 0L;
        if (updatedById == null) updatedById = createdById;
        if (phienBan == null) phienBan = 1;
    }

    @PreUpdate
    void onPreUpdate() {
        updatedAt = new Date();
        if (updatedById == null) updatedById = createdById != null ? createdById : 0L;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaIcd() { return maIcd; }
    public void setMaIcd(String maIcd) { this.maIcd = maIcd; }
    public String getTenBenh() { return tenBenh; }
    public void setTenBenh(String tenBenh) { this.tenBenh = tenBenh; }
    public String getTenBenhEn() { return tenBenhEn; }
    public void setTenBenhEn(String tenBenhEn) { this.tenBenhEn = tenBenhEn; }
    public String getChuong() { return chuong; }
    public void setChuong(String chuong) { this.chuong = chuong; }
    public String getNhomChinh() { return nhomChinh; }
    public void setNhomChinh(String nhomChinh) { this.nhomChinh = nhomChinh; }
    public String getNhomPhu() { return nhomPhu; }
    public void setNhomPhu(String nhomPhu) { this.nhomPhu = nhomPhu; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Integer getPhienBan() { return phienBan; }
    public void setPhienBan(Integer phienBan) { this.phienBan = phienBan; }
    public Date getImportedAt() { return importedAt; }
    public void setImportedAt(Date importedAt) { this.importedAt = importedAt; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public Long getUpdatedById() { return updatedById; }
    public void setUpdatedById(Long updatedById) { this.updatedById = updatedById; }
}
