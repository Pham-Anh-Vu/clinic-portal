package com.company.clinicportal.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.UUID;

@JmixEntity
@Table(name = "qua_trinh_hoat_dong")
@Entity
public class QuaTrinhHoatDong {

    @Id
    @JmixGeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "ten")
    private String ten;

    @Column(name = "cccd")
    private String cccd;

    @Column(name = "thoi_gian")
    private String thoiGian;

    @Column(name = "loai_hoat_dong")
    private String loaiHoatDong;

    @Column(name = "dia_diem_hoat_dong")
    private String diaDiemHoatDong;

    @Column(name = "chuc_vu_dam_nhiem")
    private String chucVuDamNhan;



    /**
     * N - 1 tới PhieuThuThap
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_thu_thap_id")
    private PhieuThuThap phieuThuThap;

    // ===== Getter / Setter =====

    public String getChucVuDamNhan() {
        return chucVuDamNhan;
    }

    public void setChucVuDamNhan(String chucVuDamNhan) {
        this.chucVuDamNhan = chucVuDamNhan;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(String thoiGian) {
        this.thoiGian = thoiGian;
    }

    public String getLoaiHoatDong() {
        return loaiHoatDong;
    }

    public void setLoaiHoatDong(String loaiHoatDong) {
        this.loaiHoatDong = loaiHoatDong;
    }

    public String getDiaDiemHoatDong() {
        return diaDiemHoatDong;
    }

    public void setDiaDiemHoatDong(String diaDiemHoatDong) {
        this.diaDiemHoatDong = diaDiemHoatDong;
    }

    public PhieuThuThap getPhieuThuThap() {
        return phieuThuThap;
    }

    public void setPhieuThuThap(PhieuThuThap phieuThuThap) {
        this.phieuThuThap = phieuThuThap;
    }
}
