package com.company.clinicportal.view.phieuthuthap;

import com.company.clinicportal.entity.PhieuThuThap;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.StandardListView;

@Route(value = "phieuThuThaps", layout = MainView.class)
@ViewController(id = "PhieuThuThap.list")
@ViewDescriptor(path = "phieu-thu-thap-list-view.xml")
@LookupComponent("phieuThuThapsDataGrid")
@DialogMode(width = "64em")
public class PhieuThuThapListView extends StandardListView<PhieuThuThap> {

    @ViewComponent
    private DataGrid<PhieuThuThap> phieuThuThapsDataGrid;
}