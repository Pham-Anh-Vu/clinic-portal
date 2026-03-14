package com.company.clinicportal.view.lichsuthanhtoan;

import com.company.clinicportal.entity.LichSuThanhToan;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Route(value = "lich-su-thanh-toans/:id", layout = MainView.class)
@ViewController(id = "LichSuThanhToan.detail")
@ViewDescriptor(path = "lich-su-thanh-toan-detail-view.xml")
@EditedEntityContainer("lichSuThanhToanDc")
public class LichSuThanhToanDetailView extends StandardDetailView<LichSuThanhToan> {
    private ChiTietDieuTri chiTietDieuTri;
    @Autowired
    private EntityStates entityStates;

    public void setChiTietDieuTri(ChiTietDieuTri chiTietDieuTri) {
        this.chiTietDieuTri = chiTietDieuTri;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if (entityStates.isNew(getEditedEntity())
                && getEditedEntity().getThanhToanLuc() == null) {
            getEditedEntity().setThanhToanLuc(new Date());
        }
        if (entityStates.isNew(getEditedEntity()) && chiTietDieuTri != null) {
            getEditedEntity().setIdChiTietDieuTri(chiTietDieuTri);
        }
    }

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        if (chiTietDieuTri != null) {
            getEditedEntity().setIdChiTietDieuTri(chiTietDieuTri);
        }
    }
}