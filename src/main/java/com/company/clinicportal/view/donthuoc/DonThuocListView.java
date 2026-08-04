package com.company.clinicportal.view.donthuoc;

import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.lienthong.LienThongOutboxService;
import com.company.clinicportal.lienthong.entity.LienThongDonThuocOutbox;
import com.company.clinicportal.view.lienthong.LienThongSyncDialogView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Route(value = "don-thuocs", layout = MainView.class)
@ViewController(id = "ltcs_DonThuoc.list")
@ViewDescriptor(path = "don-thuoc-list-view.xml")
@LookupComponent("donThuocsDataGrid")
@DialogMode(width = "80em")
public class DonThuocListView extends StandardListView<DonThuoc> {

    @Autowired
    private DataManager dataManager;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private io.jmix.flowui.Dialogs dialogs;

    @ViewComponent
    private DataGrid<DonThuoc> donThuocsDataGrid;
    @ViewComponent
    private io.jmix.flowui.model.CollectionLoader<DonThuoc> donThuocsDl;
    @ViewComponent
    private JmixButton retryBtn;
    @ViewComponent
    private JmixButton syncBtn;
    @ViewComponent
    private JmixButton bulkSyncBtn;
    @ViewComponent
    private JmixButton createBtn;
    @ViewComponent
    private JmixButton editBtn;
    @ViewComponent
    private JmixButton readBtn;
    @ViewComponent
    private JmixButton removeBtn;

    @Subscribe
    public void onInit(InitEvent event) {
        // Bulk buttons
        if (retryBtn != null) retryBtn.addClickListener(e -> onRetry());
        if (syncBtn != null) syncBtn.addClickListener(e -> onSyncNow());
        if (bulkSyncBtn != null) bulkSyncBtn.addClickListener(e -> onBulkSync());

        // Action buttons (top toolbar)
        if (createBtn != null) createBtn.addClickListener(e -> onCreate());
        if (editBtn != null) editBtn.addClickListener(e -> onEditSelected());
        if (readBtn != null) readBtn.addClickListener(e -> onReadSelected());
        if (removeBtn != null) removeBtn.addClickListener(e -> onRemoveSelected());

        // Per-row action column
        configureActionsColumn();
    }

    /** Thêm cột "Thao tác" với nút Sửa / Xoá trên mỗi dòng. */
    private void configureActionsColumn() {
        Grid.Column<DonThuoc> col = donThuocsDataGrid.addComponentColumn(this::buildActionsCell);
        col.setHeader("Thao tác");
        col.setWidth("7em");
        col.setFlexGrow(0);
    }

