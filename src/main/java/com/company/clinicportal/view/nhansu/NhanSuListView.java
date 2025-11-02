package com.company.clinicportal.view.nhansu;

import com.company.clinicportal.entity.LichHen;
import com.company.clinicportal.entity.NhanSu;
import com.company.clinicportal.view.lichhen.LichHenDetailView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.list.ReadAction;
import io.jmix.flowui.action.list.RemoveAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;


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
    @ViewComponent
    private DataGrid<NhanSu> nhanSusDataGrid;
    @Autowired
    private DialogWindows dialogWindows;
    @ViewComponent
    private CollectionLoader<NhanSu> nhanSusDl;
    @ViewComponent("nhanSusDataGrid.removeAction")
    private RemoveAction<NhanSu> nhanSusDataGridRemoveAction;
    @ViewComponent("nhanSusDataGrid.readAction")
    private ReadAction<NhanSu> nhanSusDataGridReadAction;

    @Subscribe
    public void onInit(final InitEvent event) {
        nhanSusDataGrid.addComponentColumn(nhanSu -> {
                    // Tạo layout chứa hai nút
                    HorizontalLayout actionsLayout = uiComponents.create(HorizontalLayout.class);

                    // Nút Sửa
                    JmixButton editButton = uiComponents.create(JmixButton.class);
                    editButton.setText("Sửa");
                    editButton.addClickListener(e -> {
                        DialogWindow<NhanSuDetailView> window = dialogWindows.detail(this, NhanSu.class)
                                .editEntity(nhanSu) // chỉnh sửa entity hiện tại
                                .withViewClass(NhanSuDetailView.class)
                                .build();
                        window.addAfterCloseListener(e1 -> {
                            nhanSusDl.load();
                        });
                        window.open();
                    });

                    // Nút Xen
                    JmixButton readButton = uiComponents.create(JmixButton.class);
                    readButton.setText("Xem");
                    readButton.addClickListener(e -> {
                        nhanSusDataGrid.select(nhanSu);
                        nhanSusDataGridReadAction.execute();
                    });

                    // Nút Xóa
                    JmixButton deleteButton = uiComponents.create(JmixButton.class);
                    deleteButton.setText("Xóa");
                    deleteButton.addClickListener(e -> {
                        nhanSusDataGrid.select(nhanSu);
                        nhanSusDataGridRemoveAction.execute();
                    });

                    // Thêm 3 nút vào layout
                    actionsLayout.add(editButton);
                    actionsLayout.add(readButton);
                    actionsLayout.add(deleteButton);

                    return actionsLayout;
                })
                .setHeader("Thao tác")
                .setAutoWidth(true);
    }

    @Supply(to = "nhanSusDataGrid.gioiTinh", subject = "renderer")
    private Renderer<NhanSu> nhanSusDataGridGioiTinhRenderer() {
        return new ComponentRenderer<>(nhansu -> {
            // TODO: create suitable component
            Span span = uiComponents.create(Span.class);
            if (nhansu.getGioiTinh()!=null) {
                span.setText(messages.getMessage(nhansu.getGioiTinh()));
                span.addClassName("gioi_tinh");
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