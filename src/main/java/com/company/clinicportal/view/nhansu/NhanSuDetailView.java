package com.company.clinicportal.view.nhansu;

import com.company.clinicportal.entity.NhanSu;
import com.company.clinicportal.entity.TinhKpi;
import com.company.clinicportal.entity.TinhKpiChiTiet;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Route(value = "nhan-sus/:id", layout = MainView.class)
@ViewController(id = "NhanSu.detail")
@ViewDescriptor(path = "nhan-su-detail-view.xml")
@EditedEntityContainer("nhanSuDc")
@DialogMode(height = "100%", width = "80%")
public class NhanSuDetailView extends StandardDetailView<NhanSu> {
    @ViewComponent
    private CollectionLoader<TinhKpi> tinhKpisDl;
    @ViewComponent
    private CollectionLoader<TinhKpiChiTiet> tinhKpiChiTietsDl;
    @Autowired
    private EntityStates entityStates;
    @ViewComponent
    private VerticalLayout kpiMonth;
    @ViewComponent
    private VerticalLayout kpiDetail;
    @ViewComponent
    private TypedDatePicker<LocalDate> tuNgayFilterField;
    @ViewComponent
    private TypedDatePicker<LocalDate> denNgayFilterField;
    @ViewComponent
    private Button locKpiChiTietButton;
    @ViewComponent
    private Button xoaLocKpiChiTietButton;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if (this.isReadOnly()) {
            kpiMonth.setVisible(true);
            kpiDetail.setVisible(true);
        }

        tinhKpisDl.setParameter("idNhanSu", getEditedEntity());
        tinhKpisDl.load();

        loadTinhKpiChiTiet();
    }

    @Subscribe("locKpiChiTietButton")
    public void onLocKpiChiTietButtonClick(final com.vaadin.flow.component.ClickEvent<Button> event) {
        loadTinhKpiChiTiet();
    }

    @Subscribe("xoaLocKpiChiTietButton")
    public void onXoaLocKpiChiTietButtonClick(final com.vaadin.flow.component.ClickEvent<Button> event) {
        tuNgayFilterField.clear();
        denNgayFilterField.clear();
        loadTinhKpiChiTiet();
    }

    private void loadTinhKpiChiTiet() {
        tinhKpiChiTietsDl.setParameter("idNhanSu", getEditedEntity());

        Date fromDate = toStartOfDay(tuNgayFilterField.getValue());
        Date toDateExclusive = toStartOfNextDay(denNgayFilterField.getValue());

        tinhKpiChiTietsDl.setParameter("tuNgay", fromDate);
        tinhKpiChiTietsDl.setParameter("denNgay", toDateExclusive);
        tinhKpiChiTietsDl.load();
    }

    private Date toStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date toStartOfNextDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
