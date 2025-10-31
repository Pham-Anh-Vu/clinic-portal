package com.company.clinicportal.view.phieudieutri;

import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "phieu-dieu-tris", layout = MainView.class)
@ViewController(id = "PhieuDieuTri.list")
@ViewDescriptor(path = "phieu-dieu-tri-list-view.xml")
@LookupComponent("phieuDieuTrisDataGrid")
@DialogMode(width = "64em")
public class PhieuDieuTriListView extends StandardListView<PhieuDieuTri> {
}