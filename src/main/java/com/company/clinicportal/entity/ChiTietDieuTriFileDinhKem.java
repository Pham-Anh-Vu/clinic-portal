package com.company.clinicportal.entity;

import io.jmix.core.FileRef;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "chi_tiet_dieu_tri_file_dinh_kem", indexes = {
        @Index(name = "IDX_CTDT_FILE_DINH_KEM_CTDT", columnList = "ID_CHI_TIET_DIEU_TRI")
})
@Entity
public class ChiTietDieuTriFileDinhKem {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "ID_CHI_TIET_DIEU_TRI", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ChiTietDieuTri chiTietDieuTri;

    @Column(name = "ten_file")
    private String tenFile;

    @Column(name = "file_ref", length = 1024)
    private FileRef file;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChiTietDieuTri getChiTietDieuTri() {
        return chiTietDieuTri;
    }

    public void setChiTietDieuTri(ChiTietDieuTri chiTietDieuTri) {
        this.chiTietDieuTri = chiTietDieuTri;
    }

    public String getTenFile() {
        return tenFile;
    }

    public void setTenFile(String tenFile) {
        this.tenFile = tenFile;
    }

    public FileRef getFile() {
        return file;
    }

    public void setFile(FileRef file) {
        this.file = file;
    }
}
