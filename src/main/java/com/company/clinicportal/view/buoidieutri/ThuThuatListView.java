package com.company.clinicportal.view.buoidieutri;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.view.chitietdieutri.ChiTietDieuTriDetailView;
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


@Route(value = "thu-thuats", layout = MainView.class)
@ViewController(id = "ThuThuat.list")
@ViewDescriptor(path = "thu-thuat-list-view.xml")
@LookupComponent("thuThuatsDataGrid")
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
    @ViewComponent
    private DataGrid<BuoiDieuTri> buoiDieuTrisDataGrid;
    @Autowired
    private DialogWindows dialogWindows;
    @ViewComponent("buoiDieuTrisDataGrid.removeAction")
    private RemoveAction<BuoiDieuTri> buoiDieuTrisDataGridRemoveAction;
    @ViewComponent
    private DataGrid<BuoiDieuTri> buoiDieuTrisDataGrid2;
    @ViewComponent("buoiDieuTrisDataGrid2.removeAction2")
    private RemoveAction<BuoiDieuTri> buoiDieuTrisDataGrid2RemoveAction2;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        buoiDieuTrisDl.setParameter("ngayThucHien", new Date());
        buoiDieuTrisDl.load();
    }

    @Subscribe
    public void onInit(InitEvent event) {
        buoiDieuTrisDataGrid.addComponentColumn(buoiDieuTri -> {
                    // Tạo layout chứa hai nút
                    HorizontalLayout actionsLayout = uiComponents.create(HorizontalLayout.class);

                    // Nút Sửa
                    JmixButton editButton = uiComponents.create(JmixButton.class);
                    editButton.setText("Cập nhật");
                    editButton.addClickListener(e -> {
                        DialogWindow<ThuThuatDetailView> window = dialogWindows.detail(this, BuoiDieuTri.class)
                                .editEntity(buoiDieuTri) // chỉnh sửa entity hiện tại
                                .withViewClass(ThuThuatDetailView.class)
                                .build();
                        window.addAfterCloseListener(e1 -> {
                            buoiDieuTrisDl.setParameter("ngayThucHien", new Date());
                            buoiDieuTrisDl.load();
                        });
                        window.open();
                    });

                    // Nút Xóa
                    JmixButton deleteButton = uiComponents.create(JmixButton.class);
                    deleteButton.setText("Xóa");
                    deleteButton.addClickListener(e -> {
                        buoiDieuTrisDataGrid.select(buoiDieuTri);
                        buoiDieuTrisDataGridRemoveAction.execute();
                    });

                    // Thêm 2 nút vào layout
                    actionsLayout.add(editButton);
                    actionsLayout.add(deleteButton);

                    return actionsLayout;
                })
                .setHeader("Thao tác")
                .setAutoWidth(true);


        buoiDieuTrisDataGrid2.addComponentColumn(buoiDieuTri -> {
                    // Tạo layout chứa hai nút
                    HorizontalLayout actionsLayout = uiComponents.create(HorizontalLayout.class);

                    // Nút Sửa
                    JmixButton editButton = uiComponents.create(JmixButton.class);
                    editButton.setText("Cập nhật");
                    editButton.addClickListener(e -> {
                        DialogWindow<ThuThuatDetailView> window = dialogWindows.detail(this, BuoiDieuTri.class)
                                .editEntity(buoiDieuTri) // chỉnh sửa entity hiện tại
                                .withViewClass(ThuThuatDetailView.class)
                                .build();
                        window.addAfterCloseListener(e1 -> {
                            buoiDieuTrisDl_1.load();
                        });
                        window.open();
                    });

                    // Nút Xóa
                    JmixButton deleteButton = uiComponents.create(JmixButton.class);
                    deleteButton.setText("Xóa");
                    deleteButton.addClickListener(e -> {
                        buoiDieuTrisDataGrid2.select(buoiDieuTri);
                        buoiDieuTrisDataGrid2RemoveAction2.execute();
                    });

                    // Thêm 2 nút vào layout
                    actionsLayout.add(editButton);
                    actionsLayout.add(deleteButton);

                    return actionsLayout;
                })
                .setHeader("Thao tác")
                .setAutoWidth(true);
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