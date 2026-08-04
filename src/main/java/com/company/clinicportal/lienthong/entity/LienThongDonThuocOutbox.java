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
 * Outbox/queue bền vững cho việc gửi đơn thuốc.
 *
 * <p>Mỗi job gửi đơn = 1 row. Status vòng đời:
 * <ul>
 *     <li>PENDING: chờ worker tick kế tiếp.</li>
 *     <li>IN_PROGRESS: worker đang gọi API (có thể stale nếu crash → sweep).</li>
 *     <li>DONE: API trả 2xx.</li>
 *     <li>FAILED: lỗi không retry (4xx khác 408/429, validation).</li>
 *     <li>RETRY_SCHEDULED: lỗi retryable, sẽ thử lại sau {@code nextAttemptAt}.</li>
 * </ul>
 *
 * <p>Idempotency: cột {@code idempotency_key} UNIQUE - nếu 1 request trùng key
 * đã chạy trước, không gọi lại.</p>
 */
@JmixEntity
@Entity(name = "ltcs_LienThongDonThuocOutbox")
@Table(name = "lt_don_thuoc_outbox", indexes = {
        @Index(name = "idx_lt_outbox_status", columnList = "status"),
        @Index(name = "idx_lt_outbox_next", columnList = "next_attempt_at"),
        @Index(name = "idx_lt_outbox_madt", columnList = "ma_don_thuoc"),
        @Index(name = "idx_lt_outbox_api", columnList = "api")
})
public class LienThongDonThuocOutbox {

    @Id
    @Column(name = "id")
    @JmixGeneratedValue
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "api", length = 64)
    private String api;

    @Column(name = "ma_don_thuoc", length = 32)
    private String maDonThuoc;

    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "attempt")
    private Integer attempt;

    @Column(name = "max_attempt")
    private Integer maxAttempt;

    @Column(name = "next_attempt_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextAttemptAt;

    @Column(name = "locked_by", length = 64)
    private String lockedBy;

    @Column(name = "locked_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lockedAt;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "last_error", length = 1024)
    private String lastError;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getApi() { return api; }
    public void setApi(String api) { this.api = api; }
    public String getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) { this.maDonThuoc = maDonThuoc; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAttempt() { return attempt; }
    public void setAttempt(Integer attempt) { this.attempt = attempt; }
    public Integer getMaxAttempt() { return maxAttempt; }
    public void setMaxAttempt(Integer maxAttempt) { this.maxAttempt = maxAttempt; }
    public Date getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(Date nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
    public String getLockedBy() { return lockedBy; }
    public void setLockedBy(String lockedBy) { this.lockedBy = lockedBy; }
    public Date getLockedAt() { return lockedAt; }
    public void setLockedAt(Date lockedAt) { this.lockedAt = lockedAt; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}