package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum NguonBenhNhan implements EnumClass<String> {

    KHACH_VANG_LAI("Khách vãng lai"),
    PAGE("Page"),
    WEBSITE("Website"),
    SEEDING("Seeding"),
    HOTLINE("hotline");

    private final String id;

    NguonBenhNhan(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static NguonBenhNhan fromId(String id) {
        for (NguonBenhNhan at : NguonBenhNhan.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}