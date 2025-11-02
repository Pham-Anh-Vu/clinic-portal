package com.company.clinicportal.view.buoidieutri;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "thu-thuats/:id", layout = MainView.class)
@ViewController(id = "ThuThuatdetail")
@ViewDescriptor(path = "thu-thuat-detail-view.xml")
@EditedEntityContainer("buoiDieuTriDc")
public class ThuThuatDetailView extends StandardDetailView<BuoiDieuTri> {
}