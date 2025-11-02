package com.company.clinicportal.view.chitietdieutri;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Date;

@Route(value = "phi-chi-dinhs/:id", layout = MainView.class)
@ViewController(id = "PhieuChiDinh.detail")
@ViewDescriptor(path = "phieu-chi-dinh-detail-view.xml")
@EditedEntityContainer("chiTietDieuTriDc")
public class PhieuChiDinhDetailView extends StandardDetailView<ChiTietDieuTri> {
    @ViewComponent
    private TypedDateTimePicker<Date> ngayChiDinhField;

    public Long idBenhNhan;
    @Autowired
    private DataManager dataManager;

    public void setIdBenhNhan(Long idBenhNhan) {
        this.idBenhNhan = idBenhNhan;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        ngayChiDinhField.setValue(LocalDateTime.now());

        if (idBenhNhan != null) {
            BenhNhan benhNhan = dataManager.load(BenhNhan.class).id(idBenhNhan).optional().orElse(null);
            getEditedEntity().setIdBenhNhan(benhNhan);
        }
    }
}