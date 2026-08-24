package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

/**
 * Hình thức điều trị kê đơn, mapping mã API "hinh_thuc_dieu_tri":
 * <ul>
 *     <li>1 - Ngoại trú</li>
 *     <li>2 - Nội trú</li>
 *     <li>3 - Điều trị ngoại trú ban ngày</li>
 *     <li>4 - Khác</li>
 * </ul>
 */
public enum HinhThucDieuTri implements EnumClass<String> {

    NGOAI_TRU("2", "Ngoại trú"),
    NOI_TRU("1", "Nội trú"),
    NGOAI_TRU_BAN_NGAY("3", "Ngoại trú ban ngày"),
    KHAC("4", "Khác");

    private final String apiCode;
    private final String tenHienThi;

    HinhThucDieuTri(String apiCode, String tenHienThi) {
        this.apiCode = apiCode;
        this.tenHienThi = tenHienThi;
    }

    public String getId() { return apiCode; }
    public String getTenHienThi() { return tenHienThi; }
    public String getApiCode() { return apiCode; }

    @Nullable
    public static HinhThucDieuTri fromId(String id) {
        if (id == null) return null;
        for (HinhThucDieuTri v : values()) {
            if (v.apiCode.equals(id.trim())) return v;
        }
        return null;
    }

    @Nullable
    public static HinhThucDieuTri fromApiCode(String code) {
        return fromId(code);
    }
}
