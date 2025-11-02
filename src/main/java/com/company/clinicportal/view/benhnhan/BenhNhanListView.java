package com.company.clinicportal.view.benhnhan;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.view.buoidieutri.ThuThuatDetailView;
import com.company.clinicportal.view.chitietdieutri.ChiTietDieuTriListView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;


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
    @ViewComponent
    private DataGrid<BenhNhan> benhNhansDataGrid;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private DialogWindows dialogWindows;
    @ViewComponent
    private CollectionLoader<BenhNhan> benhNhansDl;
    @ViewComponent("benhNhansDataGrid.removeAction")
    private RemoveAction<BenhNhan> benhNhansDataGridRemoveAction;
    @ViewComponent("benhNhansDataGrid.readAction")
    private ReadAction<BenhNhan> benhNhansDataGridReadAction;

    @Subscribe
    public void onInit(InitEvent event) {
        benhNhansDataGrid.addComponentColumn(lh -> {
                    if (lh == null) return uiComponents.create(Span.class);

                    Optional<PhieuDieuTri> phieuDieuTri = dataManager.load(PhieuDieuTri.class)
                            .query("select p from PhieuDieuTri p where p.idBenhNhan = :bn order by p.ngayKham desc")
                            .parameter("bn", lh)
                            .maxResults(1)
                            .optional();

                    Span span = uiComponents.create(Span.class);

                    if (phieuDieuTri.isPresent() && phieuDieuTri.get().getTrangThai() != null) {
                        var trangThai = phieuDieuTri.get().getTrangThai(); // Enum hoặc String
                        span.setText(messages.getMessage(trangThai));
                        span.addClassName(trangThai.toString()); // Gán class CSS nếu muốn
                    }

                    return span;
                })
                .setHeader("Trạng thái")
                .setAutoWidth(true);

        benhNhansDataGrid.addColumn(lh -> {
                    if (lh == null) return uiComponents.create(Span.class);

                    Optional<PhieuDieuTri> phieuDieuTri = dataManager.load(PhieuDieuTri.class)
                            .query("select p from PhieuDieuTri p where p.idBenhNhan = :bn order by p.ngayKham desc")
                            .parameter("bn", lh)
                            .maxResults(1)
                            .optional();
                    if(phieuDieuTri.isEmpty() || phieuDieuTri.get().getThoiGianTaiKham() == null) return "";

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    return sdf.format(phieuDieuTri.get().getThoiGianTaiKham());
                })
                .setHeader("Thời gian tái khám")
                .setAutoWidth(true);

        benhNhansDataGrid.addComponentColumn(benhNhan -> {
            // Tạo layout chứa hai nút
            HorizontalLayout actionsLayout = uiComponents.create(HorizontalLayout.class);

            // Nút Xóa
            JmixButton readButton = uiComponents.create(JmixButton.class);
            readButton.setText("Chi tiết");
            readButton.addClickListener(e -> {
                benhNhansDataGrid.select(benhNhan);
                benhNhansDataGridReadAction.execute();
            });

            // Nút Sửa
            JmixButton editButton = uiComponents.create(JmixButton.class);
            editButton.setText("Sửa");
            editButton.addClickListener(e -> {
                DialogWindow<BenhNhanDetailView> window = dialogWindows.detail(this, BenhNhan.class)
                        .editEntity(benhNhan) // chỉnh sửa entity hiện tại
                        .withViewClass(BenhNhanDetailView.class)
                        .build();
                window.addAfterCloseListener(e1 -> {
                    benhNhansDl.load();
                });
                window.open();
            });

            // Nút Xóa
            JmixButton deleteButton = uiComponents.create(JmixButton.class);
            deleteButton.setText("Xóa");
            deleteButton.addClickListener(e -> {
                benhNhansDataGrid.select(benhNhan);
                benhNhansDataGridRemoveAction.execute();
            });

            // Thêm 3 nút vào layout
            actionsLayout.add(readButton);
            actionsLayout.add(editButton);
            actionsLayout.add(deleteButton);

            return actionsLayout;
        }).setHeader("Thao tác").setAutoWidth(true);
    }

    @Supply(to = "benhNhansDataGrid.gioiTinh", subject = "renderer")
    private Renderer<BenhNhan> benhNhansDataGridGioiTinhRenderer() {
        return new ComponentRenderer<>(benhnhan -> {
            // TODO: create suitable component
            Span span = uiComponents.create(Span.class);
            if (benhnhan.getGioiTinh()!=null) {
                span.setText(messages.getMessage(benhnhan.getGioiTinh()));
                span.addClassName("gioi_tinh");
            }
            return span;
        });
    }
}