package com.company.clinicportal.view.lichhen;

import com.company.clinicportal.entity.LichHen;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "lich-hens/:id", layout = MainView.class)
@ViewController(id = "LichHen.detail")
@ViewDescriptor(path = "lich-hen-detail-view.xml")
@EditedEntityContainer("lichHenDc")
public class LichHenDetailView extends StandardDetailView<LichHen> {
}