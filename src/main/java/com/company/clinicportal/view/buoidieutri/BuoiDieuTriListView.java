package com.company.clinicportal.view.buoidieutri;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.entity.LichHen;
import com.company.clinicportal.view.lichhen.LichHenDetailView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
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


@Route(value = "buoi-dieu-tris", layout = MainView.class)
@ViewController(id = "BuoiDieuTri.list")
@ViewDescriptor(path = "buoi-dieu-tri-list-view.xml")
@LookupComponent("buoiDieuTrisDataGrid")
@DialogMode(width = "80%", height = "100%")
public class BuoiDieuTriListView extends StandardListView<BuoiDieuTri> {
    public Long idChiTietDichVu = null;

    @ViewComponent
    private CollectionLoader<BuoiDieuTri> buoiDieuTrisDl;
    @Autowired
    private DataManager dataManager;
    @ViewComponent
    private H3 dichVuField;
    @Autowired
    private Messages messages;
    @Autowired
    private UiComponents uiComponents;
    @ViewComponent
    private DataGrid<BuoiDieuTri> buoiDieuTrisDataGrid;
    @Autowired
    private DialogWindows dialogWindows;
    @ViewComponent("buoiDieuTrisDataGrid.removeAction")
    private RemoveAction<BuoiDieuTri> buoiDieuTrisDataGridRemoveAction;

    public void setIdChiTietDichVu(Long idChiTietDichVu) {
        this.idChiTietDichVu = idChiTietDichVu;
    }

    @Subscribe
    public void onInit(InitEvent event) {
        buoiDieuTrisDataGrid.addComponentColumn(buoiDieuTri -> {
                    // Tạo layout chứa hai nút
                    HorizontalLayout actionsLayout = uiComponents.create(HorizontalLayout.class);

                    // Nút Sửa
                    JmixButton editButton = uiComponents.create(JmixButton.class);
                    editButton.setText("Sửa");
                    editButton.addClickListener(e -> {
                        DialogWindow<BuoiDieuTriDetailView> window = dialogWindows.detail(this, BuoiDieuTri.class)
                                .editEntity(buoiDieuTri) // chỉnh sửa entity hiện tại
                                .withViewClass(BuoiDieuTriDetailView.class)
                                .build();
                        window.addAfterCloseListener(e1 -> {
                            buoiDieuTrisDl.setParameter("idChiTietDichVu", idChiTietDichVu);
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
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        buoiDieuTrisDl.setParameter("idChiTietDichVu", idChiTietDichVu);
        buoiDieuTrisDl.load();

        ChiTietDichVu chiTietDichVu = dataManager.load(ChiTietDichVu.class).id(idChiTietDichVu).optional().orElse(null);
        if(chiTietDichVu != null) dichVuField.setText("Dịch vụ: " + chiTietDichVu.getIdDichVu().getTenDichVu());
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


}