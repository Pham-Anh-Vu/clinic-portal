package com.company.clinicportal.view.benhnhan;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.view.chitietdieutri.ChiTietDieuTriListView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;


@Route(value = "so-benh-ans", layout = MainView.class)
@ViewController(id = "SoBenhAn.list")
@ViewDescriptor(path = "so-benh-an-list-view.xml")
@LookupComponent("benhNhansDataGrid")
@DialogMode(width = "64em")
public class SoBenhAnListView extends StandardListView<BenhNhan> {
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private ViewNavigators viewNavigators;
    @ViewComponent
    private DataGrid<BenhNhan> benhNhansDataGrid;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private Messages messages;
    @ViewComponent
    private CollectionLoader<BenhNhan> benhNhansDl;

    @Subscribe
    public void onInit(InitEvent event) {
        benhNhansDataGrid.addColumn(lh -> {
                    if (lh == null) return uiComponents.create(Span.class);
                    if (lh.getNgayKhamBenh() == null) {
                        return "";
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    return sdf.format(lh.getNgayKhamBenh());
                })
                .setHeader("Ngày khám bệnh")
                .setAutoWidth(true);

        benhNhansDataGrid.addComponentColumn(lh -> {
                    if (lh == null) return uiComponents.create(Span.class);

                    Span span = uiComponents.create(Span.class);
                    if (lh.getTrangThaiKhamBenh() != null) {
                        var trangThai = lh.getTrangThaiKhamBenh();
                        span.setText(messages.getMessage(trangThai));
                        span.addClassName(trangThai.toString());
                    }

                    return span;
                })
                .setHeader("Trạng thái")
                .setAutoWidth(true);

        benhNhansDataGrid.addComponentColumn(benhNhan -> {
            JmixButton button = uiComponents.create(JmixButton.class);
            button.setText("Chi tiết");
            button.addClickListener(e -> {
                if (benhNhan != null && benhNhan.getId() != null) {
                    DialogWindow<SoBenhAnDetailView> windows = dialogWindows.view(this, SoBenhAnDetailView.class).build();
                    windows.getView().setIdBenhNhan(benhNhan);
                    windows.addAfterCloseListener(soBenhAnDetailViewAfterCloseEvent -> {benhNhansDl.load();});
                    windows.open();
                }
            });
            return button;
        }).setHeader("Thao tác").setAutoWidth(true);
    }

    @Supply(to = "benhNhansDataGrid.tuoi", subject = "renderer")
    private Renderer<BenhNhan> benhNhansDataGridTuoiRenderer() {
        return new ComponentRenderer<>(benhNhan -> {
            Span span = uiComponents.create(Span.class);
            span.setText(BenhNhan.calculateTuoi(benhNhan != null ? benhNhan.getNgaySinh() : null));
            return span;
        });
    }
}