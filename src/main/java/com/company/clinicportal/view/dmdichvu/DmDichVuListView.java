package com.company.clinicportal.view.dmdichvu;

import com.company.clinicportal.entity.DmDichVu;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "dm-dich-vus", layout = MainView.class)
@ViewController(id = "DmDichVu.list")
@ViewDescriptor(path = "dm-dich-vu-list-view.xml")
@LookupComponent("dmDichVusDataGrid")
@DialogMode(width = "64em")
public class DmDichVuListView extends StandardListView<DmDichVu> {
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Messages messages;

    @Supply(to = "dmDichVusDataGrid.nhomDichVu", subject = "renderer")
    private Renderer<DmDichVu> dmDichVusDataGridNhomDichVuRenderer() {
        return new ComponentRenderer<>(dmdichvu -> {
            // TODO: create suitable component
            Span span = uiComponents.create(Span.class);
            if (dmdichvu.getNhomDichVu()!=null) {
                span.setText(messages.getMessage(dmdichvu.getNhomDichVu()));
                span.addClassName(dmdichvu.getNhomDichVu().getId());
            }
            return span;
        });
    }

}