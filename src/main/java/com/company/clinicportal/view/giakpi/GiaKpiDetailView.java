package com.company.clinicportal.view.giakpi;

import com.company.clinicportal.entity.GiaKpi;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "gia-kpis/:id", layout = MainView.class)
@ViewController(id = "GiaKpi.detail")
@ViewDescriptor(path = "gia-kpi-detail-view.xml")
@EditedEntityContainer("giaKpiDc")
public class GiaKpiDetailView extends StandardDetailView<GiaKpi> {
}