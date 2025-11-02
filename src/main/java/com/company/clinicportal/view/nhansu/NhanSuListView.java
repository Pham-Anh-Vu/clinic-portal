package com.company.clinicportal.view.nhansu;

import com.company.clinicportal.entity.NhanSu;
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


@Route(value = "nhan-sus", layout = MainView.class)
@ViewController(id = "NhanSu.list")
@ViewDescriptor(path = "nhan-su-list-view.xml")
@LookupComponent("nhanSusDataGrid")
@DialogMode(width = "64em")
public class NhanSuListView extends StandardListView<NhanSu> {
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Messages messages;

    @Supply(to = "nhanSusDataGrid.gioiTinh", subject = "renderer")
    private Renderer<NhanSu> nhanSusDataGridGioiTinhRenderer() {
        return new ComponentRenderer<>(nhansu -> {
            // TODO: create suitable component
            Span span = uiComponents.create(Span.class);
            if (nhansu.getGioiTinh()!=null) {
                span.setText(messages.getMessage(nhansu.getGioiTinh()));
                span.addClassName(nhansu.getGioiTinh().getId());
            }
            return span;
        });
    }

    @Supply(to = "nhanSusDataGrid.chucVu", subject = "renderer")
    private Renderer<NhanSu> nhanSusDataGridChucVuRenderer() {
        return new ComponentRenderer<>(nhansu -> {
            // TODO: create suitable component
            Span span = uiComponents.create(Span.class);
            if (nhansu.getChucVu()!=null) {
                span.setText(messages.getMessage(nhansu.getChucVu()));
                span.addClassName(nhansu.getChucVu().toString());
            }
            return span;
        });
    }

    @Supply(to = "nhanSusDataGrid.hinhThucLamVic", subject = "renderer")
    private Renderer<NhanSu> nhanSusDataGridHinhThucLamVicRenderer() {
        return new ComponentRenderer<>(nhansu -> {
            // TODO: create suitable component
            Span span = uiComponents.create(Span.class);
            if (nhansu.getHinhThucLamVic()!=null) {
                span.setText(messages.getMessage(nhansu.getHinhThucLamVic()));
                span.addClassName(nhansu.getHinhThucLamVic().toString());
            }
            return span;
        });
    }


}