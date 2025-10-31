package com.company.clinicportal.view.tinhkpi;

import com.company.clinicportal.entity.TinhKpi;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "tinh-kpis", layout = MainView.class)
@ViewController(id = "TinhKpi.list")
@ViewDescriptor(path = "tinh-kpi-list-view.xml")
@LookupComponent("tinhKpisDataGrid")
@DialogMode(width = "64em")
public class TinhKpiListView extends StandardListView<TinhKpi> {
}