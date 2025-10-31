package com.company.clinicportal.view.benhnhan;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "benh-nhans", layout = MainView.class)
@ViewController(id = "BenhNhan.list")
@ViewDescriptor(path = "benh-nhan-list-view.xml")
@LookupComponent("benhNhansDataGrid")
@DialogMode(width = "64em")
public class BenhNhanListView extends StandardListView<BenhNhan> {
}