package com.company.clinicportal.view.lichsuthanhtoan;

import com.company.clinicportal.entity.LichSuThanhToan;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "lich-su-thanh-toans/:id", layout = MainView.class)
@ViewController(id = "LichSuThanhToan.detail")
@ViewDescriptor(path = "lich-su-thanh-toan-detail-view.xml")
@EditedEntityContainer("lichSuThanhToanDc")
public class LichSuThanhToanDetailView extends StandardDetailView<LichSuThanhToan> {
}