package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum HinhThucLamViec implements EnumClass<String> {

    FT("fulltime"),
    PT("parttime");

    private final String id;

    HinhThucLamViec(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static HinhThucLamViec fromId(String id) {
        for (HinhThucLamViec at : HinhThucLamViec.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}