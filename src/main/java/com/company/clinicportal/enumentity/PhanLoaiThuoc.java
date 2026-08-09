package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

/**
 * Phân loại danh mục thuốc / vật tư trong {@link com.company.clinicportal.entity.DmThuoc}.
 * Lưu trong DB dưới dạng {@code VARCHAR} (id là tiếng Việt có dấu).
 */
public enum PhanLoaiThuoc implements EnumClass<String> {

    THUOC("Thuốc"),
    MY_PHAM("Mỹ phẩm"),
    TPCN("TPCN"),
    VTYT("VTYT");

    private final String id;

    PhanLoaiThuoc(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static PhanLoaiThuoc fromId(String id) {
        for (PhanLoaiThuoc at : PhanLoaiThuoc.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
