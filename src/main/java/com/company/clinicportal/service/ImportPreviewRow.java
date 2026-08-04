package com.company.clinicportal.service;

import io.jmix.core.metamodel.annotation.JmixEntity;

/**
 * Hàng preview cho dialog import danh mục (dùng chung cho DmThuoc / Icd10).
 * Phải là {@link JmixEntity} để {@code <collection>} trong XML dialog có MetaClass
 * (POJO thường sẽ gây {@code MetaClass not found} lúc load view).
 */
@JmixEntity
public class ImportPreviewRow {

    private Integer rowNumber;
    private String code;
    private String status;
    private String message;
    private Boolean ok;

    public ImportPreviewRow() {
    }

    public static ImportPreviewRow ok(Integer rowNumber, String code) {
        ImportPreviewRow r = new ImportPreviewRow();
        r.rowNumber = rowNumber;
        r.code = code;
        r.ok = Boolean.TRUE;
        r.status = "OK";
        r.message = "";
        return r;
    }

    public static ImportPreviewRow error(Integer rowNumber, String message) {
        ImportPreviewRow r = new ImportPreviewRow();
        r.rowNumber = rowNumber;
        r.code = "";
        r.ok = Boolean.FALSE;
        r.status = "Lỗi";
        r.message = message;
        return r;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getOk() {
        return ok;
    }

    public void setOk(Boolean ok) {
        this.ok = ok;
    }
}
