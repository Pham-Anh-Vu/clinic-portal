package com.company.clinicportal.view.nhansu;

import com.company.clinicportal.entity.NhanSu;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "nhan-sus", layout = MainView.class)
@ViewController(id = "NhanSu.list")
@ViewDescriptor(path = "nhan-su-list-view.xml")
@LookupComponent("nhanSusDataGrid")
@DialogMode(width = "64em")
public class NhanSuListView extends StandardListView<NhanSu> {
}