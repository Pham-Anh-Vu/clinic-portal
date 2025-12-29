package com.company.clinicportal.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.DdlGeneration;
import jakarta.persistence.*;

import java.util.List;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.DISABLED)
@JmixEntity
@Table(name = "phieu_thu_thap")
@Entity
public class PhieuThuThap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "ho_va_ten")
    private String hoVaten;

    @Column(name = "ho_va_ten_khai_sinh")
    private String hoVaTenKhaiSinh;

    @Column(name = "ngay_thang_nam_sinh")
    private String ngayThangNamSinh;

    @Column(name = "gioi_tinh")
    private String gioiTinh;

    @Column(name = "dan_toc")
    private String danToc;

    @Column(name = "quoc_tich")
    private String quocTich;

    @Column(name = "so_can_cuoc_cong_dan")
    private String soCanCuocCongDan;

    @Column(name = "ngay_cap")
    private String ngayCap;

    @Column(name = "noi_cap")
    private String noiCap;

    @Column(name = "que_quan")
    private String queQuan;

    @Column(name = "noi_thuong_tru")
    private String noiThuongTru;

    @Column(name = "noi_o_hien_tai")
    private String noiOHienTai;

    @OneToMany(mappedBy = "phieuThuThap", fetch = FetchType.LAZY)
    private List<QuaTrinhHoatDong> quaTrinhHoatDongs;

    @Column(name = "trinh_do_hoc_van")
    private String trinhDoHocVan;

    @Column(name = "trinh_do_chuyen_mon")
    private String trinhDoChuyenMon;

    @Column(name = "trinh_do_dao_tao_ton_giao")
    private String trinhDoDaoTaoTonGiao;

    @Column(name = "co_so_dao_tao_ton_giao")
    private String coSoDaoTaoTonGiao;

    @Column(name = "ngoai_ngu")
    private String ngoaiNgu;

    public String getNgoaiNgu() {
        return ngoaiNgu;
    }

    public void setNgoaiNgu(String ngoaiNgu) {
        this.ngoaiNgu = ngoaiNgu;
    }

    public String getCoSoDaoTaoTonGiao() {
        return coSoDaoTaoTonGiao;
    }

    public void setCoSoDaoTaoTonGiao(String coSoDaoTaoTonGiao) {
        this.coSoDaoTaoTonGiao = coSoDaoTaoTonGiao;
    }

    public String getTrinhDoDaoTaoTonGiao() {
        return trinhDoDaoTaoTonGiao;
    }

    public void setTrinhDoDaoTaoTonGiao(String trinhDoDaoTaoTonGiao) {
        this.trinhDoDaoTaoTonGiao = trinhDoDaoTaoTonGiao;
    }

    public String getTrinhDoChuyenMon() {
        return trinhDoChuyenMon;
    }

    public void setTrinhDoChuyenMon(String trinhDoChuyenMon) {
        this.trinhDoChuyenMon = trinhDoChuyenMon;
    }

    public String getTrinhDoHocVan() {
        return trinhDoHocVan;
    }

    public void setTrinhDoHocVan(String trinhDoHocVan) {
        this.trinhDoHocVan = trinhDoHocVan;
    }

    public List<QuaTrinhHoatDong> getQuaTrinhHoatDongs() {
        return quaTrinhHoatDongs;
    }

    public void setQuaTrinhHoatDongs(List<QuaTrinhHoatDong> quaTrinhHoatDongs) {
        this.quaTrinhHoatDongs = quaTrinhHoatDongs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHoVaten() {
        return hoVaten;
    }

    public void setHoVaten(String hoVaten) {
        this.hoVaten = hoVaten;
    }

    public String getHoVaTenKhaiSinh() {
        return hoVaTenKhaiSinh;
    }

    public void setHoVaTenKhaiSinh(String hoVaTenKhaiSinh) {
        this.hoVaTenKhaiSinh = hoVaTenKhaiSinh;
    }

    public String getNgayThangNamSinh() {
        return ngayThangNamSinh;
    }

    public void setNgayThangNamSinh(String ngayThangNamSinh) {
        this.ngayThangNamSinh = ngayThangNamSinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getDanToc() {
        return danToc;
    }

    public void setDanToc(String danToc) {
        this.danToc = danToc;
    }

    public String getQuocTich() {
        return quocTich;
    }

    public void setQuocTich(String quocTich) {
        this.quocTich = quocTich;
    }

    public String getSoCanCuocCongDan() {
        return soCanCuocCongDan;
    }

    public void setSoCanCuocCongDan(String soCanCuocCongDan) {
        this.soCanCuocCongDan = soCanCuocCongDan;
    }

    public String getNgayCap() {
        return ngayCap;
    }

    public void setNgayCap(String ngayCap) {
        this.ngayCap = ngayCap;
    }

    public String getNoiCap() {
        return noiCap;
    }

    public void setNoiCap(String noiCap) {
        this.noiCap = noiCap;
    }

    public String getQueQuan() {
        return queQuan;
    }

    public void setQueQuan(String queQuan) {
        this.queQuan = queQuan;
    }

    public String getNoiThuongTru() {
        return noiThuongTru;
    }

    public void setNoiThuongTru(String noiThuongTru) {
        this.noiThuongTru = noiThuongTru;
    }

    public String getNoiOHienTai() {
        return noiOHienTai;
    }

    public void setNoiOHienTai(String noiOHienTai) {
        this.noiOHienTai = noiOHienTai;
    }
}
