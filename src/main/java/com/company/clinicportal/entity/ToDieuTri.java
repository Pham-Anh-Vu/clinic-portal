package com.company.clinicportal.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "to_dieu_tri", indexes = {
        @Index(name = "IDX_TO_DIEU_TRI_CTDT", columnList = "ID_CHI_TIET_DIEU_TRI")
})
@Entity
public class ToDieuTri {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "ID_CHI_TIET_DIEU_TRI", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ChiTietDieuTri chiTietDieuTri;

    @Temporal(TemporalType.DATE)
    @Column(name = "tu_ngay")
    private Date tuNgay;

    @Temporal(TemporalType.DATE)
    @Column(name = "den_ngay")
    private Date denNgay;

    @Lob
    @Column(name = "mo_ta_dien_bien_benh")
    private String moTaDienBienBenh;

    @JoinColumn(name = "ID_NGUOI_THUC_HIEN")
    @ManyToOne(fetch = FetchType.LAZY)
    private NhanSu idNguoiThucHien;

    @JoinColumn(name = "ID_BAC_SI_CHI_DINH")
    @ManyToOne(fetch = FetchType.LAZY)
    private NhanSu idBacSiChiDinh;

    @Lob
    @Column(name = "ghi_chu")
    private String ghiChu;

    @OnDelete(DeletePolicy.CASCADE)
    @Composition
    @OneToMany(mappedBy = "toDieuTri", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ToDieuTriKyThuat> kyThuatList;

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

    public Date getTuNgay() {
        return tuNgay;
    }

    public void setTuNgay(Date tuNgay) {
        this.tuNgay = tuNgay;
    }

    public Date getDenNgay() {
        return denNgay;
    }

    public void setDenNgay(Date denNgay) {
        this.denNgay = denNgay;
    }

    public String getMoTaDienBienBenh() {
        return moTaDienBienBenh;
    }

    public void setMoTaDienBienBenh(String moTaDienBienBenh) {
        this.moTaDienBienBenh = moTaDienBienBenh;
    }

    public NhanSu getIdNguoiThucHien() {
        return idNguoiThucHien;
    }

    public void setIdNguoiThucHien(NhanSu idNguoiThucHien) {
        this.idNguoiThucHien = idNguoiThucHien;
    }

    public NhanSu getIdBacSiChiDinh() {
        return idBacSiChiDinh;
    }

    public void setIdBacSiChiDinh(NhanSu idBacSiChiDinh) {
        this.idBacSiChiDinh = idBacSiChiDinh;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public List<ToDieuTriKyThuat> getKyThuatList() {
        return kyThuatList;
    }

    public void setKyThuatList(List<ToDieuTriKyThuat> kyThuatList) {
        this.kyThuatList = kyThuatList;
    }
}
