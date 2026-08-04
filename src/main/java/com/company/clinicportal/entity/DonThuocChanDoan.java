package com.company.clinicportal.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

/**
 * Chẩn đoán ICD-10 snapshot trên đơn thuốc.
 */
@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "don_thuoc_chan_doan",
       indexes = {
           @Index(name = "IDX_DTCD_DON_THUOC", columnList = "id_don_thuoc"),
           @Index(name = "IDX_DTCD_MA_ICD", columnList = "ma_icd_snapshot")
       })
@Entity
public class DonThuocChanDoan {

    @Id
    @Column(name = "id", nullable = false)
    @JmixGeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_don_thuoc", nullable = false)
    private DonThuoc donThuoc;

    @Column(name = "stt")
    private Integer stt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_icd10")
    private Icd10 icd10;

    @Column(name = "ma_icd_snapshot", length = 16)
    private String maIcdSnapshot;

    @Column(name = "ten_icd_snapshot", length = 512)
    private String tenIcdSnapshot;

    @Column(name = "ket_luan", length = 1024)
    private String ketLuan;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdat\"")
    private Date createdAt;

    @Column(name = "\"createdbyid\"")
    private Long createdById;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public DonThuoc getDonThuoc() { return donThuoc; }
    public void setDonThuoc(DonThuoc donThuoc) { this.donThuoc = donThuoc; }
    public Integer getStt() { return stt; }
    public void setStt(Integer stt) { this.stt = stt; }
    public Icd10 getIcd10() { return icd10; }
    public void setIcd10(Icd10 icd10) { this.icd10 = icd10; }
    public String getMaIcdSnapshot() { return maIcdSnapshot; }
    public void setMaIcdSnapshot(String maIcdSnapshot) { this.maIcdSnapshot = maIcdSnapshot; }
    public String getTenIcdSnapshot() { return tenIcdSnapshot; }
    public void setTenIcdSnapshot(String tenIcdSnapshot) { this.tenIcdSnapshot = tenIcdSnapshot; }
    public String getKetLuan() { return ketLuan; }
    public void setKetLuan(String ketLuan) { this.ketLuan = ketLuan; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public void snapshotFrom(Icd10 icd) {
        if (icd == null) return;
        this.icd10 = icd;
        this.maIcdSnapshot = icd.getMaIcd();
        this.tenIcdSnapshot = icd.getTenBenh();
    }
}
