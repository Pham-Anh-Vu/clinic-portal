package com.company.clinicportal.entity;

import com.company.clinicportal.enumentity.CaLamViec;
import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "tinh_kpi_chi_tiet", indexes = {
        @Index(name = "IDX_TINH_KPI_CT_CHI_TIET", columnList = "ID_CHI_TIET_DIEU_TRI"),
        @Index(name = "IDX_TINH_KPI_CT_NHAN_SU", columnList = "ID_NHAN_SU"),
        @Index(name = "IDX_TINH_KPI_CT_BUOI", columnList = "ID_BUOI_DIEU_TRI"),
        @Index(name = "IDX_TINH_KPI_CT_DICH_VU", columnList = "ID_CHI_TIET_DICH_VU")
})
@Entity
public class TinhKpiChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"updatedAt\"")
    private Date updatedAt;

    @Column(name = "\"updatedById\"")
    private Long updatedById;

    @JoinColumn(name = "ID_CHI_TIET_DIEU_TRI")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChiTietDieuTri idChiTietDieuTri;

    @JoinColumn(name = "ID_CHI_TIET_DICH_VU")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChiTietDichVu idChiTietDichVu;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "ID_BUOI_DIEU_TRI")
    @ManyToOne(fetch = FetchType.LAZY)
    private BuoiDieuTri idBuoiDieuTri;

    @JoinColumn(name = "ID_NHAN_SU")
    @ManyToOne(fetch = FetchType.LAZY)
    private NhanSu idNhanSu;

    @Column(name = "ca_thuc_hien")
    private String caThucHien;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_thuc_hien")
    private Date ngayThucHien;

    @Column(name = "trong_so_kpi")
    private Double trongSoKpi;

    @Column(name = "thanh_tien")
    private Long thanhTien;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getUpdatedById() {
        return updatedById;
    }

    public void setUpdatedById(Long updatedById) {
        this.updatedById = updatedById;
    }

    public ChiTietDieuTri getIdChiTietDieuTri() {
        return idChiTietDieuTri;
    }

    public void setIdChiTietDieuTri(ChiTietDieuTri idChiTietDieuTri) {
        this.idChiTietDieuTri = idChiTietDieuTri;
    }

    public ChiTietDichVu getIdChiTietDichVu() {
        return idChiTietDichVu;
    }

    public void setIdChiTietDichVu(ChiTietDichVu idChiTietDichVu) {
        this.idChiTietDichVu = idChiTietDichVu;
    }

    public BuoiDieuTri getIdBuoiDieuTri() {
        return idBuoiDieuTri;
    }

    public void setIdBuoiDieuTri(BuoiDieuTri idBuoiDieuTri) {
        this.idBuoiDieuTri = idBuoiDieuTri;
    }

    public NhanSu getIdNhanSu() {
        return idNhanSu;
    }

    public void setIdNhanSu(NhanSu idNhanSu) {
        this.idNhanSu = idNhanSu;
    }

    public CaLamViec getCaThucHien() {
        return caThucHien == null ? null : CaLamViec.fromId(caThucHien);
    }

    public void setCaThucHien(CaLamViec caThucHien) {
        this.caThucHien = caThucHien == null ? null : caThucHien.getId();
    }

    public Date getNgayThucHien() {
        return ngayThucHien;
    }

    public void setNgayThucHien(Date ngayThucHien) {
        this.ngayThucHien = ngayThucHien;
    }

    public Double getTrongSoKpi() {
        return trongSoKpi;
    }

    public void setTrongSoKpi(Double trongSoKpi) {
        this.trongSoKpi = trongSoKpi;
    }

    public Long getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(Long thanhTien) {
        this.thanhTien = thanhTien;
    }
}
