package com.company.clinicportal.view.trongsokpidichvu;

import com.company.clinicportal.entity.TrongSoKpiDichVu;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "trong-so-kpi-dich-vus", layout = MainView.class)
@ViewController(id = "TrongSoKpiDichVu.list")
@ViewDescriptor(path = "trong-so-kpi-dich-vu-list-view.xml")
@LookupComponent("trongSoKpiDichVusDataGrid")
@DialogMode(width = "64em")
public class TrongSoKpiDichVuListView extends StandardListView<TrongSoKpiDichVu> {
}