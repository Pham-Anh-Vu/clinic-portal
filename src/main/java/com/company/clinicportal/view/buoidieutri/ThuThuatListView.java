package com.company.clinicportal.view.buoidieutri;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;


@Route(value = "thu-thuats", layout = MainView.class)
@ViewController(id = "ThuThuat.list")
@ViewDescriptor(path = "thu-thuat-list-view.xml")
@LookupComponent("buoiDieuTrisDataGrid")
@DialogMode(width = "64em")
public class ThuThuatListView extends StandardListView<BuoiDieuTri> {
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Messages messages;
    @ViewComponent
    private CollectionLoader<BuoiDieuTri> buoiDieuTrisDl_1;
    @ViewComponent
    private CollectionLoader<BuoiDieuTri> buoiDieuTrisDl;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        buoiDieuTrisDl.setParameter("ngayThucHien", new Date());
        buoiDieuTrisDl.load();
    }

    @Supply(to = "buoiDieuTrisDataGrid.trangThai", subject = "renderer")
    private Renderer<BuoiDieuTri> buoiDieuTrisDataGridTrangThaiRenderer() {
        return new ComponentRenderer<>(buoidieutri -> {
            // TODO: create suitable component
            Span span = uiComponents.create(Span.class);
            if (buoidieutri.getTrangThai()!=null) {
                span.setText(messages.getMessage(buoidieutri.getTrangThai()));
                span.addClassName(buoidieutri.getTrangThai().getId());
            }
            return span;
        });
    }

    @Supply(to = "buoiDieuTrisDataGrid2.trangThai", subject = "renderer")
    private Renderer<BuoiDieuTri> buoiDieuTrisDataGrid2TrangThaiRenderer() {
        return new ComponentRenderer<>(buoidieutri -> {
            // TODO: create suitable component
            Span span = uiComponents.create(Span.class);
            if (buoidieutri.getTrangThai()!=null) {
                span.setText(messages.getMessage(buoidieutri.getTrangThai()));
                span.addClassName(buoidieutri.getTrangThai().getId());
            }
            return span;
        });
    }


}