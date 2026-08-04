package com.company.clinicportal.lienthong.dto;

import java.util.List;

/**
 * Response chuẩn cho /api/v1/gui-don-thuoc.
 *
 * <p>BYT có thể trả về nhiều trường khác (don_thuoc_id, ma_don_thuoc_qg, ...).
 * Các trường này được log đầy đủ qua {@code DonThuocAuditService}.</p>
 */
public class GuiDonThuocResponse {

    public boolean success;
    public String error_code;
    public String message;
    public String ma_don_thuoc_qg;
    public String don_thuoc_id;
    public List<ValidationError> errors;

    public static class ValidationError {
        public String field;
        public String code;
        public String message;
    }
}
