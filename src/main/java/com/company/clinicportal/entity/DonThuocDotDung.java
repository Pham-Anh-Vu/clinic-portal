package com.company.clinicportal.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

/**
 * Đợt dùng thuốc (đặc biệt quan trọng cho YHCT).
 */
@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "don_thuoc_dot_dung", indexes = {
        @Index(name = "IDX_DTDD_DON_THUOC", columnList = "id_don_thuoc")
})
@Entity
public class DonThuocDotDung {

    @Id
    @Column(name = "id", nullable = false)
    @JmixGeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_don_thuoc", nullable = false)
    private DonThuoc donThuoc;

    @Column(name = "so_dot")
    private Integer soDot;

    @Temporal(TemporalType.DATE)
    @Column(name = "tu_ngay")
    private Date tuNgay;

    @Temporal(TemporalType.DATE)
    @Column(name = "den_ngay")
    private Date denNgay;

    @Column(name = "so_thang_thuoc")
    private Integer soThangThuoc;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdat\"")
    private Date createdAt;

    @Column(name = "\"createdbyid\"")
    private Long createdById;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public DonThuoc getDonThuoc() { return donThuoc; }
    public void setDonThuoc(DonThuoc donThuoc) { this.donThuoc = donThuoc; }
    public Integer getSoDot() { return soDot; }
    public void setSoDot(Integer soDot) { this.soDot = soDot; }
    public Date getTuNgay() { return tuNgay; }
    public void setTuNgay(Date tuNgay) { this.tuNgay = tuNgay; }
    public Date getDenNgay() { return denNgay; }
    public void setDenNgay(Date denNgay) { this.denNgay = denNgay; }
    public Integer getSoThangThuoc() { return soThangThuoc; }
    public void setSoThangThuoc(Integer soThangThuoc) { this.soThangThuoc = soThangThuoc; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
}
