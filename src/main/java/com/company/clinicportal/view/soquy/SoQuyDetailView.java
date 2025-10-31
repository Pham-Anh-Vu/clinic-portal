package com.company.clinicportal.view.soquy;

import com.company.clinicportal.entity.SoQuy;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "so-quys/:id", layout = MainView.class)
@ViewController(id = "SoQuy.detail")
@ViewDescriptor(path = "so-quy-detail-view.xml")
@EditedEntityContainer("soQuyDc")
public class SoQuyDetailView extends StandardDetailView<SoQuy> {
}