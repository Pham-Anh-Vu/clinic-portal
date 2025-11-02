package com.company.clinicportal.view.buoidieutri;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "thu-thuats/:id", layout = MainView.class)
@ViewController(id = "ThuThuatdetail")
@ViewDescriptor(path = "thu-thuat-detail-view.xml")
@EditedEntityContainer("buoiDieuTriDc")
@DialogMode(width = "80%", height = "100%")
public class ThuThuatDetailView extends StandardDetailView<BuoiDieuTri> {
}