package com.company.clinicportal.view.phieuthuthap;

import com.company.clinicportal.entity.PhieuThuThap;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "phieuThuThaps1/:id", layout = MainView.class)
@ViewController(id = "PhieuThuThap.detail1")
@ViewDescriptor(path = "phieu-thu-thap-detail-view1.xml")
@EditedEntityContainer("phieuThuThapDc")
public class PhieuThuThapDetailView1 extends StandardDetailView<PhieuThuThap> {
}