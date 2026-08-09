package com.company.clinicportal.view.lichhen;

import com.company.clinicportal.entity.LichHen;
import com.company.clinicportal.view.chitietdieutri.ChiTietDieuTriListView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.list.CreateAction;
import io.jmix.flowui.action.list.RemoveAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.Date;


@Route(value = "lich-hens", layout = MainView.class)
@ViewController(id = "LichHen.list")
@ViewDescriptor(path = "lich-hen-list-view.xml")
@LookupComponent("lichHensDataGrid")
@DialogMode(width = "64em")
public class LichHenListView extends StandardListView<LichHen> {
    @ViewComponent
    private CollectionLoader<LichHen> lichHensDl;
    @Autowired
    private Messages messages;
    @ViewComponent
    private DataGrid<LichHen> lichHensDataGrid;
    @Autowired
    private DialogWindows dialogWindows;
    @ViewComponent("lichHensDataGrid.removeAction")
    private RemoveAction<LichHen> lichHensDataGridRemoveAction;
    @ViewComponent
    private DataGrid<LichHen> lichHensDataGrid2;
    @ViewComponent("lichHensDataGrid2.removeAction")
    private RemoveAction<LichHen> lichHensDataGrid2RemoveAction;
    @ViewComponent("lichHensDataGrid.createAction")
    private CreateAction<LichHen> lichHensDataGridCreateAction;
    @ViewComponent("lichHensDataGrid2.createAction")
    private CreateAction<LichHen> lichHensDataGrid2CreateAction;
    @ViewComponent
    private CollectionLoader<LichHen> lichHensDl_1;
    @ViewComponent
    private CollectionContainer<LichHen> lichHensDc;
    @ViewComponent
    private CollectionContainer<LichHen> lichHensDc_1;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        lichHensDl.setParameter("currentDate", new Date());
        lichHensDl.load();
    }

    @Autowired
    private UiComponents uiComponents;

    @Subscribe
    public void onInit(InitEvent event) {
        lichHensDataGrid.addComponentColumn(lichHen -> {
                    // Tạo layout chứa hai nút
                    HorizontalLayout actionsLayout = uiComponents.create(HorizontalLayout.class);

                    // Nút Sửa
                    JmixButton editButton = uiComponents.create(JmixButton.class);
                    editButton.setText("Sửa");
                    editButton.addClickListener(e -> {
                        DialogWindow<LichHenDetailView> window = dialogWindows.detail(this, LichHen.class)
                                .editEntity(lichHen) // chỉnh sửa entity hiện tại
                                .withViewClass(LichHenDetailView.class)
                                .build();
                        window.addAfterCloseListener(e1 -> {
                            lichHensDl.setParameter("currentDate", new Date());
                            lichHensDl.load();
                        });
                        window.open();
                    });

                    // Nút Xóa
                    JmixButton deleteButton = uiComponents.create(JmixButton.class);
                    deleteButton.setText("Xóa");
                    deleteButton.addClickListener(e -> {
                        lichHensDataGrid.select(lichHen);
                        lichHensDataGridRemoveAction.execute();
                    });

                    // Thêm 2 nút vào layout
                    actionsLayout.add(editButton);
                    actionsLayout.add(deleteButton);

                    return actionsLayout;
                })
                .setHeader("Thao tác")
                .setAutoWidth(true);


        lichHensDataGrid2.addComponentColumn(lichHen -> {
                    // Tạo layout chứa hai nút
                    HorizontalLayout actionsLayout = uiComponents.create(HorizontalLayout.class);

                    // Nút Sửa
                    JmixButton editButton = uiComponents.create(JmixButton.class);
                    editButton.setText("Sửa");
                    editButton.addClickListener(e -> {
                        DialogWindow<LichHenDetailView> window = dialogWindows.detail(this, LichHen.class)
                                .editEntity(lichHen) // chỉnh sửa entity hiện tại
                                .withViewClass(LichHenDetailView.class)
                                .build();
                        window.addAfterCloseListener(e1 -> {
                            lichHensDl_1.load();
                        });
                        window.open();
                    });

                    // Nút Xóa
                    JmixButton deleteButton = uiComponents.create(JmixButton.class);
                    deleteButton.setText("Xóa");
                    deleteButton.addClickListener(e -> {
                        lichHensDataGrid2.select(lichHen);
                        lichHensDataGrid2RemoveAction.execute();
                    });

                    // Thêm 2 nút vào layout
                    actionsLayout.add(editButton);
                    actionsLayout.add(deleteButton);

                    return actionsLayout;
                })
                .setHeader("Thao tác")
                .setAutoWidth(true);
    }

    @Install(to = "lichHensDataGrid.createAction", subject = "afterSaveHandler")
    private void lichHensDataGridCreateActionAfterSaveHandler(final LichHen lichHen) {
        lichHensDc.getMutableItems().clear();
        lichHensDl.setParameter("currentDate", new Date());
        lichHensDl.load();

        lichHensDc_1.getMutableItems().clear();
        lichHensDl_1.load();
    }

    @Install(to = "lichHensDataGrid.removeAction", subject = "afterActionPerformedHandler")
    private void lichHensDataGridRemoveActionAfterActionPerformedHandler(final RemoveOperation.AfterActionPerformedEvent<LichHen> afterActionPerformedEvent) {
        lichHensDc.getMutableItems().clear();
        lichHensDl.setParameter("currentDate", new Date());
        lichHensDl.load();

        lichHensDc_1.getMutableItems().clear();
        lichHensDl_1.load();
    }


    @Install(to = "lichHensDataGrid.editAction", subject = "afterSaveHandler")
    private void lichHensDataGridEditActionAfterSaveHandler(final LichHen lichHen) {
        lichHensDc.getMutableItems().clear();
        lichHensDl.setParameter("currentDate", new Date());
        lichHensDl.load();

        lichHensDc_1.getMutableItems().clear();
        lichHensDl_1.load();
    }

    @Supply(to = "lichHensDataGrid.hinhThuc", subject = "renderer")
    private Renderer<LichHen> lichHensDataGridHinhThucRenderer() {
        return new ComponentRenderer<>(lichhen -> {
            // TODO: create suitable component
            Span span = uiComponents.create(Span.class);
            if (lichhen.getHinhThuc()!=null) {
                span.setText(messages.getMessage(lichhen.getHinhThuc().getId()));
                span.addClassName(lichhen.getHinhThuc().toString());
            }
            span.getElement().getThemeList().add("badge");
            return span;
        }); 
    }



    @Supply(to = "lichHensDataGrid2.hinhThuc", subject = "renderer")
    private Renderer<LichHen> lichHensDataGrid2HinhThucRenderer() {
        return new ComponentRenderer<>(lichhen -> {
            Span span = uiComponents.create(Span.class);
            if (lichhen.getHinhThuc()!=null) {
                span.setText(messages.getMessage(lichhen.getHinhThuc().getId()));
                span.addClassName(lichhen.getHinhThuc().toString());
            }
            span.getElement().getThemeList().add("badge");
            return span;
        });
    }


}