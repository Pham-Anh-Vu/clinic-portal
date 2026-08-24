package com.company.clinicportal.service;

/**
 * Một lỗi validation gắn với một field trên view.
 *
 * @param fieldKey  id của component trong view (vd: "hoVaTenField"). Có thể null
 *                  nếu lỗi không gắn với field cụ thể nào (khi đó view sẽ hiển thị
 *                  như notification thay vì inline).
 * @param messageKey message key trong messages bundle (vd: "bn.hoVaTenRequired").
 */
public record ValidationError(String fieldKey, String messageKey) {
    public static ValidationError of(String fieldKey, String messageKey) {
        return new ValidationError(fieldKey, messageKey);
    }

    public static ValidationError withoutField(String messageKey) {
        return new ValidationError(null, messageKey);
    }
}