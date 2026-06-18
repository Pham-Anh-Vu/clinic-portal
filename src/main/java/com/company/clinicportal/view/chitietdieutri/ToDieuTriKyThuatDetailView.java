package com.company.clinicportal.view.chitietdieutri;

import com.company.clinicportal.entity.ToDieuTriKyThuat;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "to-dieu-tri-ky-thuats/:id", layout = MainView.class)
@ViewController(id = "ToDieuTriKyThuat.detail")
@ViewDescriptor(path = "to-dieu-tri-ky-thuat-detail-view.xml")
@EditedEntityContainer("toDieuTriKyThuatDc")
@DialogMode(width = "32em", height = "AUTO")
public class ToDieuTriKyThuatDetailView extends StandardDetailView<ToDieuTriKyThuat> {
}
