package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum HinhThuc implements EnumClass<String> {

    DAT_LICH("Đặt lịch"),
    KHAM_TRUC_TIEP("Khám trực tiếp");

    private final String id;

    HinhThuc(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static HinhThuc fromId(String id) {
        for (HinhThuc at : HinhThuc.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}