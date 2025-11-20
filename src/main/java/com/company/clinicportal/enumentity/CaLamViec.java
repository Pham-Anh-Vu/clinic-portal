package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum CaLamViec implements EnumClass<String> {

    TOI("toi"),
    SANG("sang");

    private final String id;

    CaLamViec(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static CaLamViec fromId(String id) {
        for (CaLamViec at : CaLamViec.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}