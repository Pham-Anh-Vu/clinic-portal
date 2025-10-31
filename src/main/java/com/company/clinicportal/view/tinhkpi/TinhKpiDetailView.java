package com.company.clinicportal.view.tinhkpi;

import com.company.clinicportal.entity.TinhKpi;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "tinh-kpis/:id", layout = MainView.class)
@ViewController(id = "TinhKpi.detail")
@ViewDescriptor(path = "tinh-kpi-detail-view.xml")
@EditedEntityContainer("tinhKpiDc")
public class TinhKpiDetailView extends StandardDetailView<TinhKpi> {
}