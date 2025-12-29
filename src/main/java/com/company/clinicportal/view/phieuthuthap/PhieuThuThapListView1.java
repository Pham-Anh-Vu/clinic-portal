package com.company.clinicportal.view.phieuthuthap;

import com.company.clinicportal.entity.PhieuThuThap;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "phieuThuThaps1", layout = MainView.class)
@ViewController(id = "PhieuThuThap.list1")
@ViewDescriptor(path = "phieu-thu-thap-list-view1.xml")
@LookupComponent("phieuThuThapsDataGrid")
@DialogMode(width = "64em")
public class PhieuThuThapListView1 extends StandardListView<PhieuThuThap> {
}