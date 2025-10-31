package com.company.clinicportal.view.phieudieutri;

import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "phieu-dieu-tris/:id", layout = MainView.class)
@ViewController(id = "PhieuDieuTri.detail")
@ViewDescriptor(path = "phieu-dieu-tri-detail-view.xml")
@EditedEntityContainer("phieuDieuTriDc")
public class PhieuDieuTriDetailView extends StandardDetailView<PhieuDieuTri> {
}