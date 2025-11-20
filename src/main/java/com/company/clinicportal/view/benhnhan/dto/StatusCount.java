package com.company.clinicportal.view.benhnhan.dto;

import io.jmix.core.metamodel.annotation.JmixEntity;

@JmixEntity(name = "clinicportal_StatusCount")
public class StatusCount {
    private String trangThai;
    private Integer soLuong;

    public StatusCount() {
    }

    public StatusCount(String trangThai, Integer soLuong) {
        this.trangThai = trangThai;
        this.soLuong = soLuong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }
}



