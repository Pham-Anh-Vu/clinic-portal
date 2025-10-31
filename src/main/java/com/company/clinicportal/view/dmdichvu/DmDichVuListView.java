package com.company.clinicportal.view.dmdichvu;

import com.company.clinicportal.entity.DmDichVu;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "dm-dich-vus", layout = MainView.class)
@ViewController(id = "DmDichVu.list")
@ViewDescriptor(path = "dm-dich-vu-list-view.xml")
@LookupComponent("dmDichVusDataGrid")
@DialogMode(width = "64em")
public class DmDichVuListView extends StandardListView<DmDichVu> {
}