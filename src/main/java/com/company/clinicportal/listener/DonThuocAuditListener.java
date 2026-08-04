package com.company.clinicportal.listener;

import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.entity.DonThuocChanDoan;
import com.company.clinicportal.entity.DonThuocChiTiet;
import com.company.clinicportal.entity.DonThuocDotDung;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.event.EntityLoadingEvent;
import io.jmix.core.event.EntitySavingEvent;

import java.util.Date;

/**
 * Jmix listener gắn audit field (createdBy, createdAt, updatedBy, updatedAt) cho
 * DonThuoc + các entity con. Listener thực sự là Jmix Platform Listener
 * được trigger qua các event của Jmix DataManager.
 *
 * <p>Nguyên tắc: nếu entity đã có sẵn CreatedBy/UpdatedAt thì không đè
 * (cho phép service set khi import dữ liệu cũ). Nếu chưa có thì điền từ
 * currentUser + new Date().</p>
 */
@Component
public class DonThuocAuditListener {

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @EventListener
    public void onDonThuocSaving(EntitySavingEvent<DonThuoc> event) {
        DonThuoc e = event.getEntity();
        if (e.getCreatedAt() == null) e.setCreatedAt(new Date());
        if (e.getCreatedById() == null) e.setCreatedById(currentUserId());
        e.setUpdatedAt(new Date());
        e.setUpdatedById(currentUserId());
    }

    @EventListener
    public void onDonThuocChanged(EntityChangedEvent<DonThuoc> event) {
        if (event.getType() == EntityChangedEvent.Type.UPDATED) {
            // Đã có EntitySavingEvent; ở đây chỉ đánh dấu log nếu cần.
        }
    }

    @EventListener
    public void onChiTietSaving(EntitySavingEvent<DonThuocChiTiet> event) {
        DonThuocChiTiet e = event.getEntity();
        if (e.getCreatedAt() == null) e.setCreatedAt(new Date());
        if (e.getCreatedById() == null) e.setCreatedById(currentUserId());
    }

    @EventListener
    public void onChanDoanSaving(EntitySavingEvent<DonThuocChanDoan> event) {
        DonThuocChanDoan e = event.getEntity();
        if (e.getCreatedAt() == null) e.setCreatedAt(new Date());
        if (e.getCreatedById() == null) e.setCreatedById(currentUserId());
    }

    @EventListener
    public void onDotDungSaving(EntitySavingEvent<DonThuocDotDung> event) {
        DonThuocDotDung e = event.getEntity();
        if (e.getCreatedAt() == null) e.setCreatedAt(new Date());
        if (e.getCreatedById() == null) e.setCreatedById(currentUserId());
    }

    private Long currentUserId() {
        try {
            Object principal = currentAuthentication.getUser();
            if (principal == null) return 0L;
            if (principal instanceof Number n) return n.longValue();
            try {
                return Long.parseLong(principal.toString());
            } catch (NumberFormatException ex) {
                return 0L;
            }
        } catch (Exception e) {
            return 0L;
        }
    }
}
