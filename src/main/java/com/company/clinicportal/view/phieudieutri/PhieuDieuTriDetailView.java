package com.company.clinicportal.view.phieudieutri;

import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.view.donthuoc.DonThuocDetailView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "phieu-dieu-tris/:id", layout = MainView.class)
@ViewController(id = "PhieuDieuTri.detail")
@ViewDescriptor(path = "phieu-dieu-tri-detail-view.xml")
@EditedEntityContainer("phieuDieuTriDc")
public class PhieuDieuTriDetailView extends StandardDetailView<PhieuDieuTri> {

    @Autowired
    private DialogWindows dialogWindows;

    @ViewComponent
    private JmixButton keDonThuocButton;

    @Subscribe("keDonThuocButton")
    public void onKeDonThuocClick(com.vaadin.flow.component.ClickEvent<JmixButton> e) {
        PhieuDieuTri pdt = getEditedEntity();
        if (pdt == null) return;
        // Mở DonThuocDetailView, view tự tạo nháp khi có setPhieuDieuTriIdParam
        dialogWindows.view(this, DonThuocDetailView.class)
                .withViewConfigurer(v -> ((DonThuocDetailView) v).setPhieuDieuTriIdParam(pdt.getId()))
                .open();
    }
}
