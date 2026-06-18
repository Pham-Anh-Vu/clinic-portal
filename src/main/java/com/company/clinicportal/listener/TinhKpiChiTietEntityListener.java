package com.company.clinicportal.listener;

import com.company.clinicportal.entity.NhanSu;
import com.company.clinicportal.entity.TinhKpiChiTiet;
import com.company.clinicportal.service.TinhKpiChiTietService;
import io.jmix.core.Id;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TinhKpiChiTietEntityListener {

    @Autowired
    private TinhKpiChiTietService tinhKpiChiTietService;

    @EventListener
    public void onTinhKpiChiTietChanged(EntityChangedEvent<TinhKpiChiTiet> event) {
        if (event.getType() != EntityChangedEvent.Type.DELETED) {
            return;
        }

        Long nhanSuPk = resolveNhanSuId(event);
        Date ngayThucHien = event.getChanges().getOldValue("ngayThucHien");
        if (nhanSuPk == null || ngayThucHien == null) {
            return;
        }

        tinhKpiChiTietService.recalcTinhKpiForNhanSuAndDate(nhanSuPk, ngayThucHien);
    }

    private Long resolveNhanSuId(EntityChangedEvent<TinhKpiChiTiet> event) {
        Id<?> refId = event.getChanges().getOldReferenceId("idNhanSu");
        if (refId != null && refId.getValue() instanceof Long id) {
            return id;
        }

        Object oldValue = event.getChanges().getOldValue("idNhanSu");
        if (oldValue instanceof NhanSu nhanSu) {
            return nhanSu.getId();
        }

        Object entityId = EntityValues.getId(oldValue);
        if (entityId instanceof Long id) {
            return id;
        }
        if (entityId instanceof Id<?> id) {
            Object value = id.getValue();
            return value instanceof Long longId ? longId : null;
        }
        return null;
    }
}
