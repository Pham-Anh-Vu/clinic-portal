package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum ChucVu implements EnumClass<String> {

    KTV("Kỹ thuật viên"),
    HCNV("HC-NV"),
    MKT("mkt"),
    BS("Bác sĩ");

    private final String id;

    ChucVu(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ChucVu fromId(String id) {
        for (ChucVu at : ChucVu.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}