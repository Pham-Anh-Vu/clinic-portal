package com.company.clinicportal.view.phieuchi;

import com.company.clinicportal.entity.PhieuChi;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "phieu-chis/:id", layout = MainView.class)
@ViewController(id = "PhieuChi.detail")
@ViewDescriptor(path = "phieu-chi-detail-view.xml")
@EditedEntityContainer("phieuChiDc")
public class PhieuChiDetailView extends StandardDetailView<PhieuChi> {
}