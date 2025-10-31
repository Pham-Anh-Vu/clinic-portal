package com.company.clinicportal.view.phieuchi;

import com.company.clinicportal.entity.PhieuChi;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "phieu-chis", layout = MainView.class)
@ViewController(id = "PhieuChi.list")
@ViewDescriptor(path = "phieu-chi-list-view.xml")
@LookupComponent("phieuChisDataGrid")
@DialogMode(width = "64em")
public class PhieuChiListView extends StandardListView<PhieuChi> {
}