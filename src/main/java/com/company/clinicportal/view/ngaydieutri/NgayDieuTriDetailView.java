package com.company.clinicportal.view.ngaydieutri;

import com.company.clinicportal.entity.NgayDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "ngay-dieu-tris/:id", layout = MainView.class)
@ViewController(id = "NgayDieuTri.detail")
@ViewDescriptor(path = "ngay-dieu-tri-detail-view.xml")
@EditedEntityContainer("ngayDieuTriDc")
public class NgayDieuTriDetailView extends StandardDetailView<NgayDieuTri> {
}