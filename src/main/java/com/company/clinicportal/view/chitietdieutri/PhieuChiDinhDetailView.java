package com.company.clinicportal.view.chitietdieutri;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.Icd10;
import com.company.clinicportal.entity.LichHen;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Date;

@Route(value = "phi-chi-dinhs/:id", layout = MainView.class)
@ViewController(id = "PhieuChiDinh.detail")
@ViewDescriptor(path = "phieu-chi-dinh-detail-view.xml")
@EditedEntityContainer("chiTietDieuTriDc")
@DialogMode(width = "80%", height = "100%")
public class PhieuChiDinhDetailView extends StandardDetailView<ChiTietDieuTri> {
    @ViewComponent
    private TypedDateTimePicker<Date> ngayChiDinhField;
    @ViewComponent
    private EntityPicker<Icd10> chuanDoanIcdField;

    public Long idBenhNhan;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private EntityStates entityStates;

    public void setIdBenhNhan(Long idBenhNhan) {
        this.idBenhNhan = idBenhNhan;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        ngayChiDinhField.setValue(LocalDateTime.now());

        if (idBenhNhan != null) {
            BenhNhan benhNhan = dataManager.load(BenhNhan.class).id(idBenhNhan).optional().orElse(null);
            getEditedEntity().setIdBenhNhan(benhNhan);
            applyDefaultsFromLatestAppointment();
        }
    }

    @Subscribe("chuanDoanIcdField")
    public void onChuanDoanIcdFieldValueChange(final EntityPicker.ValueChangeEvent<Icd10> event) {
        Icd10 icd = event.getValue();
        if (icd == null) {
            getEditedEntity().setChuanDoanMaIcd(null);
            getEditedEntity().setChuanDoanTenIcd(null);
            return;
        }
        getEditedEntity().setChuanDoanMaIcd(icd.getMaIcd());
        getEditedEntity().setChuanDoanTenIcd(icd.getTenBenh());
    }

    private void applyDefaultsFromLatestAppointment() {
        ChiTietDieuTri editedEntity = getEditedEntity();
        if (!entityStates.isNew(editedEntity) || idBenhNhan == null) {
            return;
        }
        boolean needDoctor = editedEntity.getIdNhanSu() == null;
        boolean needDefaultTime = editedEntity.getGioHenMacDinh() == null;
        if (!needDoctor && !needDefaultTime) {
            return;
        }

        LichHen latestAppointment = dataManager.load(LichHen.class)
                .query("select e from LichHen e where e.idBenhNhan.id = :idBenhNhan order by e.id desc")
                .parameter("idBenhNhan", idBenhNhan)
                .maxResults(1)
                .optional()
                .orElse(null);
        if (latestAppointment == null) {
            return;
        }
        if (needDoctor && latestAppointment.getIdBacSi() != null) {
            editedEntity.setIdNhanSu(latestAppointment.getIdBacSi());
        }
        if (needDefaultTime && latestAppointment.getThoiGianHen() != null) {
            editedEntity.setGioHenMacDinh(latestAppointment.getThoiGianHen());
        }
    }
}