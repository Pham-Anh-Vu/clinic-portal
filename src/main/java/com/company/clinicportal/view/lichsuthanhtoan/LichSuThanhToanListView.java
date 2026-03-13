package com.company.clinicportal.view.lichsuthanhtoan;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.LichSuThanhToan;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;


@Route(value = "lich-su-thanh-toans", layout = MainView.class)
@ViewController(id = "LichSuThanhToan.list")
@ViewDescriptor(path = "lich-su-thanh-toan-list-view.xml")
@LookupComponent("lichSuThanhToansDataGrid")
@DialogMode(width = "64em")
public class LichSuThanhToanListView extends StandardListView<LichSuThanhToan> {
    private BenhNhan idBenhNhan;

    @ViewComponent
    private CollectionLoader<LichSuThanhToan> lichSuThanhToansDl;

    public void setIdBenhNhan(BenhNhan idBenhNhan) {
        this.idBenhNhan = idBenhNhan;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if (idBenhNhan != null) {
            lichSuThanhToansDl.setParameter("idBenhNhan", idBenhNhan);
            lichSuThanhToansDl.load();
        }
    }
}