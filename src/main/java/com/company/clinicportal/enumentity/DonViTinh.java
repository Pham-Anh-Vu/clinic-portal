package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

/**
 * Đơn vị tính phổ biến. API chấp nhận chuỗi tự do; enum này dùng để gợi ý khi kê đơn.
 */
public enum DonViTinh implements EnumClass<String> {

    VIEN("Viên"),
    ONG("Ống"),
    GOI("Gói"),
    CHAI("Chai"),
    HOP("Hộp"),
    TUYP("Tuýp"),
    ML("ml"),
    G("g"),
    MG("mg"),
    LIEU("Liều"),
    KHAC("Khác");

    private final String id;

    DonViTinh(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public String getTenHienThi() { return id; }

    @Nullable
    public static DonViTinh fromId(String id) {
        if (id == null) return null;
        for (DonViTinh v : values()) {
            if (v.id.equalsIgnoreCase(id.trim())) return v;
        }
        return null;
    }
}
