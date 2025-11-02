package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum TrangThaiBuoiDieuTri implements EnumClass<String> {

    CHUA_THUC_HIEN("chua_thuc_hien"),
    DA_THUC_HIEN("da_thuc_hien"),
    DA_HUY("da_huy");

    private final String id;

    TrangThaiBuoiDieuTri(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static TrangThaiBuoiDieuTri fromId(String id) {
        for (TrangThaiBuoiDieuTri at : TrangThaiBuoiDieuTri.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}