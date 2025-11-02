package com.company.clinicportal.view.dmdichvu;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.DmDichVu;
import com.company.clinicportal.view.buoidieutri.ThuThuatDetailView;
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
import io.jmix.flowui.action.list.RemoveAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;


@Route(value = "dm-dich-vus", layout = MainView.class)
@ViewController(id = "DmDichVu.list")
@ViewDescriptor(path = "dm-dich-vu-list-view.xml")
@LookupComponent("dmDichVusDataGrid")
@DialogMode(width = "80%", height = "80%")
public class DmDichVuListView extends StandardListView<DmDichVu> {
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Messages messages;
    @ViewComponent
    private DataGrid<DmDichVu> dmDichVusDataGrid;
    @Autowired
    private DialogWindows dialogWindows;
    @ViewComponent
    private CollectionLoader<DmDichVu> dmDichVusDl;
    @ViewComponent("dmDichVusDataGrid.removeAction")
    private RemoveAction<DmDichVu> dmDichVusDataGridRemoveAction;

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

    @Subscribe
    public void onInit(InitEvent event) {
        dmDichVusDataGrid.addComponentColumn(dmDichVu -> {
                    // Tạo layout chứa hai nút
                    HorizontalLayout actionsLayout = uiComponents.create(HorizontalLayout.class);

                    // Nút Sửa
                    JmixButton editButton = uiComponents.create(JmixButton.class);
                    editButton.setText("Cập nhật");
                    editButton.addClickListener(e -> {
                        DialogWindow<DmDichVuDetailView> window = dialogWindows.detail(this, DmDichVu.class)
                                .editEntity(dmDichVu) // chỉnh sửa entity hiện tại
                                .withViewClass(DmDichVuDetailView.class)
                                .build();
                        window.addAfterCloseListener(e1 -> {
                            dmDichVusDl.load();
                        });
                        window.open();
                    });

                    // Nút Xóa
                    JmixButton deleteButton = uiComponents.create(JmixButton.class);
                    deleteButton.setText("Xóa");
                    deleteButton.addClickListener(e -> {
                        dmDichVusDataGrid.select(dmDichVu);
                        dmDichVusDataGridRemoveAction.execute();
                    });

                    // Thêm 2 nút vào layout
                    actionsLayout.add(editButton);
                    actionsLayout.add(deleteButton);

                    return actionsLayout;
                })
                .setHeader("Thao tác")
                .setAutoWidth(true);
    }
}