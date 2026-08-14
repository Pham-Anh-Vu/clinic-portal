package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

/**
 * Loại đơn thuốc theo Quyết định 808/QĐ-BYT.
 * Mapping mã API "loai_don_thuoc":
 * <ul>
 *     <li>c - Đơn thuốc cơ bản (thường)</li>
 *     <li>h - Đơn thuốc hướng tâm thần và thuốc tiền chất</li>
 *     <li>n - Đơn thuốc gây nghiện</li>
 *     <li>y - Đơn thuốc y học cổ truyền</li>
 * </ul>
 */
public enum LoaiDon implements EnumClass<String> {

    THUONG("Thuong", "Đơn thuốc thông thường", "c"),
    HUONG_TAM_THAN("HuongTamThan", "Đơn thuốc hướng tâm thần/tiền chất", "h"),
    GAY_NGHIEN("GayNghien", "Đơn thuốc gây nghiện", "n"),
    Y_HOC_CO_TRUYEN("YHocCoTruyen", "Đơn thuốc y học cổ truyền", "y");

    private final String id;
    private final String tenHienThi;
    private final String apiCode;

    LoaiDon(String id, String tenHienThi, String apiCode) {
        this.id = id;
        this.tenHienThi = tenHienThi;
        this.apiCode = apiCode;
    }

    public String getId() { return id; }
    public String getTenHienThi() { return tenHienThi; }
    public String getApiCode() { return apiCode; }

    @Nullable
    public static LoaiDon fromId(String id) {
        if (id == null) return null;
        for (LoaiDon v : values()) {
            if (v.id.equalsIgnoreCase(id)) return v;
        }
        return null;
    }

    @Nullable
    public static LoaiDon fromApiCode(String code) {
        if (code == null) return null;
        for (LoaiDon v : values()) {
            if (v.apiCode.equalsIgnoreCase(code.trim())) return v;
        }
        return null;
    }
}
