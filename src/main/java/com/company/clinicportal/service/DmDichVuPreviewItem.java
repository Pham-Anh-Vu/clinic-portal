package com.company.clinicportal.service;

import com.company.clinicportal.entity.DmDichVu;
import com.company.clinicportal.enumentity.NhomDichVu;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.apache.xpath.operations.Bool;

@JmixEntity
public class DmDichVuPreviewItem {
    private Integer rowNumber;
    private DmDichVu entity;
    private String errorMessage;
    private Boolean hasError;

    public DmDichVuPreviewItem(Integer rowNumber, DmDichVu entity) {
        this.rowNumber = rowNumber;
        this.entity = entity;
        this.hasError = false;
    }

    public DmDichVuPreviewItem(Integer rowNumber, String errorMessage) {
        this.rowNumber = rowNumber;
        this.errorMessage = errorMessage;
        this.hasError = true;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public DmDichVu getEntity() {
        return entity;
    }

    public void setEntity(DmDichVu entity) {
        this.entity = entity;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Boolean isHasError() {
        return hasError;
    }

    public void setHasError(Boolean hasError) {
        this.hasError = hasError;
    }

    // Helper methods for display
    public String getTenDichVu() {
        return entity != null ? entity.getTenDichVu() : "";
    }

    public String getNhomDichVu() {
        if (entity != null && entity.getNhomDichVu() != null) {
            return entity.getNhomDichVu().getId();
        }
        return "";
    }

    public String getMoTa() {
        return entity != null ? entity.getMoTa() : "";
    }

    public Long getGia() {
        return entity != null ? entity.getGia() : null;
    }
}

