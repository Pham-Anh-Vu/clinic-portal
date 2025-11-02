package com.company.clinicportal.view.buoidieutri;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;

import java.util.Date;


@Route(value = "buoi-dieu-tris", layout = MainView.class)
@ViewController(id = "BuoiDieuTri.list")
@ViewDescriptor(path = "buoi-dieu-tri-list-view.xml")
@LookupComponent("buoiDieuTrisDataGrid")
@DialogMode(width = "64em")
public class BuoiDieuTriListView extends StandardListView<BuoiDieuTri> {
    public Long idChiTietDichVu = null;

    @ViewComponent
    private CollectionLoader<BuoiDieuTri> buoiDieuTrisDl;

    public void setIdChiTietDichVu(Long idChiTietDichVu) {
        this.idChiTietDichVu = idChiTietDichVu;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        buoiDieuTrisDl.setParameter("idChiTietDichVu", idChiTietDichVu);
        buoiDieuTrisDl.load();
    }
}