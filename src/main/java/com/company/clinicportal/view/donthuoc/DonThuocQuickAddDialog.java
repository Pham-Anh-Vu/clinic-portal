package com.company.clinicportal.view.donthuoc;

import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.entity.DonThuocChiTiet;
import com.company.clinicportal.entity.DonThuocChanDoan;
import com.company.clinicportal.entity.Icd10;
import com.company.clinicportal.enumentity.LoaiDon;
import com.company.clinicportal.enumentity.TrangThaiDonThuoc;
import com.company.clinicportal.service.DonThuocService;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

/**
 * Dialog "Thêm mới đơn thuốc" — màn full: 9 trường header + bảng Danh sách thuốc (5 cột).
 *
 * <p>Cột: STT | Thuốc | Mã thuốc | Đơn vị tính | Số lượng | Cách dùng | ⋮
 * Mỗi dòng có nút ✏️ Sửa (mở modal) và 🗑 Xoá (confirm).</p>
 *
 * <p>Mở từ tab Đơn thuốc của ChiTietDieuTri qua {@link #setChiTietDieuTri(ChiTietDieuTri)}.</p>
 */
@Route(value = "don-thuoc-quick-add", layout = MainView.class)
@ViewController(id = "DonThuocQuickAddDialog")
@ViewDescriptor(path = "don-thuoc-quick-add-dialog.xml")
@EditedEntityContainer("donThuocDc")
@DialogMode(width = "90%", height = "90%", resizable = true)
public class DonThuocQuickAddDialog extends StandardDetailView<DonThuoc> {
    private static final Logger log = LoggerFactory.getLogger(DonThuocQuickAddDialog.class);

    /** Bác sĩ kê đơn mặc định (dev placeholder). */
    public static final String DEFAULT_TEN_BAC_SI = "BS. Đặng Thị Hà";

    @Autowired
    private DataManager dataManager;
    @Autowired
    private DonThuocService donThuocService;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private UiComponents uiComponents;

    @ViewComponent
    private InstanceContainer<DonThuoc> donThuocDc;
    @ViewComponent
    private CollectionContainer<DonThuocChiTiet> chiTietsDc;
    @ViewComponent
    private CollectionContainer<DonThuocChanDoan> chanDoansDc;
    @ViewComponent
    private DataGrid<DonThuocChiTiet> chiTietDataGrid;
    @ViewComponent
    private DataGrid<DonThuocChanDoan> chanDoanDataGrid;
    @ViewComponent
    private com.vaadin.flow.component.html.Span thuocCountLabel;
    @ViewComponent
    private com.vaadin.flow.component.html.Span chanDoanCountLabel;
    @ViewComponent
    private JmixButton issueBtn;
    @ViewComponent
    private JmixButton saveDraftBtn;
    @ViewComponent
    private JmixButton cancelDonBtn;
    @ViewComponent
    private JmixButton closeBtn;
    @ViewComponent
    private TypedTextField<String> maDonThuocField;
    @ViewComponent
    private JmixSelect<com.company.clinicportal.enumentity.LoaiDon> loaiDonField;

    private ChiTietDieuTri chiTietDieuTri;
    private UUID donThuocIdParam;

    /** View gọi truyền ChiTietDieuTri đang chọn trước khi mở dialog (chế độ THÊM MỚI). */
    public void setChiTietDieuTri(ChiTietDieuTri ctx) {
        this.chiTietDieuTri = ctx;
    }

    /** View gọi truyền id đơn thuốc đã có trước khi mở dialog (chế độ SỬA). */
    public void setDonThuocId(UUID id) {
        this.donThuocIdParam = id;
    }

    @Subscribe
    public void onInit(InitEvent event) {
        // Buttons dùng Jmix actions (action="chiTietDataGrid.create" trong XML)
        issueBtn.addClickListener(e -> onIssue(e));
        saveDraftBtn.addClickListener(e -> onSaveDraft(e));
        cancelDonBtn.addClickListener(e -> onCancelDon(e));
        closeBtn.addClickListener(e -> close(StandardOutcome.CLOSE));
        configureThuocActionsColumn();
        configureChanDoanActionsColumn();
    }

