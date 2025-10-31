package com.company.clinicportal.view.dmdichvu;

import com.company.clinicportal.entity.DmDichVu;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "dm-dich-vus/:id", layout = MainView.class)
@ViewController(id = "DmDichVu.detail")
@ViewDescriptor(path = "dm-dich-vu-detail-view.xml")
@EditedEntityContainer("dmDichVuDc")
public class DmDichVuDetailView extends StandardDetailView<DmDichVu> {
}