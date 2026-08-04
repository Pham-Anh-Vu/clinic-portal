package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

/**
 * Giới tính mapping sang mã API liên thông 808/QĐ-BYT:
 * <ul>
 *     <li>1 = Nam</li>
 *     <li>2 = Nữ</li>
 *     <li>3 = Khác</li>
 * </ul>
 *
 * Lưu ý: id enum là mã hiển thị ("Nam", "Nữ", "Khác") để không phá UI hiện có.
 * Mapping sang API dùng {@link #getApiCode()} ở Giai đoạn 4.
 */
public enum GioiTinh implements EnumClass<String> {

    NAM("Nam"),
    NU("Nữ"),
    KHAC("Khác");

    private final String id;

    GioiTinh(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Trả về mã mapping 1/2/3 theo tài liệu API.
     */
    public String getApiCode() {
        switch (this) {
            case NAM: return "1";
            case NU: return "2";
            case KHAC: return "3";
            default: return null;
        }
    }

    /**
     * Map mã API 1/2/3 sang enum; trả null nếu không khớp.
     */
    @Nullable
    public static GioiTinh fromApiCode(String apiCode) {
        if (apiCode == null) return null;
        switch (apiCode.trim()) {
            case "1": return NAM;
            case "2": return NU;
            case "3": return KHAC;
            default: return null;
        }
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
