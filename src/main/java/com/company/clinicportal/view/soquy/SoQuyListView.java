package com.company.clinicportal.view.soquy;

import com.company.clinicportal.entity.SoQuy;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "so-quys", layout = MainView.class)
@ViewController(id = "SoQuy.list")
@ViewDescriptor(path = "so-quy-list-view.xml")
@LookupComponent("soQuysDataGrid")
@DialogMode(width = "64em")
public class SoQuyListView extends StandardListView<SoQuy> {
}