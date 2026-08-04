package com.company.clinicportal.lienthong.entity;

import com.company.clinicportal.lienthong.SecretCipher;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import java.util.Date;
import java.util.UUID;

/**
 * Cấu hình cơ sở khám chữa bệnh dùng để liên thông đơn thuốc quốc gia.
 * Lưu ý: KHÔNG lưu plaintext secret. Password/mã bí mật lưu dạng mã hoá (AES-GCM)
 * qua {@link SecretCipher}.
 */
@JmixEntity
@Entity(name = "ltcs_CoSoKhamChuaBenhLienThong")
@Table(name = "lt_co_so_kcb", indexes = {
        @jakarta.persistence.Index(name = "idx_lt_cs_kcb_mlt", columnList = "ma_lien_thong", unique = true)
})
public class CoSoKhamChuaBenhLienThong {

    @Id
    @Column(name = "id")
    @JmixGeneratedValue
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "ma_lien_thong", nullable = false, length = 64)
    private String maLienThong;

    /** Mã cơ sở KCB theo quy định (5 ký tự) dùng để sinh mã đơn. */
    @Column(name = "ma_co_so_kcb", length = 16)
    private String maCoSoKcb;

    @Column(name = "ten_co_so", length = 255)
    private String tenCoSo;

    /** Password đã mã hoá (AES-GCM). */
    @Column(name = "password_cipher", length = 512)
    private String passwordCipher;

    /** Token cơ sở đã mã hoá (cache nhanh giữa các lần restart). */
    @Column(name = "token_cipher", length = 2048)
    private String tokenCipher;

    @Column(name = "token_expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date tokenExpiresAt;

    @Column(name = "environment", length = 32)
    private String environment;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @InstanceName
    public String getInstanceName() {
        return (tenCoSo == null || tenCoSo.isBlank() ? maLienThong : tenCoSo) + " [" + (environment == null ? "?" : environment) + "]";
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getMaLienThong() { return maLienThong; }
    public void setMaLienThong(String maLienThong) { this.maLienThong = maLienThong; }
    public String getMaCoSoKcb() { return maCoSoKcb; }
    public void setMaCoSoKcb(String maCoSoKcb) { this.maCoSoKcb = maCoSoKcb; }
    public String getTenCoSo() { return tenCoSo; }
    public void setTenCoSo(String tenCoSo) { this.tenCoSo = tenCoSo; }
    public String getPasswordCipher() { return passwordCipher; }
    public void setPasswordCipher(String passwordCipher) { this.passwordCipher = passwordCipher; }
    public String getTokenCipher() { return tokenCipher; }
    public void setTokenCipher(String tokenCipher) { this.tokenCipher = tokenCipher; }
    public Date getTokenExpiresAt() { return tokenExpiresAt; }
    public void setTokenExpiresAt(Date tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}