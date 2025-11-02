package com.company.clinicportal.view.benhnhan;

import com.company.clinicportal.entity.BenhNhan;
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


@Route(value = "benh-nhans", layout = MainView.class)
@ViewController(id = "BenhNhan.list")
@ViewDescriptor(path = "benh-nhan-list-view.xml")
@LookupComponent("benhNhansDataGrid")
@DialogMode(width = "64em")
public class BenhNhanListView extends StandardListView<BenhNhan> {
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Messages messages;

    @Supply(to = "benhNhansDataGrid.gioiTinh", subject = "renderer")
    private Renderer<BenhNhan> benhNhansDataGridGioiTinhRenderer() {
        return new ComponentRenderer<>(benhnhan -> {
            // TODO: create suitable component
            Span span = uiComponents.create(Span.class);
            if (benhnhan.getGioiTinh()!=null) {
                span.setText(messages.getMessage(benhnhan.getGioiTinh()));
                span.addClassName(benhnhan.getGioiTinh().getId());
            }
            return span;
        });
    }

}