    /**
     * Khi user đổi loại đơn → cập nhật loại đơn trên entity VÀ sinh lại mã đơn với hậu tố đúng.
     */
    @Subscribe("loaiDonField")
    public void onLoaiDonValueChange(
            HasValue.ValueChangeEvent<com.company.clinicportal.enumentity.LoaiDon> event) {
        DonThuoc dt = getEditedEntity();
        if (dt != null && event.getValue() != null) {
            // 1. Cập nhật loại đơn trên entity TRƯỚC
            dt.setLoaiDon(event.getValue());
            // 2. Sinh mã mới với hậu tố đúng (-c / -h / -n / -y)
            String newMa = donThuocService.regenerateMaDonThuoc(dt, event.getValue());
            dt.setMaDonThuoc(newMa);
            // 3. Cập nhật UI
            try {
                maDonThuocField.setValue(newMa);
            } catch (Exception e) {}        }
    }

    @Subscribe("loaiDonField")
    public void onLoaiDonFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixSelect<LoaiDon>, LoaiDon> event) {
        DonThuoc dt = getEditedEntity();
        if (dt != null && event.getValue() != null) {
            // 1. Cập nhật loại đơn trên entity TRƯỚC
            dt.setLoaiDon(event.getValue());
            // 2. Sinh mã mới với hậu tố đúng (-c / -h / -n / -y)
            String newMa = donThuocService.regenerateMaDonThuoc(dt, event.getValue());
            dt.setMaDonThuoc(newMa);
            // 3. Cập nhật UI
            try {
                maDonThuocField.setValue(newMa);
            } catch (Exception e) {}
        }
    }

    /**
     * Jmix standard action: tạo dòng thuốc mới, mở modal edit.
     * Pattern tham khảo từ ChiTietDieuTriSBADetailView.onChiTietDichVusDataGridCreate.
     */
    @Subscribe("chiTietDataGrid.create")
    public void onChiTietDataGridCreate(final ActionPerformedEvent event) {
        DialogWindow<DonThuocChiTietEditDialog> window =
                dialogWindows.detail(this, DonThuocChiTiet.class)
                        .withViewClass(DonThuocChiTietEditDialog.class)
                        .newEntity()
                        .withParentDataContext(getViewData().getDataContext())
                        .build();

        window.addAfterCloseListener(closeEvent -> {
            if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                DonThuocChiTiet saved = closeEvent.getView().getEditedEntity();
                // Set parent reference và stt
                saved.setDonThuoc(getEditedEntity());
                saved.setStt(nextSttThuoc());
                // Thêm vào container để hiển thị trên grid
                chiTietsDc.getMutableItems().add(saved);
                updateThuocCountLabel();
            }
        });
        window.open();
    }

    /**
     * Jmix standard action: tạo chẩn đoán mới, mở modal edit.
     */
    @Subscribe("chanDoanDataGrid.create")
    public void onChanDoanDataGridCreate(final ActionPerformedEvent event) {
        DialogWindow<DonThuocChanDoanEditDialog> window =
                dialogWindows.detail(this, DonThuocChanDoan.class)
                        .withViewClass(DonThuocChanDoanEditDialog.class)
                        .newEntity()
                        .withParentDataContext(getViewData().getDataContext())
                        .build();

        window.addAfterCloseListener(closeEvent -> {
            if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                DonThuocChanDoan saved = closeEvent.getView().getEditedEntity();
                // Set parent reference và stt
                saved.setDonThuoc(getEditedEntity());
                saved.setStt(nextSttChanDoan());
                // Thêm vào container để hiển thị trên grid
                chanDoansDc.getMutableItems().add(saved);
                updateChanDoanCountLabel();
            }
        });
        window.open();
    }

    @Subscribe("chiTietDataGrid.remove")
    public void onChiTietDataGridRemove(final ActionPerformedEvent event) {
        DonThuocChiTiet selected = chiTietDataGrid.getSingleSelectedItem();
        if (selected != null) {
            dialogs.createOptionDialog()
                    .withHeader("Xoá thuốc")
                    .withText("Bạn có chắc muốn xoá \""
                            + orDefault(selected.getTenThuocSnapshot(), "dòng thuốc này") + "\"?")
                    .withActions(
                            new io.jmix.flowui.action.DialogAction(
                                    io.jmix.flowui.action.DialogAction.Type.NO),
                            new io.jmix.flowui.action.DialogAction(
                                    io.jmix.flowui.action.DialogAction.Type.YES)
                                    .withHandler(e -> {
                                        chiTietsDc.getMutableItems().remove(selected);
                                        updateThuocCountLabel();
                                        notifications.create("Đã xoá thuốc")
                                                .withType(Notifications.Type.SUCCESS).show();
                                    }))
                    .withWidth("320px")
                    .open();
        }
    }

    @Subscribe("chanDoanDataGrid.remove")
    public void onChanDoanDataGridRemove(final ActionPerformedEvent event) {
        DonThuocChanDoan selected = chanDoanDataGrid.getSingleSelectedItem();
        if (selected != null) {
            dialogs.createOptionDialog()
                    .withHeader("Xoá chuẩn đoán")
                    .withText("Bạn có chắc muốn xoá chuẩn đoán này?")
                    .withActions(
                            new io.jmix.flowui.action.DialogAction(
                                    io.jmix.flowui.action.DialogAction.Type.NO),
                            new io.jmix.flowui.action.DialogAction(
                                    io.jmix.flowui.action.DialogAction.Type.YES)
                                    .withHandler(e -> {
                                        chanDoansDc.getMutableItems().remove(selected);
                                        updateChanDoanCountLabel();
                                        notifications.create("Đã xoá chuẩn đoán")
                                                .withType(Notifications.Type.SUCCESS).show();
                                    }))
                    .withWidth("320px")
                    .open();
        }
    }

    /** Thêm cột "Thao tác" với nút Sửa cho mỗi dòng thuốc. */
    private void configureThuocActionsColumn() {
        Grid.Column<DonThuocChiTiet> col = chiTietDataGrid.addComponentColumn(this::buildThuocActionsCell);
        col.setHeader("");
        col.setWidth("7em");
        col.setFlexGrow(0);
        col.setSortable(false);
    }

    private HorizontalLayout buildThuocActionsCell(DonThuocChiTiet ct) {
        HorizontalLayout actions = uiComponents.create(HorizontalLayout.class);
        actions.setSpacing(false);
        actions.setPadding(false);

        JmixButton editBtn = uiComponents.create(JmixButton.class);
        editBtn.setIcon(VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editBtn.setTitle("Sửa");
        editBtn.addClickListener(e -> openThuocEditModal(ct));

        JmixButton delBtn = uiComponents.create(JmixButton.class);
        delBtn.setIcon(VaadinIcon.TRASH.create());
        delBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY);
        delBtn.setTitle("Xoá");
        delBtn.addClickListener(e -> {
            chiTietsDc.getMutableItems().remove(ct);
            updateThuocCountLabel();
            notifications.create("Đã xoá thuốc").withType(Notifications.Type.SUCCESS).show();
        });

        actions.add(editBtn, delBtn);
        return actions;
    }

    /** Thêm cột "Thao tác" với nút Sửa cho mỗi dòng chẩn đoán. */
    private void configureChanDoanActionsColumn() {
        Grid.Column<DonThuocChanDoan> col = chanDoanDataGrid.addComponentColumn(
                this::buildChanDoanActionsCell);
        col.setHeader("");
        col.setWidth("7em");
        col.setFlexGrow(0);
        col.setSortable(false);
    }

    private HorizontalLayout buildChanDoanActionsCell(DonThuocChanDoan cd) {
        HorizontalLayout actions = uiComponents.create(HorizontalLayout.class);
        actions.setSpacing(false);
        actions.setPadding(false);

        JmixButton editBtn = uiComponents.create(JmixButton.class);
        editBtn.setIcon(VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editBtn.setTitle("Sửa");
        editBtn.addClickListener(e -> openChanDoanEditModal(cd));

        JmixButton delBtn = uiComponents.create(JmixButton.class);
        delBtn.setIcon(VaadinIcon.TRASH.create());
        delBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY);
        delBtn.setTitle("Xoá");
        delBtn.addClickListener(e -> {
            chanDoansDc.getMutableItems().remove(cd);
            updateChanDoanCountLabel();
            notifications.create("Đã xoá chuẩn đoán")
                    .withType(Notifications.Type.SUCCESS).show();
        });

        actions.add(editBtn, delBtn);
        return actions;
    }

    /** Mở modal edit dòng thuốc. */
    private void openThuocEditModal(DonThuocChiTiet ct) {
        io.jmix.flowui.view.DialogWindow<DonThuocChiTietEditDialog> window =
                dialogWindows.detail(this, DonThuocChiTiet.class)
                        .withViewClass(DonThuocChiTietEditDialog.class)
                        .editEntity(ct)
                        .withParentDataContext(getViewData().getDataContext())
                        .build();
        window.open();
    }

    /** Mở modal edit chẩn đoán. */
    private void openChanDoanEditModal(DonThuocChanDoan cd) {
        io.jmix.flowui.view.DialogWindow<DonThuocChanDoanEditDialog> window =
                dialogWindows.detail(this, DonThuocChanDoan.class)
                        .withViewClass(DonThuocChanDoanEditDialog.class)
                        .editEntity(cd)
                        .withParentDataContext(getViewData().getDataContext())
                        .build();
        window.open();
    }

    /** Khi user chọn Icd10 trong cột entityPicker, tự điền các trường snapshot. */
    @Subscribe("chanDoanDataGrid.icd10")
    public void onChanDoanIcd10ValueChange(HasValue.ValueChangeEvent<Icd10> e) {
        if (!e.isFromClient()) {
            return;
        }
        try {
            // Tìm chẩn đoán đang được edit trong editor buffer của Jmix DataGrid.
            // Khi editor bật, item đang edit được track qua EditorImpl; ta tìm
            // item có icd10 vừa được set (khớp với entity user vừa chọn).
            Icd10 chosen = e.getValue();
            if (chosen == null) {
                return;
            }
            DonThuocChanDoan target = chanDoansDc.getItems().stream()
                    .filter(cd -> chosen.equals(cd.getIcd10()))
                    .findFirst()
                    .orElse(null);
            if (target == null) {
                // Có thể chẩn đoán chưa có trong container (đang edit buffer).
                // Fallback: lấy selected item.
                target = chanDoanDataGrid.getSingleSelectedItem();
            }
            if (target != null) {
                donThuocService.pickDiagnosis(target, chosen);
                chanDoanDataGrid.getDataProvider().refreshItem(target);
            }
        } catch (IllegalStateException ex) {
            notifications.create(ex.getMessage())
                    .withType(Notifications.Type.WARNING).show();
        }
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        // SỬA đơn đã có: load từ id truyền vào, không tự tạo draft mới.
        if (donThuocIdParam != null) {
            DonThuoc existing = dataManager.load(DonThuoc.class)
                    .id(donThuocIdParam)
                    .fetchPlan(fp -> fp
                            .addFetchPlan("_base")
                            .add("chiTiets", b -> b.addFetchPlan("_base"))
                            .add("chanDoans", b -> b.addFetchPlan("_base")))
                    .one();
            donThuocDc.setItem(existing);
            return;
        }
        // THÊM MỚI: phải có chiTietDieuTri thì mới tạo draft.
        if (chiTietDieuTri == null) {
            close(StandardOutcome.CLOSE);
            return;
        }
        DonThuoc draft = donThuocService.createDraftForChiTietDieuTri(
                chiTietDieuTri, DEFAULT_TEN_BAC_SI);
        donThuocDc.setItem(draft);
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        updateThuocCountLabel();
        updateChanDoanCountLabel();
    }

    private void updateThuocCountLabel() {
        thuocCountLabel.setText(chiTietsDc.getItems().size() + " dòng");
    }

    private void updateChanDoanCountLabel() {
        chanDoanCountLabel.setText(chanDoansDc.getItems().size() + " dòng");
    }

    private int nextSttThuoc() {
        return chiTietsDc.getItems().size() + 1;
    }

    private int nextSttChanDoan() {
        return chanDoansDc.getItems().size() + 1;
    }

    private void onSaveDraft(com.vaadin.flow.component.ClickEvent<Button> e) {
        try {
            DonThuoc dt = getEditedEntity();
            if (dt == null || dt.getId() == null) {
                notifications.create("Đơn thuốc chưa được tạo")
                        .withType(Notifications.Type.WARNING).show();
                return;
            }
            // Reload để tránh OptimisticLockException
            DonThuoc managed = dataManager.load(DonThuoc.class)
                    .id(dt.getId())
                    .fetchPlan(fp -> fp
                            .addFetchPlan("_base")
                            .add("chiTiets", b -> b.addFetchPlan("_base"))
                            .add("chanDoans", b -> b.addFetchPlan("_base"))
                            .add("dotDungs", b -> b.addFetchPlan("_base")))
                    .one();
            donThuocDc.setItem(managed);
            DonThuoc saved = donThuocService.saveDraft(managed);
            donThuocDc.setItem(saved);
            notifications.create("Đã lưu nháp đơn " + saved.getMaDonThuoc())
                    .withType(Notifications.Type.SUCCESS).show();
        } catch (Exception ex) {
            throw new ValidationException("Lỗi khi lưu nháp: " + ex.getMessage());
        }
    }

    private void onIssue(com.vaadin.flow.component.ClickEvent<Button> e) {
        DonThuoc dt = getEditedEntity();
        if (dt.getTrangThaiEnum() == TrangThaiDonThuoc.NHAP) {
            dt.setTrangThaiEnum(TrangThaiDonThuoc.CHO_GUI);
        }
        List<String> errors = donThuocService.validateForIssue(dt);
        if (!errors.isEmpty()) {
            throw new ValidationException(
                    "Đơn chưa đủ điều kiện phát hành:\n- "
                            + String.join("\n- ", errors));
        }
        try {
            DonThuoc saved = donThuocService.saveDraft(dt);
            notifications.create(
                            "Đã lưu đơn " + saved.getMaDonThuoc()
                                    + " (trạng thái chờ gửi). Bấm Đồng bộ để gửi liên thông.")
                    .withType(Notifications.Type.SUCCESS).show();
        } catch (Exception ex) {
            throw new ValidationException("Lỗi khi lưu đơn: " + ex.getMessage());
        }
        close(StandardOutcome.DISCARD);
    }

    private void onCancelDon(com.vaadin.flow.component.ClickEvent<Button> e) {
        try {
            donThuocService.cancel(getEditedEntity(),
                    "Bác sĩ huỷ đơn từ dialog quick-add");
            notifications.create("Đã huỷ đơn")
                    .withType(Notifications.Type.SUCCESS).show();
        } catch (Exception ex) {
            throw new ValidationException("Không thể huỷ đơn: " + ex.getMessage());
        }
        close(StandardOutcome.CLOSE);
    }

    private static String orDefault(String s, String dflt) {
        return (s == null || s.isBlank()) ? dflt : s;
    }

    /** Đảm bảo loại đơn mặc định nếu user chưa chọn. */
    @Subscribe
    public void onValidation(ValidationEvent event) {
        if (getEditedEntity() != null && getEditedEntity().getLoaiDon() == null) {
            getEditedEntity().setLoaiDon(LoaiDon.THUONG);
        }
    }
}
