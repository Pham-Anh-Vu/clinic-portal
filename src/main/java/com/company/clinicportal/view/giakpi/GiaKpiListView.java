package com.company.clinicportal.view.giakpi;

import com.company.clinicportal.entity.GiaKpi;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "gia-kpis", layout = MainView.class)
@ViewController(id = "GiaKpi.list")
@ViewDescriptor(path = "gia-kpi-list-view.xml")
@LookupComponent("giaKpisDataGrid")
@DialogMode(width = "64em")
public class GiaKpiListView extends StandardListView<GiaKpi> {
}