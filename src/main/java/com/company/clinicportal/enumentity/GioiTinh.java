package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum GioiTinh implements EnumClass<String> {

    NAM("Nam"),
    NU("Nữ");

    private final String id;

    GioiTinh(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static GioiTinh fromId(String id) {
        for (GioiTinh at : GioiTinh.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}