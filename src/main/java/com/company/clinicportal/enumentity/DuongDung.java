package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

/**
 * Đường dùng thuốc, mapping mã API "duong_dung":
 * Ví dụ: 1=Uống, 2=Tiêm tĩnh mạch, 3=Tiêm bắp, 4=Tiêm dưới da, 5=Ngoài da, 6=Nhỏ mắt, 7=Xông, 8=Khác.
 *
 * Đây chỉ là tập phổ biến; API cho phép mở rộng.
 */
public enum DuongDung implements EnumClass<String> {

    UONG("1", "Uống"),
    TIEM_TINH_MACH("2", "Tiêm tĩnh mạch"),
    TIEM_BAP("3", "Tiêm bắp"),
    TIEM_DUOI_DA("4", "Tiêm dưới da"),
    NGOAI_DA("5", "Ngoài da"),
    NHO_MAT("6", "Nhỏ mắt"),
    XONG("7", "Xông"),
    KHAC("8", "Khác");

    private final String apiCode;
    private final String tenHienThi;

    DuongDung(String apiCode, String tenHienThi) {
        this.apiCode = apiCode;
        this.tenHienThi = tenHienThi;
    }

    public String getId() { return apiCode; }
    public String getTenHienThi() { return tenHienThi; }
    public String getApiCode() { return apiCode; }

    @Nullable
    public static DuongDung fromId(String id) {
        if (id == null) return null;
        for (DuongDung v : values()) {
            if (v.apiCode.equals(id.trim())) return v;
        }
        return null;
    }

    @Nullable
    public static DuongDung fromApiCode(String code) {
        return fromId(code);
    }
}
