package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;
import jakarta.annotation.Nullable;

public enum TinhTheoGia implements EnumClass<String> {

    GOI("Gói"),
    LE("Lẻ");

    private final String id;

    TinhTheoGia(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static TinhTheoGia fromId(String id) {
        for (TinhTheoGia item : TinhTheoGia.values()) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }
}