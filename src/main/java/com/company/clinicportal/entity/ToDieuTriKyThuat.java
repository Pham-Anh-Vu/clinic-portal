package com.company.clinicportal.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "to_dieu_tri_ky_thuat", indexes = {
        @Index(name = "IDX_TO_DIEU_TRI_KT_TD", columnList = "ID_TO_DIEU_TRI")
})
@Entity
public class ToDieuTriKyThuat {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "ID_TO_DIEU_TRI", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ToDieuTri toDieuTri;

    @JoinColumn(name = "ID_DICH_VU")
    @ManyToOne(fetch = FetchType.LAZY)
    private DmDichVu idDichVu;

    @Column(name = "thoi_gian_phut")
    private Integer thoiGianPhut;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ToDieuTri getToDieuTri() {
        return toDieuTri;
    }

    public void setToDieuTri(ToDieuTri toDieuTri) {
        this.toDieuTri = toDieuTri;
    }

    public DmDichVu getIdDichVu() {
        return idDichVu;
    }

    public void setIdDichVu(DmDichVu idDichVu) {
        this.idDichVu = idDichVu;
    }

    public Integer getThoiGianPhut() {
        return thoiGianPhut;
    }

    public void setThoiGianPhut(Integer thoiGianPhut) {
        this.thoiGianPhut = thoiGianPhut;
    }
}
