package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

/**
 * Trạng thái hoạt động của dịch vụ trong {@link com.company.clinicportal.entity.DmDichVu}.
 * Lưu trong DB dưới dạng {@code VARCHAR} (id tiếng Việt không dấu).
 */
public enum TrangThaiDichVu implements EnumClass<String> {

    HOAT_DONG("hoat_dong"),
    DUNG_HOAT_DONG("dung_hoat_dong");

    private final String id;

    TrangThaiDichVu(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static TrangThaiDichVu fromId(String id) {
        for (TrangThaiDichVu at : TrangThaiDichVu.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
