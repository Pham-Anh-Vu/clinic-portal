package com.company.clinicportal.view.lichsuthanhtoan;

import com.company.clinicportal.entity.LichSuThanhToan;
import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "lich-su-thanh-toans/:id", layout = MainView.class)
@ViewController(id = "LichSuThanhToan.detail")
@ViewDescriptor(path = "lich-su-thanh-toan-detail-view.xml")
@EditedEntityContainer("lichSuThanhToanDc")
public class LichSuThanhToanDetailView extends StandardDetailView<LichSuThanhToan> {
    private PhieuDieuTri phieuDieuTri;
    @Autowired
    private DataManager dataManager;

    public void setPhieuDieuTri(PhieuDieuTri phieuDieuTri) {
        this.phieuDieuTri = phieuDieuTri;
    }

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        if(phieuDieuTri != null) getEditedEntity().setIdPhieuDieuTri(phieuDieuTri);
    }
}