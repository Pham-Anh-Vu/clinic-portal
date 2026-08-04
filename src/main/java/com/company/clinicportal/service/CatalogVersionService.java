package com.company.clinicportal.service;

import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tự tăng phiên bản (phien_ban) cho các danh mục có versioning (DmThuoc, Icd10, ...).
 * Tránh hard-code MAX + 1 ở nhiều service.
 *
 * <p>Nguyên tắc: phiên bản tăng đồng bộ cho cả bảng khi import. Mỗi lần import thành công
 * sẽ ghi {@code version_identifier} trong metadata mới nhất (1 dòng cho mỗi bảng) hoặc
 * fallback bằng {@code MAX(phien_ban)} nếu bảng không thiết lập bảng phiên bản.</p>
 */
@Service
public class CatalogVersionService {

    @Autowired
    private DataManager dataManager;

    /**
     * Trả về phiên bản kế tiếp = max(phien_ban) + 1; nếu bảng rỗng trả 1.
     */
    @Transactional(readOnly = true)
    public int nextVersion(String entityName, String phienBanField) {
        try {
            Number max = dataManager.loadValue(
                    "select max(e." + phienBanField + ") from " + entityName + " e",
                    Number.class).one();
            return (max == null ? 0 : max.intValue()) + 1;
        } catch (Exception ex) {
            return 1;
        }
    }
}
