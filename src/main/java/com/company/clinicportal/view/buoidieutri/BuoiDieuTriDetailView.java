package com.company.clinicportal.view.buoidieutri;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "buoi-dieu-tris/:id", layout = MainView.class)
@ViewController(id = "BuoiDieuTri.detail")
@ViewDescriptor(path = "buoi-dieu-tri-detail-view.xml")
@EditedEntityContainer("buoiDieuTriDc")
public class BuoiDieuTriDetailView extends StandardDetailView<BuoiDieuTri> {
}