    private HorizontalLayout buildActionsCell(DonThuoc donThuoc) {
        HorizontalLayout actions = uiComponents.create(HorizontalLayout.class);
        actions.setSpacing(false);
        actions.setPadding(false);

        JmixButton editBtn = uiComponents.create(JmixButton.class);
        editBtn.setIcon(VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editBtn.setTitle("Sửa");
        editBtn.addClickListener(e -> openDetail(donThuoc, true));

        JmixButton delBtn = uiComponents.create(JmixButton.class);
        delBtn.setIcon(VaadinIcon.TRASH.create());
        delBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        delBtn.setTitle("Xoá");
        delBtn.addClickListener(e -> confirmAndDelete(donThuoc));

        actions.add(editBtn, delBtn);
        return actions;
    }

    // ===== Top toolbar actions =====
    private void onCreate() {
        dialogWindows.detail(this, DonThuoc.class)
                .withViewClass(DonThuocDetailView.class)
                .newEntity()
                .build()
                .open();
    }

    private void onEditSelected() {
        DonThuoc selected = donThuocsDataGrid.getSingleSelectedItem();
        if (selected == null) {
            notifications.create("Chọn 1 đơn thuốc để sửa.")
                    .withType(Notifications.Type.WARNING).show();
            return;
        }
        openDetail(selected, true);
    }

    private void onReadSelected() {
        DonThuoc selected = donThuocsDataGrid.getSingleSelectedItem();
        if (selected == null) {
            notifications.create("Chọn 1 đơn thuốc để xem.")
                    .withType(Notifications.Type.WARNING).show();
            return;
        }
        openDetail(selected, false);
    }

    private void onRemoveSelected() {
        DonThuoc selected = donThuocsDataGrid.getSingleSelectedItem();
        if (selected == null) {
            notifications.create("Chọn 1 đơn thuốc để xoá.")
                    .withType(Notifications.Type.WARNING).show();
            return;
        }
        confirmAndDelete(selected);
    }

    private void openDetail(DonThuoc dt, boolean editable) {
        io.jmix.flowui.view.DialogWindow<DonThuocDetailView> window =
                dialogWindows.detail(this, DonThuoc.class)
                        .withViewClass(DonThuocDetailView.class)
                        .editEntity(dt)
                        .build();
        // ReadOnly mode: đóng sau khi đóng modal → không lưu (nếu user không thay đổi)
        // Jmix StandardDetailView mặc định vẫn cho lưu; nếu cần readOnly thật, thêm ReadOnlyAwareView.
        window.addAfterCloseListener(e -> donThuocsDl.load());
        window.open();
    }

    private void confirmAndDelete(DonThuoc dt) {
        if (dt == null) return;
        io.jmix.flowui.action.DialogAction yesAction =
                new io.jmix.flowui.action.DialogAction(io.jmix.flowui.action.DialogAction.Type.YES)
                        .withHandler(e -> {
                            try {
                                dataManager.remove(dt);
                                notifications.create("Đã xoá đơn " + dt.getMaDonThuoc())
                                        .withType(Notifications.Type.SUCCESS).show();
                                donThuocsDl.load();
                            } catch (Exception ex) {
                                notifications.create("Không thể xoá: " + ex.getMessage())
                                        .withType(Notifications.Type.ERROR).show();
                            }
                        });
        io.jmix.flowui.view.View<?> self = this;
        dialogs.createOptionDialog()
                .withHeader("Xoá đơn thuốc")
                .withText("Bạn có chắc muốn xoá đơn \"" + dt.getMaDonThuoc() + "\"?")
                .withActions(
                        new io.jmix.flowui.action.DialogAction(io.jmix.flowui.action.DialogAction.Type.NO),
                        yesAction
                )
                .withWidth("320px")
                .open();
    }

    // ===== Existing bulk operations =====
    private void onBulkSync() {
        dialogWindows.view(this, LienThongSyncDialogView.class).open();
    }

    private void onRetry() {
        DonThuoc selected = donThuocsDataGrid.getSingleSelectedItem();
        if (selected == null) {
            notifications.create("Chọn 1 đơn thuốc để retry.")
                    .withType(Notifications.Type.WARNING).show();
            return;
        }
        Optional<LienThongDonThuocOutbox> job = dataManager.load(LienThongDonThuocOutbox.class)
                .query("select e from ltcs_LienThongDonThuocOutbox e where e.maDonThuoc = :m")
                .parameter("m", selected.getMaDonThuoc())
                .optional();
        if (job.isEmpty()) {
            String key = selected.getIdempotencyKey();
            if (key == null || key.isBlank()) {
                key = "eco-" + System.currentTimeMillis() + "-" + Math.abs(selected.getMaDonThuoc().hashCode());
                selected.setIdempotencyKey(key);
                dataManager.save(selected);
            }
            notifications.create("Đơn chưa có job liên thông. Đã tạo job mới (idempotency=" + key + ").")
                    .withType(Notifications.Type.DEFAULT).show();
            return;
        }
        LienThongDonThuocOutbox j = job.get();
        j.setStatus(LienThongOutboxService.STATUS_PENDING);
        j.setNextAttemptAt(Date.from(Instant.now()));
        j.setAttempt(0);
        j.setLastError(null);
        j.setUpdatedAt(Date.from(Instant.now()));
        dataManager.save(j);
        notifications.create("Đã đặt lại job. Worker sẽ xử lý trong vòng 30s.")
                .withType(Notifications.Type.SUCCESS).show();
    }

    @Transactional
    private void onSyncNow() {
        DonThuoc selected = donThuocsDataGrid.getSingleSelectedItem();
        if (selected == null) {
            notifications.create("Chọn 1 đơn thuốc để đồng bộ.")
                    .withType(Notifications.Type.WARNING).show();
            return;
        }
        notifications.create("Sử dụng 'Retry' cho đơn thất bại. Đồng bộ tay đang được phát triển.")
                .withType(Notifications.Type.DEFAULT).show();
    }
}
