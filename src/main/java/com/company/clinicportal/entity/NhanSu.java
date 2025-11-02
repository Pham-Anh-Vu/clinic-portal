package com.company.clinicportal.entity;

import com.company.clinicportal.enumentity.ChucVu;
import com.company.clinicportal.enumentity.GioiTinh;
import com.company.clinicportal.enumentity.HinhThucLamViec;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.Date;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "nhan_su")
@Entity
public class NhanSu {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chuc_vu")
    private String chucVu;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "dien_thoai")
    private String dienThoai;

    @Column(name = "gioi_tinh")
    private String gioiTinh;

    @Column(name = "hinh_thuc_lam_vic")
    private String hinhThucLamVic;

    @Column(name = "ho_ten")
    private String hoTen;

    @Column(name = "luong_co_ban")
    private Long luongCoBan;

    @Column(name = "ma_nhan_su", unique = true)
    private String maNhanSu;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_sinh")
    private Date ngaySinh;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_vao_lam")
    private Date ngayVaoLam;

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

    public Date getNgayVaoLam() {
        return ngayVaoLam;
    }

    public void setNgayVaoLam(Date ngayVaoLam) {
        this.ngayVaoLam = ngayVaoLam;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getMaNhanSu() {
        return maNhanSu;
    }

    public void setMaNhanSu(String maNhanSu) {
        this.maNhanSu = maNhanSu;
    }

    public Long getLuongCoBan() {
        return luongCoBan;
    }

    public void setLuongCoBan(Long luongCoBan) {
        this.luongCoBan = luongCoBan;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public HinhThucLamViec getHinhThucLamVic() {
        return hinhThucLamVic == null ? null : HinhThucLamViec.fromId(hinhThucLamVic);
    }

    public void setHinhThucLamVic(HinhThucLamViec hinhThucLamVic) {
        this.hinhThucLamVic = hinhThucLamVic == null ? null : hinhThucLamVic.getId();
    }

    public GioiTinh getGioiTinh() {
        return gioiTinh == null ? null : GioiTinh.fromId(gioiTinh);
    }

    public void setGioiTinh(GioiTinh gioiTinh) {
        this.gioiTinh = gioiTinh == null ? null : gioiTinh.getId();
    }

    public String getDienThoai() {
        return dienThoai;
    }

    public void setDienThoai(String dienThoai) {
        this.dienThoai = dienThoai;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
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

    public ChucVu getChucVu() {
        return chucVu == null ? null : ChucVu.fromId(chucVu);
    }

    public void setChucVu(ChucVu chucVu) {
        this.chucVu = chucVu == null ? null : chucVu.getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @InstanceName
    @DependsOnProperties({"hoTen", "id"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s - %s",id ,hoTen);
    }
}