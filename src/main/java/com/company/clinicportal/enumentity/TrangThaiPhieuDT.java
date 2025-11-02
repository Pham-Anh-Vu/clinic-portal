package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum TrangThaiPhieuDT implements EnumClass<String> {

    DA_DT("Đã điều trị"),
    DANG_DT("Đang điều trị"),
    KHONG_DT("Không điều trị");

    private final String id;

    TrangThaiPhieuDT(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static TrangThaiPhieuDT fromId(String id) {
        for (TrangThaiPhieuDT at : TrangThaiPhieuDT.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}