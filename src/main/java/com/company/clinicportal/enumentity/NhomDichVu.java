package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum NhomDichVu implements EnumClass<String> {

    VAT_LY_TRI_LIEU("vat_ly_tri_lieu"),
    VAN_DONG_TRI_LIEU("van_dong_tri_lieu"),
    KEO_NAN_TRI_LIEU("keo_nan_tri_lieu"),
    XOA_BOP_TRI_LIEU("xoa_bop_tri_lieu"),
    KHAM_LUONG_GIA("kham_luong_gia");

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