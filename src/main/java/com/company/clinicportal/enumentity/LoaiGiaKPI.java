package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum LoaiGiaKPI implements EnumClass<String> {

    SANG("sang"),
    TOI("toi");

    private final String id;

    LoaiGiaKPI(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static LoaiGiaKPI fromId(String id) {
        for (LoaiGiaKPI at : LoaiGiaKPI.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}