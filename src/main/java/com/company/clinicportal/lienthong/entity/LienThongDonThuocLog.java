package com.company.clinicportal.lienthong.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

import java.util.Date;
import java.util.UUID;

/**
 * Audit log cho mỗi request/response gọi API liên thông đơn thuốc.
 * KHÔNG lưu token/password/CCCD/signature plaintext.
 */
@JmixEntity
@Entity(name = "ltcs_LienThongDonThuocLog")
@Table(name = "lt_don_thuoc_log", indexes = {
        @Index(name = "idx_lt_dtl_created", columnList = "created_at"),
        @Index(name = "idx_lt_dtl_api", columnList = "api"),
        @Index(name = "idx_lt_dtl_madt", columnList = "ma_don_thuoc")
})
public class LienThongDonThuocLog {

    @Id
    @Column(name = "id")
    @JmixGeneratedValue
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    /** Đăng nhập cơ sở / thêm bác sĩ / xoá bác sĩ / đăng nhập bác sĩ / gửi đơn thuốc. */
    @Column(name = "api", length = 64)
    private String api;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "ma_don_thuoc", length = 32)
    private String maDonThuoc;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "outcome", length = 16)
    private String outcome;

    /** Masked JSON request. */
    @Column(name = "req_masked", length = 4000)
    private String requestMasked;

    /** Masked JSON response hoặc thông điệp lỗi. */
    @Column(name = "resp_masked", length = 4000)
    private String responseMasked;

    @Column(name = "retryable")
    private Boolean retryable;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getApi() { return api; }
    public void setApi(String api) { this.api = api; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) { this.maDonThuoc = maDonThuoc; }
    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }
    public Long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public String getRequestMasked() { return requestMasked; }
    public void setRequestMasked(String requestMasked) { this.requestMasked = requestMasked; }
    public String getResponseMasked() { return responseMasked; }
    public void setResponseMasked(String responseMasked) { this.responseMasked = responseMasked; }
    public Boolean getRetryable() { return retryable; }
    public void setRetryable(Boolean retryable) { this.retryable = retryable; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}