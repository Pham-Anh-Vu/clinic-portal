package com.company.clinicportal.entity;

import com.company.clinicportal.enumentity.GioiTinh;
import com.company.clinicportal.enumentity.NguonBenhNhan;
import com.company.clinicportal.enumentity.TrangThaiPhieuDT;
import io.jmix.core.FileRef;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "benh_nhan")
@Entity
public class BenhNhan {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "\"createdById\"")
    private Long createdById;

    @Column(name = "dia_chi")
    @Lob
    private String diaChi;

    @Column(name = "dien_thoai")
    private String dienThoai;

    @Column(name = "gioi_tinh")
    private String gioiTinh;

    @Column(name = "ho_ten_nguoi_than")
    private String hoTenNguoiThan;

    @Column(name = "ho_va_ten")
    private String hoVaTen;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_sinh")
    private Date ngaySinh;

    @Column(name = "nguoi_gioi_thieu")
    private String nguoiGioiThieu;

    @Column(name = "nguon_benh_nhan")
    private String nguonBenhNhan;

    @Column(name = "quan_he_voi_benh_nhan")
    private String quanHeVoiBenhNhan;

    @Column(name = "sdt_nguoi_than")
    private String sdtNguoiThan;

    @Column(name = "tuoi")
    private String tuoi;

    @Column(name = "trang_thai_kham_benh")
    private String trangThaiKhamBenh;

    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_kham_benh")
    private Date ngayKhamBenh;

    @Temporal(TemporalType.DATE)
    @Column(name = "thoi_gian_tai_kham")
    private Date thoiGianTaiKham;

    @Column(name = "file_so_da_ky", length = 1024)
    private FileRef fileSoDaKy;

    @Column(name = "ten_file_so_da_ky")
    private String tenFileSoDaKy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"updatedAt\"")
    private Date updatedAt;

    @Column(name = "\"updatedById\"")
    private Long updatedById;

    @Composition
    @OneToMany(mappedBy = "idBenhNhan")
    private List<PhieuDieuTri> phieuDieuTri;

    public List<PhieuDieuTri> getPhieuDieuTri() {
        return phieuDieuTri;
    }

    public void setPhieuDieuTri(List<PhieuDieuTri> phieuDieuTri) {
        this.phieuDieuTri = phieuDieuTri;
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

    public String getTuoi() {
        return tuoi;
    }

    public void setTuoi(String tuoi) {
        this.tuoi = tuoi;
    }

    public TrangThaiPhieuDT getTrangThaiKhamBenh() {
        return trangThaiKhamBenh == null ? null : TrangThaiPhieuDT.fromId(trangThaiKhamBenh);
    }

    public void setTrangThaiKhamBenh(TrangThaiPhieuDT trangThaiKhamBenh) {
        this.trangThaiKhamBenh = trangThaiKhamBenh == null ? null : trangThaiKhamBenh.getId();
    }

    public Date getNgayKhamBenh() {
        return ngayKhamBenh;
    }

    public void setNgayKhamBenh(Date ngayKhamBenh) {
        this.ngayKhamBenh = ngayKhamBenh;
    }

    public Date getThoiGianTaiKham() {
        return thoiGianTaiKham;
    }

    public void setThoiGianTaiKham(Date thoiGianTaiKham) {
        this.thoiGianTaiKham = thoiGianTaiKham;
    }

    public FileRef getFileSoDaKy() {
        return fileSoDaKy;
    }

    public void setFileSoDaKy(FileRef fileSoDaKy) {
        this.fileSoDaKy = fileSoDaKy;
    }

    public String getTenFileSoDaKy() {
        return tenFileSoDaKy;
    }

    public void setTenFileSoDaKy(String tenFileSoDaKy) {
        this.tenFileSoDaKy = tenFileSoDaKy;
    }

    public String getSdtNguoiThan() {
        return sdtNguoiThan;
    }

    public void setSdtNguoiThan(String sdtNguoiThan) {
        this.sdtNguoiThan = sdtNguoiThan;
    }

    public String getQuanHeVoiBenhNhan() {
        return quanHeVoiBenhNhan;
    }

    public void setQuanHeVoiBenhNhan(String quanHeVoiBenhNhan) {
        this.quanHeVoiBenhNhan = quanHeVoiBenhNhan;
    }

    public NguonBenhNhan getNguonBenhNhan() {
        return nguonBenhNhan == null ? null : NguonBenhNhan.fromId(nguonBenhNhan);
    }

    public void setNguonBenhNhan(NguonBenhNhan nguonBenhNhan) {
        this.nguonBenhNhan = nguonBenhNhan == null ? null : nguonBenhNhan.getId();
    }

    public String getNguoiGioiThieu() {
        return nguoiGioiThieu;
    }

    public void setNguoiGioiThieu(String nguoiGioiThieu) {
        this.nguoiGioiThieu = nguoiGioiThieu;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getHoVaTen() {
        return hoVaTen;
    }

    public void setHoVaTen(String hoVaTen) {
        this.hoVaTen = hoVaTen;
    }

    public String getHoTenNguoiThan() {
        return hoTenNguoiThan;
    }

    public void setHoTenNguoiThan(String hoTenNguoiThan) {
        this.hoTenNguoiThan = hoTenNguoiThan;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @PrePersist
    private void onPrePersist() {
        Date now = new Date();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (createdById == null) {
            createdById = 0L;
        }
        if (updatedById == null) {
            updatedById = createdById;
        }
        recalculateTuoi();
    }

    @PreUpdate
    private void onPreUpdate() {
        updatedAt = new Date();
        if (updatedById == null) {
            updatedById = createdById != null ? createdById : 0L;
        }
        recalculateTuoi();
    }

    private void recalculateTuoi() {
        this.tuoi = calculateTuoi(this.ngaySinh);
    }

    public static String calculateTuoi(Date ngaySinh) {
        if (ngaySinh == null) {
            return null;
        }

        LocalDate birthDate;
        if (ngaySinh instanceof java.sql.Date sqlDate) {
            birthDate = sqlDate.toLocalDate();
        } else {
            birthDate = ngaySinh.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        LocalDate today = LocalDate.now();

        if (birthDate.isAfter(today)) {
            return null;
        }

        long months = ChronoUnit.MONTHS.between(birthDate, today);
        if (months < 72) {
            return months + " tháng";
        }

        long years = ChronoUnit.YEARS.between(birthDate, today);
        return years + " tuổi";
    }

    @InstanceName
    @DependsOnProperties({"hoVaTen", "id"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s - %s",id ,hoVaTen);
    }
}