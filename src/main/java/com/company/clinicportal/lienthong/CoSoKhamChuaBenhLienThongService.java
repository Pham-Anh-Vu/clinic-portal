package com.company.clinicportal.lienthong;

import com.company.clinicportal.lienthong.entity.CoSoKhamChuaBenhLienThong;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

/**
 * Helper xử lý trước/sau khi lưu {@link CoSoKhamChuaBenhLienThong}: mã hoá password
 * dạng plaintext sang {@code passwordCipher}; KHÔNG ghi đè nếu đã mã hoá.
 */
@Component("ltcs_CoSoKhamChuaBenhLienThongService")
public class CoSoKhamChuaBenhLienThongService {

    @Autowired
    private SecretCipher secretCipher;
    @Autowired
    private EntityStates entityStates;

    /** Trước khi save, nếu password hiện tại là plaintext thì mã hoá. */
    public void encryptPasswordIfNeeded(CoSoKhamChuaBenhLienThong entity, String plainPassword) {
        if (entity == null || plainPassword == null || plainPassword.isBlank()) {
            return;
        }
        entity.setPasswordCipher(secretCipher.encrypt(plainPassword));
    }

    public void touchTimestamps(CoSoKhamChuaBenhLienThong entity) {
        Date now = Date.from(Instant.now());
        if (entityStates.isNew(entity)) {
            entity.setCreatedAt(now);
        }
        entity.setUpdatedAt(now);
    }

    /**
     * Trả lại password đã giải mã để hiển thị trên UI (chỉ khi muốn cho admin xem lại).
     * Trả về null nếu chưa có/giải mã lỗi.
     */
    public String decryptPasswordOrNull(CoSoKhamChuaBenhLienThong entity) {
        if (entity == null) return null;
        return secretCipher.decrypt(entity.getPasswordCipher());
    }
}
