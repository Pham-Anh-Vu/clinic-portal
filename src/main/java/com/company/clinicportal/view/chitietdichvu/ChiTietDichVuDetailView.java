package com.company.clinicportal.view.chitietdichvu;

import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "chi-tiet-dich-vus/:id", layout = MainView.class)
@ViewController(id = "ChiTietDichVu.detail")
@ViewDescriptor(path = "chi-tiet-dich-vu-detail-view.xml")
@EditedEntityContainer("chiTietDichVuDc")
public class ChiTietDichVuDetailView extends StandardDetailView<ChiTietDichVu> {
}