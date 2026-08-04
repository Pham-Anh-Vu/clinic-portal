package com.company.clinicportal.view.donthuoc;

import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.entity.DonThuocChiTiet;
import com.company.clinicportal.enumentity.LoaiDon;
import com.company.clinicportal.service.DonThuocService;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.button.Button;
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
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
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
    private CollectionLoader<DonThuocChiTiet> chiTietsDl;
    @ViewComponent
    private DataGrid<DonThuocChiTiet> chiTietDataGrid;
    @ViewComponent
    private com.vaadin.flow.component.html.Span thuocCountLabel;
    @ViewComponent
    private JmixButton addThuocButton;
    @ViewComponent
    private JmixButton issueBtn;
    @ViewComponent
    private JmixButton saveDraftBtn;
    @ViewComponent
    private JmixButton cancelDonBtn;
    @ViewComponent
    private JmixButton closeBtn;

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
        addThuocButton.addClickListener(e -> onAddThuoc());
        issueBtn.addClickListener(e -> onIssue(e));
        saveDraftBtn.addClickListener(e -> onSaveDraft(e));
        cancelDonBtn.addClickListener(e -> onCancelDon(e));
        closeBtn.addClickListener(e -> close(StandardOutcome.CLOSE));
        configureActionsColumn();
    }

    /** Thêm cột "Thao tác" với nút Sửa / Xoá cho mỗi dòng. */
    private void configureActionsColumn() {
        Grid.Column<DonThuocChiTiet> col = chiTietDataGrid.addComponentColumn(this::buildActionsCell);
        col.setHeader("");
        col.setWidth("7em");
        col.setFlexGrow(0);
        col.setSortable(false);
    }

    private HorizontalLayout buildActionsCell(DonThuocChiTiet ct) {
        HorizontalLayout actions = uiComponents.create(HorizontalLayout.class);
        actions.setSpacing(false);
        actions.setPadding(false);

        JmixButton editBtn = uiComponents.create(JmixButton.class);
        editBtn.setIcon(VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editBtn.setTitle("Sửa");
        editBtn.addClickListener(e -> openEditModal(ct));

        JmixButton delBtn = uiComponents.create(JmixButton.class);
        delBtn.setIcon(VaadinIcon.TRASH.create());
        delBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        delBtn.setTitle("Xoá");
        delBtn.addClickListener(e -> confirmAndDelete(ct));

        actions.add(editBtn, delBtn);
        return actions;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        // SỬA đơn đã có: load từ id truyền vào, không tự tạo draft mới.
        if (donThuocIdParam != null) {
            DonThuoc existing = dataManager.load(DonThuoc.class)
                    .id(donThuocIdParam)
                    .fetchPlan(fp -> fp
                            .addFetchPlan("_base")
                            .add("chiTiets", b -> b.addFetchPlan("_base")))
                    .one();
            donThuocDc.setItem(existing);
            chiTietsDl.setParameter("donThuoc", existing);
            return;
        }
        // THÊM MỚI: phải có chiTietDieuTri thì mới tạo draft.
        if (chiTietDieuTri == null) {
            close(StandardOutcome.CLOSE);
            return;
        }
        DonThuoc draft = donThuocService.createDraftForChiTietDieuTri(chiTietDieuTri, DEFAULT_TEN_BAC_SI);
        donThuocDc.setItem(draft);
        chiTietsDl.setParameter("donThuoc", draft);
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        chiTietsDl.load();
        updateCountLabel();
    }

    private void updateCountLabel() {
        thuocCountLabel.setText(chiTietsDc.getItems().size() + " dòng");
    }

    private void onAddThuoc() {
        DonThuoc dt = getEditedEntity();
        if (dt == null) return;
        if (dt.getId() == null) {
            try {
                dt = donThuocService.saveDraft(dt);
                donThuocDc.setItem(dt);
                chiTietsDl.setParameter("donThuoc", dt);
            } catch (Exception ex) {
                notifications.create("Không thể tạo đơn: " + ex.getMessage())
                        .withType(Notifications.Type.ERROR).show();
                return;
            }
        }
        DonThuocChiTiet newRow = donThuocService.addEmptyDrugLine(dt, nextStt());
        openEditModal(newRow);
    }

    private void confirmAndDelete(DonThuocChiTiet ct) {
        DonThuocChiTiet toDelete = ct;
        dialogs.createOptionDialog()
                .withHeader("Xoá thuốc")
                .withText("Bạn có chắc muốn xoá \"" + orDefault(ct.getTenThuocSnapshot(), "dòng thuốc này") + "\"?")
                .withActions(
                        new io.jmix.flowui.action.DialogAction(io.jmix.flowui.action.DialogAction.Type.NO),
                        new io.jmix.flowui.action.DialogAction(io.jmix.flowui.action.DialogAction.Type.YES)
                                .withHandler(e -> {
                                    dataManager.remove(toDelete);
                                    chiTietsDl.load();
                                    updateCountLabel();
                                    notifications.create("Đã xoá thuốc").withType(Notifications.Type.SUCCESS).show();
                                })
                )
                .withWidth("320px")
                .open();
    }

    private void openEditModal(DonThuocChiTiet ct) {
        // Dùng detail builder thay vì view builder để Jmix đặt entity vào container
        // đúng cách (kèm DataContext) — tránh stale-reference.
        io.jmix.flowui.view.DialogWindow<DonThuocChiTietEditDialog> window =
                dialogWindows.detail(this, DonThuocChiTiet.class)
                        .withViewClass(DonThuocChiTietEditDialog.class)
                        .editEntity(ct)
                        .build();
        window.addAfterCloseListener(closeEvent -> {
            chiTietsDl.load();
            updateCountLabel();
        });
        window.open();
    }

    private void onSaveDraft(com.vaadin.flow.component.ClickEvent<Button> e) {
        try {
            DonThuoc saved = donThuocService.saveDraft(getEditedEntity());
            notifications.create("Đã lưu nháp đơn " + saved.getMaDonThuoc())
                    .withType(Notifications.Type.SUCCESS).show();
            donThuocDc.setItem(saved);
        } catch (Exception ex) {
            throw new ValidationException("Lỗi khi lưu nháp: " + ex.getMessage());
        }
    }

    private void onIssue(com.vaadin.flow.component.ClickEvent<Button> e) {
        try {
            donThuocService.saveDraft(getEditedEntity());
        } catch (Exception ex) {
            throw new ValidationException("Lỗi khi lưu đơn: " + ex.getMessage());
        }
        List<String> errors = donThuocService.validateForIssue(getEditedEntity());
        if (!errors.isEmpty()) {
            throw new ValidationException("Đơn chưa đủ điều kiện phát hành:\n- " + String.join("\n- ", errors));
        }
        try {
            com.company.clinicportal.lienthong.entity.LienThongDonThuocOutbox job =
                    donThuocService.submitForSending(getEditedEntity());
            if (job != null) {
                notifications.create("Đã đưa vào hàng chờ gửi. Mã: " + job.getMaDonThuoc())
                        .withType(Notifications.Type.SUCCESS).show();
            } else {
                notifications.create("Đơn đã lưu (test mode / outbox chưa sẵn sàng). Đã bỏ qua bước gửi liên thông.")
                        .withType(Notifications.Type.WARNING).show();
            }
        } catch (Exception ex) {
            throw new ValidationException("Lỗi enqueue: " + ex.getMessage());
        }
        close(StandardOutcome.SAVE);
    }

    private void onCancelDon(com.vaadin.flow.component.ClickEvent<Button> e) {
        try {
            donThuocService.cancel(getEditedEntity(), "Bác sĩ huỷ đơn từ dialog quick-add");
            notifications.create("Đã huỷ đơn").withType(Notifications.Type.SUCCESS).show();
        } catch (Exception ex) {
            throw new ValidationException("Không thể huỷ đơn: " + ex.getMessage());
        }
        close(StandardOutcome.CLOSE);
    }

    private int nextStt() {
        return chiTietsDc.getItems().size() + 1;
    }

    private static String orDefault(String s, String dflt) {
        return (s == null || s.isBlank()) ? dflt : s;
    }

    /** Đảm bảo loại đơn mặc định nếu user chưa chọn (tránh null khi validate liên thông). */
    @Subscribe
    public void onValidation(ValidationEvent event) {
        if (getEditedEntity() != null && getEditedEntity().getLoaiDonEnum() == null) {
            getEditedEntity().setLoaiDonEnum(LoaiDon.THUONG);
        }
    }
}
