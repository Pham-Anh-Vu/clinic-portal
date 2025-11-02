package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum NhomDichVu implements EnumClass<String> {

    DIEN_TRI_LIEU("dien_tri_lieu"),
    KEO_GIAN("keo_gian"),
    VAN_DONG_TRI_LIEU("van_dong_tri_lieu"),
    TAP_PHCN("tap_phcn");

    private final String id;

    NhomDichVu(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static NhomDichVu fromId(String id) {
        for (NhomDichVu at : NhomDichVu.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}