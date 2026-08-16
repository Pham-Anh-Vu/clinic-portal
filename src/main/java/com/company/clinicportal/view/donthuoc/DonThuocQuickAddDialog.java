package com.company.clinicportal.view.donthuoc;

import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.entity.DonThuocChiTiet;
import com.company.clinicportal.entity.DonThuocChanDoan;
import com.company.clinicportal.entity.DonThuocDotDung;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
@DialogMode(width = "60%", height = "90%", resizable = true)
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
    private InstanceContainer<DonThuocDotDung> dotDungDc;
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
    @ViewComponent
    private JmixSelect<com.company.clinicportal.enumentity.HinhThucDieuTri> hinhThucDieuTriField;
    @ViewComponent
    private com.vaadin.flow.component.textfield.TextField soDotField;
    @ViewComponent
    private com.vaadin.flow.component.datepicker.DatePicker tuNgayField;
    @ViewComponent
    private com.vaadin.flow.component.datepicker.DatePicker denNgayField;

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
        // Áp dụng trạng thái required ban đầu cho "Hình thức điều trị" theo loại đơn hiện tại
        try{
            applyHinhThucDieuTriRequired(
                    getEditedEntity() != null ? getEditedEntity().getLoaiDon() : null);
        } catch (Exception e){ }
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
        // Cập nhật trạng thái required của "Hình thức điều trị" theo loại đơn
        applyHinhThucDieuTriRequired(event.getValue());
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
        applyHinhThucDieuTriRequired(event.getValue());
    }

    /**
     * Bật/tắt dấu * bắt buộc của trường "Hình thức điều trị" tuỳ theo loại đơn.
     * Chỉ đơn thuốc thông thường ({@link LoaiDon#THUONG}) mới bắt buộc nhập.
     */
    private void applyHinhThucDieuTriRequired(com.company.clinicportal.enumentity.LoaiDon loaiDon) {
        if (hinhThucDieuTriField == null) {
            return;
        }
        boolean required = loaiDon == com.company.clinicportal.enumentity.LoaiDon.THUONG;
        try {
            hinhThucDieuTriField.setRequiredIndicatorVisible(required);
            hinhThucDieuTriField.setRequired(true);
        } catch (Exception ignore) {
            // API có thể không tồn tại trên một số phiên bản Jmix; bỏ qua nếu vậy.
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

//    /**
//     * Jmix standard action: tạo chẩn đoán mới, mở modal edit.
//     */
//    @Subscribe("chanDoanDataGrid.create")
//    public void onChanDoanDataGridCreate(final ActionPerformedEvent event) {
//        DialogWindow<DonThuocChanDoanEditDialog> window =
//                dialogWindows.detail(this, DonThuocChanDoan.class)
//                        .withViewClass(DonThuocChanDoanEditDialog.class)
//                        .newEntity()
//                        .withParentDataContext(getViewData().getDataContext())
//                        .build();
//
//        window.addAfterCloseListener(closeEvent -> {
//            if (closeEvent.closedWith(StandardOutcome.SAVE)) {
//                DonThuocChanDoan saved = closeEvent.getView().getEditedEntity();
//                // Set parent reference và stt
//                saved.setDonThuoc(getEditedEntity());
//                saved.setStt(nextSttChanDoan());
//                // Thêm vào container để hiển thị trên grid
//                chanDoansDc.getMutableItems().add(saved);
//                updateChanDoanCountLabel();
//            }
//        });
//        window.open();
//    }

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
                    .withHeader("Xoá chẩn đoán")
                    .withText("Bạn có chắc muốn xoá chẩn đoán này?")
                    .withActions(
                            new io.jmix.flowui.action.DialogAction(
                                    io.jmix.flowui.action.DialogAction.Type.NO),
                            new io.jmix.flowui.action.DialogAction(
                                    io.jmix.flowui.action.DialogAction.Type.YES)
                                    .withHandler(e -> {
                                        chanDoansDc.getMutableItems().remove(selected);
                                        updateChanDoanCountLabel();
                                        notifications.create("Đã xoá chẩn đoán")
                                                .withType(Notifications.Type.SUCCESS).show();
                                    }))
                    .withWidth("320px")
                    .open();
        }
    }

    /**
     * Bind dòng {@link DonThuocDotDung} đầu tiên của đơn thuốc hiện tại vào
     * {@link #dotDungDc} để form 3 trường (Đợt / Từ ngày / Đến ngày) hiển thị
     * giá trị. Service đã đảm bảo luôn có ít nhất 1 đợt dùng (THÊM MỚI tạo kèm
     * sẵn soDot=1, SỬA load đầy đủ {@code dotDungs} fetch plan).
     */
    private void initDotDungInstance() {
        DonThuoc dt = getEditedEntity();
        if (dt == null || dotDungDc == null) {
            return;
        }
        DonThuocDotDung dd = null;
        if (dt.getDotDungs() != null && !dt.getDotDungs().isEmpty()) {
            dd = dt.getDotDungs().get(0);
        }
        if (dd == null) {
            // Fallback: tạo mới nếu vì lý do gì đó collection rỗng (vd. service
            // chưa được gọi). Đảm bảo form luôn có 1 instance binding.
            dd = dataManager.create(DonThuocDotDung.class);
            dd.setDonThuoc(dt);
            dd.setSoDot(1);
            dt.getDotDungs().add(dd);
        }
        dotDungDc.setItem(dd);
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
            notifications.create("Đã xoá chẩn đoán")
                    .withType(Notifications.Type.SUCCESS).show();
        });

        actions.add(editBtn);
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
                            .add("chanDoans", b -> b.addFetchPlan("_base"))
                            .add("dotDungs", b -> b.addFetchPlan("_base")))
                    .one();
            donThuocDc.setItem(existing);
            applyHinhThucDieuTriRequired(existing.getLoaiDon());
            initDotDungInstance();
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
        applyHinhThucDieuTriRequired(draft.getLoaiDon());
        // createDraftForChiTietDieuTri đã tạo sẵn 1 DonThuocDotDung (soDot=1) cho form 3 trường.
        initDotDungInstance();

        // === MAP TẠM danh sách ICD từ ChiTietDieuTri.dsChanDoanIcd ===
        // Không lưu DB — chỉ hiển thị preview trên grid để bác sĩ thấy các
        // ICD đã chẩn đoán ở phiếu điều trị. Khi user nhấn "Lưu nháp" hoặc
        // "Lưu & phát hành" thì mới cascade save DonThuocChanDoan xuống DB.
        mapTamIcdTuChiTietDieuTri(chiTietDieuTri);
    }

    /**
     * Map tạm danh sách ICD từ {@code ChiTietDieuTri.dsChanDoanIcd} vào
     * {@link #chanDoansDc} để hiển thị trên grid. Không lưu DB.
     *
     * <p>Quy tắc:</p>
     * <ul>
     *     <li>Load lại CTDT với fetchPlan đầy đủ cho {@code dsChanDoanIcd}
     *         (tránh LazyInit nếu entity truyền sang không fetch collection).</li>
     *     <li>Tạo {@link DonThuocChanDoan} mới cho mỗi ICD, gọi
     *         {@link DonThuocChanDoan#snapshotFrom(Icd10)} để fill mã/tên snapshot.</li>
     *     <li>Cờ {@code tamFromPhieu = true} đánh dấu dòng tạm (chưa persist).</li>
     *     <li>KHÔNG set {@code donThuoc} trên dòng ICD — để cascade save không
     *         insert vào DB khi user chưa Lưu. Nếu set parent thì save sẽ
     *         insert dòng vào bảng {@code don_thuoc_chan_doan} cùng {@code don_thuoc}.</li>
     *     <li>Nếu user bấm "Lưu nháp" / "Phát hành" mà vẫn còn dòng tạm chưa gắn
     *         parent, hệ thống sẽ tự động gắn trước khi save (xem
     *         {@link #attachChanDoansToEditedEntity()}).</li>
     * </ul>
     */
    private void mapTamIcdTuChiTietDieuTri(ChiTietDieuTri ctx) {
        if (ctx == null || ctx.getId() == null) {
            return;
        }
        // Load lại CTDT với fetchPlan cho dsChanDoanIcd để tránh Lazy
        ChiTietDieuTri ctdt;
        try {
            ctdt = dataManager.load(ChiTietDieuTri.class)
                    .id(ctx.getId())
                    .fetchPlan(fp -> fp
                            .addFetchPlan("_base")
                            .add("dsChanDoanIcd", icd -> icd.addFetchPlan("_base")))
                    .one();
        } catch (Exception ex) {
            log.warn("[MapIcdTam] Không load được CTDT id={}, dùng entity truyền vào: {}",
                    ctx.getId(), ex.getMessage());
            ctdt = ctx;
        }
        Set<Icd10> dsIcd = ctdt.getDsChanDoanIcd();
        if (dsIcd == null || dsIcd.isEmpty()) {
            return;
        }
        int added = 0;
        // Sắp xếp theo mã ICD để hiển thị ổn định
        List<Icd10> sorted = dsIcd.stream()
                .filter(java.util.Objects::nonNull)
                .sorted((a, b) -> {
                    String maA = a.getMaIcd() == null ? "" : a.getMaIcd();
                    String maB = b.getMaIcd() == null ? "" : b.getMaIcd();
                    return maA.compareTo(maB);
                })
                .toList();
        // Bỏ qua các ICD đã có trong grid (tránh trùng khi user mở lại dialog)
        Set<String> existingMaIcd = new HashSet<>();
        for (DonThuocChanDoan cd : chanDoansDc.getItems()) {
            if (cd.getMaIcdSnapshot() != null) {
                existingMaIcd.add(cd.getMaIcdSnapshot());
            }
        }
        for (Icd10 icd : sorted) {
            if (icd.getMaIcd() != null && existingMaIcd.contains(icd.getMaIcd())) {
                continue;
            }
            DonThuocChanDoan cd = dataManager.create(DonThuocChanDoan.class);
            cd.snapshotFrom(icd);
            cd.setStt(nextSttChanDoan());
            cd.setTamFromPhieu(true); // đánh dấu dòng tạm (transient flag)
            // Mặc định kết luận = tên bệnh (text tiếng Việt của ICD-10),
            // bác sĩ có thể sửa lại trên grid sau khi map.
            cd.setKetLuan(cd.getTenIcdSnapshot());
            // CHƯA setDonThuoc → không cascade save khi user chưa bấm Lưu
            chanDoansDc.getMutableItems().add(cd);
            added++;
        }
        if (added > 0) {
            updateChanDoanCountLabel();
            log.info("[MapIcdTam] Đã map tạm {} ICD từ CTDT id={} vào grid chẩn đoán.",
                    added, ctx.getId());
        }
    }

    /**
     * Trước khi save nháp / phát hành, gắn tất cả {@link DonThuocChanDoan} đang
     * nằm trong {@link #chanDoansDc} (kể cả dòng tạm map từ phiếu ĐT) vào
     * {@link DonThuoc} hiện tại để EclipseLink cascade persist.
     *
     * <p><b>Quan trọng:</b></p>
     * <ul>
     *     <li>Phải <b>vừa set parent (donThuoc) vừa add vào {@code dt.getChanDoans()}</b>.
     *         EclipseLink chỉ cascade insert các entity mới nằm trong collection
     *         của parent khi save. Nếu chỉ setDonThuoc mà không add vào collection,
     *         sẽ gây lỗi "new object through relationship not marked PERSIST".</li>
     *     <li>Vì {@link #chanDoansDc} là CollectionPropertyContainer của {@code donThuocDc},
     *         nên {@code chanDoansDc.getMutableItems().add(cd)} đã đồng bộ với
     *         {@code dt.getChanDoans()}. Tuy nhiên ta vẫn gọi {@code dt.getChanDoans().add(cd)}
     *         để đảm bảo an toàn khi {@code chanDoansDc} không còn bind vào {@code dt}.</li>
     * </ul>
     *
     * <p>Được gọi từ {@link #onSaveDraft} và {@link #onIssue}.</p>
     */
    private void attachChanDoansToEditedEntity() {
        DonThuoc dt = getEditedEntity();
        if (dt == null) {
            return;
        }
        int attached = 0;
        for (DonThuocChanDoan cd : chanDoansDc.getItems()) {
            if (cd == null) {
                continue;
            }
            // 1. Set parent để JPA biết FK (id_don_thuoc)
            if (cd.getDonThuoc() == null) {
                cd.setDonThuoc(dt);
            }
            // 2. Add vào collection của parent để EclipseLink cascade INSERT
            //    khi save DonThuoc. Đây là bắt buộc cho @OneToMany(cascade=ALL).
            if (!dt.getChanDoans().contains(cd)) {
                dt.getChanDoans().add(cd);
                attached++;
            }
        }
        if (attached > 0) {
            log.info("[MapIcdTam] Đã gắn {} dòng chẩn đoán (gồm cả tạm) vào DonThuoc.getChanDoans().",
                    attached);
        }
    }

    /**
     * Gắn {@link DonThuocDotDung} đang chỉnh sửa trong {@link #dotDungDc} vào
     * {@link DonThuoc} để EclipseLink cascade persist khi save.
     * Với thiết kế form 3 trường (Đợt / Từ ngày / Đến ngày), mỗi đơn thuốc chỉ
     * quản lý 1 đợt dùng thuốc, nên ta đảm bảo dòng đang sửa nằm trong
     * {@code dt.getDotDungs()}.
     */
    private void attachDotDungToEditedEntity() {
        DonThuoc dt = getEditedEntity();
        if (dt == null) {
            return;
        }
        DonThuocDotDung dd = dotDungDc.getItem();
        if (dd == null) {
            return;
        }
        if (dd.getDonThuoc() == null) {
            dd.setDonThuoc(dt);
        }
        if (!dt.getDotDungs().contains(dd)) {
            dt.getDotDungs().add(dd);
        }
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
            if (dt == null) {
                notifications.create("Đơn thuốc chưa được tạo")
                        .withType(Notifications.Type.WARNING).show();
                return;
            }
            // Bước 1: Gắn các dòng chẩn đoán (kể cả dòng ICD tạm map từ phiếu
            // điều trị) vào dt để EclipseLink cascade persist khi save.
            attachChanDoansToEditedEntity();
            // Đồng thời gắn đợt dùng thuốc đang chỉnh sửa trên form 3 trường vào dt.
            attachDotDungToEditedEntity();

            // Bước 2: Load lại entity managed từ DB để tránh OptimisticLockException.
            // Lưu ý: phải fetch chanDoans, dotDungs để có thể merge dòng tạm vào managed.
            // Dùng optional() thay vì one() vì entity mới tạo (chưa save) có id từ Jmix
            // nhưng chưa tồn tại trong DB → NoResultException nếu dùng one().
            java.util.Optional<DonThuoc> optManaged = dataManager.load(DonThuoc.class)
                    .id(dt.getId())
                    .fetchPlan(fp -> fp
                            .addFetchPlan("_base")
                            .add("chiTiets", b -> b.addFetchPlan("_base"))
                            .add("chanDoans", b -> b.addFetchPlan("_base"))
                            .add("dotDungs", b -> b.addFetchPlan("_base")))
                    .optional();

            DonThuoc managed;
            if (optManaged.isPresent()) {
                // Đơn đã tồn tại trong DB (chế độ SỬA) → merge dữ liệu từ dt vào managed
                managed = optManaged.get();
                mergeChanDoansFromDraft(managed, dt);
                mergeDotDungFromDraft(managed, dt);
                mergeChiTietsFromDraft(managed, dt);
            } else {
                // Đơn mới tạo, chưa lưu lần nào (chế độ THÊM MỚI)
                // Jmix đã gán id cho dt nhưng chưa persist → dùng luôn dt làm managed
                managed = dt;
                // Attach các dòng mới (đã được attach ở bước 1, nhưng đảm bảo đủ)
                attachChanDoansToEditedEntity();
                attachDotDungToEditedEntity();
                log.info("onSaveDraft: đơn mới (chưa persist), dùng dt làm managed.");
            }

            // Bước 3: Cập nhật container để UI hiển thị đúng các dòng đã merge
            // (vì container bind vào một entity duy nhất, phải setItem(managed)
            // để grid re-fetch).
            donThuocDc.setItem(managed);
            // Re-bind dotDungDc sang dòng managed tương ứng (vẫn là 1 đợt duy nhất)
            initDotDungInstance();

            // Bước 4: Save đơn — EclipseLink cascade INSERT các dòng mới,
            // UPDATE các dòng đã thay đổi.
            DonThuoc saved = donThuocService.saveDraft(managed);
            donThuocDc.setItem(saved);
            notifications.create("Đã lưu nháp đơn " + saved.getMaDonThuoc())
                    .withType(Notifications.Type.SUCCESS).show();
            close(StandardOutcome.DISCARD);
        } catch (Exception ex) {
            String detail = ex.getMessage();
            if (detail == null || detail.isBlank()) {
                detail = ex.getClass().getSimpleName();
                if (ex.getCause() != null) {
                    detail += " <- " + ex.getCause().getClass().getSimpleName()
                            + ": " + (ex.getCause().getMessage() != null
                                    ? ex.getCause().getMessage()
                                    : "(no message)");
                }
            }
            log.error("onSaveDraft failed: {}", detail, ex);
            throw new ValidationException("Lỗi khi lưu nháp: " + detail);
        }
    }

    /**
     * Merge các {@link DonThuocChanDoan} từ {@code draft} (entity in-memory) sang
     * {@code managed} (entity vừa load từ DB).
     *
     * <p>Quy tắc merge:</p>
     * <ul>
     *     <li>Với mỗi {@code cdDraft} trong {@code draft.getChanDoans()}:
     *         <ul>
     *             <li>Nếu {@code cdDraft.id == null} (dòng mới, gồm cả dòng ICD tạm
     *                 map từ phiếu ĐT) → add thẳng vào {@code managed.getChanDoans()}.
     *                 JPA sẽ INSERT.</li>
     *             <li>Nếu {@code cdDraft.id != null} (dòng đã tồn tại, user sửa) →
     *                 tìm dòng tương ứng trong {@code managed.getChanDoans()} theo id,
     *                 copy các trường có thể sửa (maIcdSnapshot, tenIcdSnapshot,
     *                 icd10, ketLuan, stt). Không xóa dòng managed cũ.</li>
     *         </ul>
     *     </li>
     *     <li>KHÔNG xóa các dòng có trong {@code managed.getChanDoans()} mà không
     *         có trong draft — tránh orphanRemoval xóa nhầm dòng đã lưu trước đó.
     *         (Nếu user muốn xóa, đã có action remove trên grid → cũng remove
     *         khỏi container, và khi save cascade sẽ xóa theo.)</li>
     * </ul>
     *
     * <p>Lý do KHÔNG thay thế {@code managed.getChanDoans()} bằng {@code draft.getChanDoans()}:
     * các dòng trong draft có thể ở trạng thái detached (đã setDonThuoc) và merge
     * cả collection dễ gây EntityExistsException hoặc OptimisticLockException.</p>
     */
    private void mergeChanDoansFromDraft(DonThuoc managed, DonThuoc draft) {
        if (managed == null || draft == null) {
            return;
        }
        // Map id → managed item để tra cứu nhanh
        java.util.Map<java.util.UUID, DonThuocChanDoan> managedById = new java.util.HashMap<>();
        for (DonThuocChanDoan cd : managed.getChanDoans()) {
            if (cd != null && cd.getId() != null) {
                managedById.put(cd.getId(), cd);
            }
        }
        int inserted = 0;
        int updated = 0;
        for (DonThuocChanDoan cdDraft : draft.getChanDoans()) {
            if (cdDraft == null) {
                continue;
            }
            if (cdDraft.getId() == null) {
                // Dòng mới (gồm cả dòng ICD tạm) → add để JPA INSERT.
                // KHÔNG thay đổi identity của entity để Jmix DataContext vẫn track đúng.
                cdDraft.setDonThuoc(managed);
                managed.getChanDoans().add(cdDraft);
                inserted++;
            } else {
                // Dòng đã tồn tại → update giá trị thay vì insert lại
                DonThuocChanDoan cdManaged = managedById.get(cdDraft.getId());
                if (cdManaged != null) {
                    cdManaged.setStt(cdDraft.getStt());
                    cdManaged.setIcd10(cdDraft.getIcd10());
                    cdManaged.setMaIcdSnapshot(cdDraft.getMaIcdSnapshot());
                    cdManaged.setTenIcdSnapshot(cdDraft.getTenIcdSnapshot());
                    cdManaged.setKetLuan(cdDraft.getKetLuan());
                    updated++;
                }
            }
        }
        if (inserted > 0 || updated > 0) {
            log.info("[MapIcdTam] Merge chanDoans: {} inserted (gồm tạm), {} updated.",
                    inserted, updated);
        }
    }

    /**
     * Merge {@link DonThuocDotDung} đang chỉnh sửa trên form (chỉ 1 dòng / đơn)
     * từ {@code draft} sang {@code managed}. INSERT nếu dòng mới, UPDATE nếu đã tồn tại.
     */
    private void mergeDotDungFromDraft(DonThuoc managed, DonThuoc draft) {
        if (managed == null || draft == null) {
            return;
        }
        DonThuocDotDung ddDraft = dotDungDc.getItem();
        if (ddDraft == null) {
            return;
        }
        // Map id → managed item để tra cứu nhanh
        java.util.Map<java.util.UUID, DonThuocDotDung> managedById = new java.util.HashMap<>();
        for (DonThuocDotDung dd : managed.getDotDungs()) {
            if (dd != null && dd.getId() != null) {
                managedById.put(dd.getId(), dd);
            }
        }
        if (ddDraft.getId() == null) {
            ddDraft.setDonThuoc(managed);
            managed.getDotDungs().add(ddDraft);
            log.info("Merge dotDung: 1 inserted.");
        } else {
            DonThuocDotDung ddManaged = managedById.get(ddDraft.getId());
            if (ddManaged != null) {
                ddManaged.setSoDot(ddDraft.getSoDot());
                ddManaged.setTuNgay(ddDraft.getTuNgay());
                ddManaged.setDenNgay(ddDraft.getDenNgay());
                ddManaged.setSoThangThuoc(ddDraft.getSoThangThuoc());
                log.info("Merge dotDung: 1 updated.");
            }
        }
    }

    /**
     * Merge các {@link DonThuocChiTiet} từ {@code draft} (entity in-memory) sang
     * {@code managed} (entity vừa load từ DB).
     *
     * <p>Quy tắc merge:</p>
     * <ul>
     *     <li>Với mỗi {@code ctDraft} trong {@code chiTietsDc.getItems()}:
     *         <ul>
     *             <li>Nếu {@code ctDraft.id == null} (dòng mới, user thêm thuốc) →
     *                 add thẳng vào {@code managed.getChiTiets()}.
     *                 JPA sẽ INSERT.</li>
     *             <li>Nếu {@code ctDraft.id != null} (dòng đã tồn tại) →
     *                 tìm dòng tương ứng trong {@code managed.getChiTiets()} theo id,
     *                 copy các trường có thể sửa. Không xóa dòng managed cũ.</li>
     *         </ul>
     *     </li>
     *     <li>KHÔNG xóa các dòng có trong {@code managed.getChiTiets()} mà không
     *         có trong container — tránh orphanRemoval xóa nhầm dòng đã lưu trước đó.
     *         (Nếu user muốn xóa, đã có action remove trên grid → cũng remove
     *         khỏi container.)</li>
     * </ul>
     */
    private void mergeChiTietsFromDraft(DonThuoc managed, DonThuoc draft) {
        if (managed == null) {
            return;
        }
        // Map id → managed item để tra cứu nhanh
        java.util.Map<java.util.UUID, DonThuocChiTiet> managedById = new java.util.HashMap<>();
        for (DonThuocChiTiet ct : managed.getChiTiets()) {
            if (ct != null && ct.getId() != null) {
                managedById.put(ct.getId(), ct);
            }
        }
        int inserted = 0;
        int updated = 0;
        for (DonThuocChiTiet ctDraft : chiTietsDc.getItems()) {
            if (ctDraft == null) {
                continue;
            }
            if (ctDraft.getId() == null) {
                // Dòng mới → add để JPA INSERT.
                ctDraft.setDonThuoc(managed);
                managed.getChiTiets().add(ctDraft);
                inserted++;
            } else {
                // Dòng đã tồn tại → update giá trị thay vì insert lại
                DonThuocChiTiet ctManaged = managedById.get(ctDraft.getId());
                if (ctManaged != null) {
                    ctManaged.setStt(ctDraft.getStt());
                    ctManaged.setDmThuoc(ctDraft.getDmThuoc());
                    ctManaged.setMaThuocSnapshot(ctDraft.getMaThuocSnapshot());
                    ctManaged.setTenThuocSnapshot(ctDraft.getTenThuocSnapshot());
                    ctManaged.setBietDuocSnapshot(ctDraft.getBietDuocSnapshot());
                    ctManaged.setDonViTinhSnapshot(ctDraft.getDonViTinhSnapshot());
                    ctManaged.setHamLuongSnapshot(ctDraft.getHamLuongSnapshot());
                    ctManaged.setDangBaoCheSnapshot(ctDraft.getDangBaoCheSnapshot());
                    ctManaged.setSoDangKySnapshot(ctDraft.getSoDangKySnapshot());
                    ctManaged.setSoLuong(ctDraft.getSoLuong());
                    ctManaged.setLieuDung(ctDraft.getLieuDung());
                    ctManaged.setTanSuat(ctDraft.getTanSuat());
                    ctManaged.setThoiGianDung(ctDraft.getThoiGianDung());
                    ctManaged.setCachDung(ctDraft.getCachDung());
                    ctManaged.setDuongDung(ctDraft.getDuongDung());
                    ctManaged.setGhiChu(ctDraft.getGhiChu());
                    updated++;
                }
            }
        }
        if (inserted > 0 || updated > 0) {
            log.info("[MergeChiTiets] {} inserted, {} updated.", inserted, updated);
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
            // Gắn các dòng chẩn đoán (kể cả dòng ICD tạm) vào DonThuoc trước khi save.
            attachChanDoansToEditedEntity();
            // Đồng thời gắn đợt dùng thuốc đang chỉnh sửa trên form 3 trường.
            attachDotDungToEditedEntity();
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
