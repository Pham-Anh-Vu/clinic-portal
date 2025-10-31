package com.company.clinicportal.view.trongsokpidichvu;

import com.company.clinicportal.entity.TrongSoKpiDichVu;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "trong-so-kpi-dich-vus/:id", layout = MainView.class)
@ViewController(id = "TrongSoKpiDichVu.detail")
@ViewDescriptor(path = "trong-so-kpi-dich-vu-detail-view.xml")
@EditedEntityContainer("trongSoKpiDichVuDc")
public class TrongSoKpiDichVuDetailView extends StandardDetailView<TrongSoKpiDichVu> {
}