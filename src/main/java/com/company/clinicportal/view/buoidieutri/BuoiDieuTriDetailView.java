package com.company.clinicportal.view.buoidieutri;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.enumentity.CaLamViec;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "buoi-dieu-tris/:id", layout = MainView.class)
@ViewController(id = "BuoiDieuTri.detail")
@ViewDescriptor(path = "buoi-dieu-tri-detail-view.xml")
@EditedEntityContainer("buoiDieuTriDc")
@DialogMode(width = "80%", height = "100%")
public class BuoiDieuTriDetailView extends StandardDetailView<BuoiDieuTri> {

    @Subscribe
    public void onBeforeSave(BeforeSaveEvent event) {
        BuoiDieuTri buoiDieuTri = getEditedEntity();
        buoiDieuTri.setCa(CaLamViec.fromGioBatDau(buoiDieuTri.getGioBatDau()));
    }
}