package com.company.clinicportal.view.chitietdieutri;

import com.company.clinicportal.entity.*;
import com.company.clinicportal.enumentity.NhomDichVu;
import com.company.clinicportal.enumentity.TinhTheoGia;
import com.company.clinicportal.enumentity.TrangThaiBuoiDieuTri;
import com.company.clinicportal.enumentity.TrangThaiDonThuoc;
import com.company.clinicportal.lienthong.CoSoKhamChuaBenhLienThongService;
import com.company.clinicportal.lienthong.LienThongGuiDonThuocService;
import com.company.clinicportal.lienthong.LienThongPasswordPrompt;
import com.company.clinicportal.lienthong.SecretCipher;
import com.company.clinicportal.lienthong.entity.CoSoKhamChuaBenhLienThong;
import com.company.clinicportal.service.*;
import com.company.clinicportal.view.benhnhan.SoBenhAnPreviewDialogView;
import com.company.clinicportal.view.buoidieutri.BuoiDieuTriListView;
import com.company.clinicportal.view.chitietdichvu.ChiTietDichVuDetailView;
import com.company.clinicportal.view.donthuoc.DonThuocDetailView;
import com.company.clinicportal.view.donthuoc.DonThuocQuickAddDialog;
import com.company.clinicportal.view.lichsuthanhtoan.LichSuThanhToanDetailView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "chi-tiet-dieu-tri-sbas/:id", layout = MainView.class)
@ViewController(id = "ChiTietDieuTriSBA.detail")
@ViewDescriptor(path = "chi-tiet-dieu-tri-sba-detail-view.xml")
@EditedEntityContainer("chiTietDieuTriDc")
@DialogMode(height = "90%", width = "90%")
public class ChiTietDieuTriSBADetailView extends StandardDetailView<ChiTietDieuTri> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy");
    private static final Logger log = LoggerFactory.getLogger(ChiTietDieuTriSBADetailView.class);
    private boolean toDieuTriColumnsConfigured = false;

    private BenhNhan idBenhNhan = null;
//    @ViewComponent
//    private DataGrid<ChiTietDichVu> chiTietDichVusDataGrid;
//    @ViewComponent("chiTietDichVusDataGrid.remove")
//    private Action chiTietDichVusRemoveAction;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private Notifications notifications;
    @Autowired
    private ToDieuTriPrintService toDieuTriPrintService;
    @Autowired
    private TheoDoiBNPrintService theoDoiBNPrintService;
    @ViewComponent
    private DataGrid<LichSuThanhToan> lichSuThanhToansDataGrid;
    @ViewComponent
    private CollectionLoader<LichSuThanhToan> lichSuThanhToansDl;
    @ViewComponent
    private TypedTextField<?> ngayBatDauField;
    @ViewComponent
    private TypedTextField<?> ngayKetThucField;
    @ViewComponent
    private JmixTextArea dienBienBenhField;
    @Autowired
    private Metadata metadata;
    @Autowired
    private DataManager dataManager;
    @ViewComponent
    private CollectionPropertyContainer<ChiTietDichVu> chiTietDichVuDc;
    @ViewComponent
    private DataGrid<ToDieuTri> toDieuTrisDataGrid;
    @ViewComponent
    private CollectionLoader<ToDieuTri> toDieuTrisDl;
    @ViewComponent
    private CollectionContainer<ToDieuTri> toDieuTrisDc;
    @ViewComponent
    private CollectionContainer<LichSuThanhToan> lichSuThanhToansDc;
    @ViewComponent
    private JmixTabSheet tabSheet;
//    @Autowired
//    private TinhKpiChiTietService tinhKpiChiTietService;
    @Autowired
    private ChiTietDieuTriPaymentSummaryService paymentSummaryService;
    @Autowired
    private DonThuocService donThuocService;
    @Autowired
    private LienThongGuiDonThuocService lienThongService;
    @Autowired
    private CoSoKhamChuaBenhLienThongService coSoService;
    @Autowired
    private SecretCipher secretCipher;
    @Autowired
    private LienThongPasswordPrompt lienThongPasswordPrompt;

    /** Tên bác sĩ mặc định khi tạo đơn thuốc nhanh từ chi tiết phiếu điều trị. */
    private static final String DEFAULT_TEN_BAC_SI = "BS. Đặng Thị Hà";

    @ViewComponent
    private DataGrid<DonThuoc> donThuocsDataGrid;
    @ViewComponent
    private CollectionLoader<DonThuoc> donThuocsDl;
    @ViewComponent
    private CollectionContainer<DonThuoc> donThuocsDc;
    @ViewComponent
    private JmixTextArea huongDieuTriField;
    @ViewComponent
    private JmixMultiSelectComboBox<Icd10> dsChanDoanIcdRaVienField;
    @ViewComponent
    private JmixTextArea chuanDoanRaVienField;

    /**
     * Renderer hiển thị tiếng Việt cho cột "Trạng thái" đơn thuốc: chuyển {@code trangThai}
     * (String ID như {@code PHAT_HANH}, {@code CHO_GUI}) sang {@link TrangThaiDonThuoc}
     * rồi lấy {@code tenHienThi}. Cột XML dùng {@code key="trangThaiLabel"} không bind
     * property — giá trị do renderer cung cấp.
     */
    @Supply(to = "donThuocsDataGrid.trangThaiLabel", subject = "renderer")
    private Renderer<DonThuoc> donThuocsDataGridTrangThaiLabelRenderer() {
        return new TextRenderer<>(dt -> {
            TrangThaiDonThuoc e = TrangThaiDonThuoc.fromId(dt.getTrangThai());
            return e == null ? dt.getTrangThai() : e.getTenHienThi();
        });
    }

    /**
     * Renderer cho cột "Chẩn đoán" của đơn thuốc: ưu tiên danh sách ICD-10 trên
     * {@code donThuoc.chiTietDieuTri.dsChanDoanIcd} (helper {@code getDsChanDoanIcdText()}
     * trả về chuỗi "Mã - Tên" đã ghép), nếu rỗng mới fallback sang
     * {@code chiTietDieuTri.chuanDoan} (text chuẩn đoán thường).
     */
    @Supply(to = "donThuocsDataGrid.chanDoanColumn", subject = "renderer")
    private Renderer<DonThuoc> donThuocsDataGridChanDoanColumnRenderer() {
        return new TextRenderer<>(dt -> {
            ChiTietDieuTri ctdt = dt.getChiTietDieuTri();
            if (ctdt == null) {
                return "";
            }
            String icdText = ctdt.getDsChanDoanIcdText();
            if (icdText != null && !icdText.isBlank()) {
                return icdText;
            }
            return ctdt.getChuanDoan() != null ? ctdt.getChuanDoan() : "";
        });
    }
    @ViewComponent
    private JmixButton addDonThuocButton;
    @ViewComponent
    private JmixButton dongBoDonThuocButton;
    @ViewComponent
    private JmixTextArea chuanDoanField;
    @ViewComponent
    private JmixMultiSelectComboBox<Icd10> dsChanDoanIcdField;
    @ViewComponent
    private TextField machField;
    @ViewComponent
    private TextField nhietDoField;
    @ViewComponent
    private TextField huyetApField;
    @ViewComponent
    private TextField nhipThoField;
    @ViewComponent
    private TextField canNangField;
    @ViewComponent
    private TextField chieuCaoField;
    @ViewComponent
    private TextField bmiField;

    public void setIdBenhNhan(BenhNhan idBenhNhan) {
        this.idBenhNhan = idBenhNhan;
    }


    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        autoFillTongKetDieuTriFields();
        toggleDiagnosisFields();
        handleTongKetTabSelected();
        updateBmiField();

        huongDieuTriField.setValue("Hướng dẫn PHCN tại nhà");

        if (getEditedEntity().getId() != null) {
            lichSuThanhToansDl.setParameter("idChiTietDieuTri", getEditedEntity());
            lichSuThanhToansDl.load();
        }

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        // ✅ Column Giá
//        Grid.Column<ChiTietDichVu> giaColumn = chiTietDichVusDataGrid.addColumn(chiTiet -> {
//
//                    if (chiTiet.getIdDichVu() == null) return "";
//
//                    Long gia = TinhTheoGia.LE.equals(chiTiet.getTinhTheoGia())
//                            ? chiTiet.getIdDichVu().getGiaBuoiLe()
//                            : chiTiet.getIdDichVu().getGia();
//
//                    return gia != null ? formatter.format(gia) : "0";
//
//                }).setHeader("Giá")
//                .setKey("giaColumn")
//                .setAutoWidth(true)
//                .setSortable(true);
//
//        // ✅ Column Thao tác
//        Grid.Column<ChiTietDichVu> actionColumn = chiTietDichVusDataGrid.addComponentColumn(this::buildChiTietDichVuActionsCell)
//                .setHeader("Thao tác")
//                .setKey("actionColumn")
//                .setAutoWidth(true);
//
//        // ✅ Set đúng thứ tự column
//        chiTietDichVusDataGrid.setColumnOrder(
//                chiTietDichVusDataGrid.getColumnByKey("tenDichVu"),
//                chiTietDichVusDataGrid.getColumnByKey("nhomDichVu"),
//                chiTietDichVusDataGrid.getColumnByKey("ghiChu"),
//                giaColumn,
//                chiTietDichVusDataGrid.getColumnByKey("soLuong"),
//                chiTietDichVusDataGrid.getColumnByKey("ngayBatDau"),
//                actionColumn
//        );

        if (getEditedEntity() != null) {
//            configureToDieuTriGridColumns();
//            toDieuTrisDl.setParameter("idChiTietDieuTri", getEditedEntity());
//            toDieuTrisDl.load();
        }

        // Load danh sách đơn thuốc của chi tiết phiếu điều trị hiện tại và cấu hình cột thao tác.
        if (getEditedEntity() != null && getEditedEntity().getId() != null) {
            donThuocsDl.setParameter("idChiTietDieuTri", getEditedEntity());
            donThuocsDl.load();
        }
        configureDonThuocGridColumns();

//        recalculatePaymentFields();

        // Nếu tab mặc định đang hiển thị là "T� điều trị" (chẳng hạn user mở dialog từ
        // chỗ nào đó đã chọn sẵn tab3) thì load + cấu hình renderer luôn trong onBeforeShow.
        // Nếu không, việc này sẽ được làm lười (lazy) trong onTabSheetSelectedChange khi
        // user thực sự chuyển sang tab này.
//        if (tabSheet != null) {
//            Tab selected = tabSheet.getSelectedTab();
//            if (selected != null
//                    && selected.getId().isPresent()
//                    && "tab3".equals(selected.getId().get())) {
//                if (getEditedEntity() != null
//                        && toDieuTrisDl != null
//                        && toDieuTrisDc.getItems().isEmpty()) {
//                    toDieuTrisDl.setParameter("idChiTietDieuTri", getEditedEntity());
//                    toDieuTrisDl.load();
//                }
//                configureToDieuTriGridColumns();
//            }
//        }

        if (getEditedEntity() != null
                && toDieuTrisDl != null
                && toDieuTrisDc.getItems().isEmpty()) {
            toDieuTrisDl.setParameter("idChiTietDieuTri", getEditedEntity());
            toDieuTrisDl.load();
        }
        configureToDieuTriGridColumns();

        visibleChuanDoan();
    }

    @Subscribe("canNangField")
    public void onCanNangFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<TypedTextField<Double>, Double> event) {
        updateBmiField();
    }

    @Subscribe("chieuCaoField")
    public void onChieuCaoFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<TypedTextField<Double>, Double> event) {
        updateBmiField();
    }

        /**
     * Lazy load + cấu hình renderer cho "Tờ điều trị" chỉ khi user thực sự mở tab "tab3".
     * Trước đó việc này chạy ngay trong {@link #onBeforeShow} khiến 8 cột ComponentRenderer
     * (đặc biệt các cột Span + indexOf O(n)) được set sẵn cho cả view, gây lag giật khi
     * kéo lên/xuống ở các tab khác vì Vaadin Grid vẫn lazy-render row của "Tờ điều trị" nếu
     * user đã từng mở tab này. Bây giờ chỉ chạy khi thực sự cần.
     */
    @Subscribe("tabSheet")
    public void onTabSheetSelectedChange(final JmixTabSheet.SelectedChangeEvent event) {
        Tab selected = event.getSelectedTab();
        if (selected == null || selected.getId().isEmpty()) {
            return;
        }
//        String tabId = selected.getId().get();
//        if ("tab3".equals(tabId)) {
//            handleToDieuTriTabSelected();
//        }
    }

    private void handleToDieuTriTabSelected() {
        if (getEditedEntity() == null) {
            return;
        }
        if (toDieuTrisDl != null && toDieuTrisDc.getItems().isEmpty()) {
            toDieuTrisDl.setParameter("idChiTietDieuTri", getEditedEntity());
            toDieuTrisDl.load();
        }
        configureToDieuTriGridColumns();
    }

    private void handleTongKetTabSelected() {
        if (getEditedEntity() == null) {
            return;
        }
        if (ngayBatDauField == null || ngayKetThucField == null) {
            return;
        }
        ChiTietDieuTri entity = getEditedEntity();
        boolean batDauTrong = entity.getNgayBatDau() == null;
        boolean ketThucTrong = entity.getNgayKetThuc() == null;
        if (!batDauTrong && !ketThucTrong) {
            // Cả ngày bắt đầu/kết thúc đều đã có → bỏ qua luôn phần ngày.
        }
        DateRange range = null;
        if (batDauTrong || ketThucTrong) {
            range = resolveDateRangeFromToDieuTri(entity);
        }
        if (batDauTrong && range != null && range.tuNgay() != null) {
            // ngayBatDauField đã bind với property ngayBatDau của entity,
            // chỉ cần set entity là UI tự cập nhật; gọi setValue(String) thủ công sẽ gây
            // IllegalArgumentException khi model type là Date.
            entity.setNgayBatDau(range.tuNgay());
        }
        if (ketThucTrong && range != null && range.denNgay() != null) {
            entity.setNgayKetThuc(range.denNgay());
        }

        // Auto-fill "Mô tả diễn biến bệnh" từ "Khám bệnh bộ phận" ở tab CHI TIẾT ĐIỀU TRỊ.
        // CHỈ fill khi trống để cho phép người dùng tự sửa ở những lần mở tab sau mà không bị ghi đè.
        if (dienBienBenhField != null
                && (entity.getDienBienBenh() == null || entity.getDienBienBenh().isBlank())) {
            String kbBoPhan = entity.getKbBoPhan();
            if (kbBoPhan != null && !kbBoPhan.isBlank()) {
                entity.setDienBienBenh(kbBoPhan);
                dienBienBenhField.setValue(kbBoPhan);
            }
        }
    }

    private boolean isToDieuTriTabSelected(JmixTabSheet.SelectedChangeEvent event) {
        Tab selected = event.getSelectedTab();
        return selected != null
                && selected.getId().isPresent()
                && "tab3".equals(selected.getId().get());
    }

    private void configureDonThuocGridColumns() {
        if (donThuocsDataGrid == null) {
            return;
        }
        // Đảm bảo chỉ add cột Thao tác một lần (tránh duplicate khi view reload).
        if (donThuocsDataGrid.getColumnByKey("thaoTacDonThuocColumn") == null) {
            Grid.Column<DonThuoc> thaoTacColumn = donThuocsDataGrid.addComponentColumn(this::buildDonThuocActionsCell);
            thaoTacColumn.setKey("thaoTacDonThuocColumn");
            thaoTacColumn.setHeader("Thao tác");
            thaoTacColumn.setWidth("9em");
            thaoTacColumn.setFlexGrow(0);
            thaoTacColumn.setSortable(false);
        }
    }

    private HorizontalLayout buildDonThuocActionsCell(DonThuoc donThuoc) {
        HorizontalLayout layout = uiComponents.create(HorizontalLayout.class);
        layout.setSpacing(false);
        layout.setPadding(false);

        JmixButton openButton = uiComponents.create(JmixButton.class);
        openButton.setIcon(VaadinIcon.EYE.create());
        openButton.addThemeVariants(ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_TERTIARY);
        openButton.setTitle("Mở xem");
        openButton.addClickListener(e -> openDonThuocDetail(donThuoc));

        JmixButton editButton = uiComponents.create(JmixButton.class);
        editButton.setIcon(VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_TERTIARY);
        editButton.setTitle("Sửa");
        editButton.addClickListener(e -> openDonThuocDetail(donThuoc));

        JmixButton delButton = uiComponents.create(JmixButton.class);
        delButton.setIcon(VaadinIcon.TRASH.create());
        delButton.addThemeVariants(ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY);
        delButton.setTitle("Xoá");
        delButton.addClickListener(e -> confirmAndDeleteDonThuoc(donThuoc));

        layout.add(openButton, editButton, delButton);
        return layout;
    }

    /**
     * Cột "Thao tác" cho bảng CHỈ ĐỊNH DỊCH VỤ (text-only, theo pattern của các màn khác):
     * - "Chi tiết" — mở danh sách buổi điều trị của dịch vụ.
     * - "Xóa" — xóa dịch vụ khỏi chỉ định (tận dụng action list_remove của grid).
     */
//    private HorizontalLayout buildChiTietDichVuActionsCell(ChiTietDichVu chiTietDichVu) {
//        HorizontalLayout actions = uiComponents.create(HorizontalLayout.class);
//        actions.setSpacing(true);
//        actions.setPadding(false);
//
//        // Nút Chi tiết
//        JmixButton editButton = uiComponents.create(JmixButton.class);
//        editButton.setText("Chi tiết");
//        editButton.addClickListener(e -> {
//            if (chiTietDichVu != null && chiTietDichVu.getId() != null) {
//                DialogWindow<View<?>> window =
//                        dialogWindows.view(this, "BuoiDieuTri.list")
//                                .build();
//                BuoiDieuTriListView view = (BuoiDieuTriListView) window.getView();
//                view.setIdChiTietDichVu(chiTietDichVu.getId());
//                window.open();
//            }
//        });
//
//        // Nút Xóa
//        JmixButton deleteButton = uiComponents.create(JmixButton.class);
//        deleteButton.setText("Xóa");
//        deleteButton.addClickListener(e -> {
//            if (chiTietDichVu == null || chiTietDichVu.getId() == null) {
//                return;
//            }
//            chiTietDichVusDataGrid.select(chiTietDichVu);
//            chiTietDichVusRemoveAction.actionPerform(chiTietDichVusDataGrid);
//        });
//
//        actions.add(editButton, deleteButton);
//        return actions;
//    }

    /** PostLoad handler để đảm bảo cột thao tác luôn được cấu hình sau khi data load. */
    @Subscribe(id = "donThuocsDl", target = Target.DATA_LOADER)
    public void onDonThuocsDlPostLoad(final CollectionLoader.PostLoadEvent<DonThuoc> event) {
        configureDonThuocGridColumns();
    }

    private void confirmAndDeleteDonThuoc(DonThuoc donThuoc) {
        if (donThuoc == null || donThuoc.getId() == null) return;
        String ma = donThuoc.getMaDonThuoc();
        dialogs.createOptionDialog()
                .withHeader("Xoá đơn thuốc")
                .withText("Bạn có chắc muốn xoá đơn \"" + ma + "\"?")
                .withActions(
                        new DialogAction(DialogAction.Type.NO),
                        new DialogAction(DialogAction.Type.YES).withHandler(e -> {
                            try {
                                dataManager.remove(donThuoc);
                                notifications.create("Đã xoá đơn " + ma)
                                        .withType(Notifications.Type.SUCCESS).show();
                                donThuocsDl.load();
                            } catch (Exception ex) {
                                log.error("Không thể xoá đơn thuốc id={}", donThuoc.getId(), ex);
                                notifications.create("Không thể xoá: " + ex.getMessage())
                                        .withType(Notifications.Type.ERROR).show();
                            }
                        })
                )
                .withWidth("320px")
                .open();
    }

    private void openDonThuocDetail(DonThuoc donThuoc) {
        if (donThuoc == null || donThuoc.getId() == null) {
            return;
        }
        // Mở dialog QuickAdd (header đơn + grid dòng thuốc + Lưu nháp / Phát hành / Huỷ đơn)
        // ở chế độ SỬA đơn thuốc đã có. Controller tự load đơn từ DB theo id.
        DialogWindow<DonThuocQuickAddDialog> window = dialogWindows.view(this, DonThuocQuickAddDialog.class)
                .withViewConfigurer(v -> ((DonThuocQuickAddDialog) v).setDonThuocId(donThuoc.getId()))
                .build();
        window.addAfterCloseListener(e -> donThuocsDl.load());
        window.open();
    }

//    @Subscribe("chiTietDichVusDataGrid.create")
//    public void onChiTietDichVusDataGridCreate(final ActionPerformedEvent event) {
//        DialogWindow<ChiTietDichVuDetailView> window =
//                dialogWindows.detail(this, ChiTietDichVu.class)
//                        .withViewClass(ChiTietDichVuDetailView.class)
//                        .newEntity()
//                        .build();
//
//        window.addAfterCloseListener(event1 -> {
//            if (event1.closedWith(StandardOutcome.SAVE)) {
//                ChiTietDichVu saved = event1.getView().getEditedEntity();
//                Long soLuong = saved.getSoLuong();
//                Long khoangCachBuoiDieuTri = saved.getKhoangCachBuoiDieuTri();
//                Date ngayBatDau = saved.getNgayBatDau();
//
//                // Validate theo quy tắc nghiệp vụ:
//                //   soLuong > 1  && khoangCachBuoiDieuTri == 0  -> báo lỗi, không lưu gì cả.
//                if (soLuong != null && soLuong > 1
//                        && (khoangCachBuoiDieuTri == null || khoangCachBuoiDieuTri == 0)) {
//                    notifications.create("Khoảng cách buổi điều trị phải lớn hơn 0")
//                            .withType(Notifications.Type.ERROR)
//                            .show();
//                    return;
//                }
//
//                saved.setIdChiTietPhieuDieuTri(getEditedEntity());
//                saved.setCreatedAt(LocalDateTime.now());
//                ChiTietDichVu persisted = dataManager.save(saved);
//
//                // Sinh buổi điều trị:
//                //   - soLuong >= 1, có ngày bắt đầu và bệnh nhân.
//                //   - khoangCachBuoiDieuTri có thể = 0 (áp dụng cho soLuong = 1, mọi buổi đều = ngayBatDau)
//                //     hoặc > 0 (các buổi cách nhau khoangCach ngày).
//                if (soLuong != null && soLuong > 0
//                        && ngayBatDau != null
//                        && persisted.getIdChiTietPhieuDieuTri() != null
//                        && persisted.getIdChiTietPhieuDieuTri().getIdBenhNhan() != null) {
//
//                    SaveContext saveContext = new SaveContext();
//
//                    long stepDays = (khoangCachBuoiDieuTri != null && khoangCachBuoiDieuTri > 0)
//                            ? khoangCachBuoiDieuTri
//                            : 0L;
//
//                    for (int i = 0; i < soLuong; i++) {
//                        BuoiDieuTri buoiDieuTri = dataManager.create(BuoiDieuTri.class);
//                        buoiDieuTri.setIdChiTietDichVu(persisted);
//                        buoiDieuTri.setIdChiTietDieuTri(getEditedEntity());
//                        buoiDieuTri.setIdBenhNhan(persisted.getIdChiTietPhieuDieuTri().getIdBenhNhan());
//
//                        Calendar ngayThucHienCal = Calendar.getInstance();
//                        ngayThucHienCal.setTime(ngayBatDau);
//                        ngayThucHienCal.add(Calendar.DAY_OF_MONTH, (int) (stepDays * i));
//                        buoiDieuTri.setNgayThucHien(ngayThucHienCal.getTime());
//                        buoiDieuTri.setTrangThai(TrangThaiBuoiDieuTri.CHUA_THUC_HIEN);
//
//                        saveContext.saving(buoiDieuTri);
//                    }
//
//                    if (!saveContext.getEntitiesToSave().isEmpty()) {
//                        dataManager.save(saveContext);
//                    }
//                }
//
//                chiTietDichVuDc.getMutableItems().add(loadChiTietDichVuForGrid(persisted.getId()));
////                recalculatePaymentFields();
//            }
//        });
//        window.open();
//    }

    private ChiTietDichVu loadChiTietDichVuForGrid(Long chiTietDichVuId) {
        return dataManager.load(ChiTietDichVu.class)
                .id(chiTietDichVuId)
                .fetchPlan(fp -> fp
                        .addFetchPlan("_base")
                        .add("idDichVu", idDichVu -> idDichVu.addFetchPlan("_base")))
                .one();
    }

    @Subscribe("toDieuTrisDataGrid.create")
    public void onToDieuTrisDataGridCreate(final ActionPerformedEvent event) {
        openToDieuTriDetail(null, true);
    }

    /**
     * Bấm "Thêm mới đơn thuốc" trên tab ĐƠN THUỐC → tạo DonThuoc nháp với 4 trường auto-fill
     * (Chuẩn đoán từ ChiTietDieuTri.chuanDoan, Hình thức ĐT = NGOAI_TRU, Ngày kê = hôm nay,
     * Bác sĩ kê đơn = BS. Đặng Thị Hà), rồi mở màn chi tiết để nhập dòng thuốc.
     */
    @Subscribe("addDonThuocButton")
    public void onAddDonThuocButtonClick(final ClickEvent<JmixButton> event) {
        ChiTietDieuTri chiTietDieuTri = getEditedEntity();
        if (chiTietDieuTri == null) {
            return;
        }
        if (chiTietDieuTri.getIdBenhNhan() == null) {
            notifications.create("Phiếu điều trị chưa có bệnh nhân, không thể tạo đơn thuốc.")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        try {
            // Mở dialog "Thêm mới đơn thuốc" — màn full đủ 9 trường header + 3 datagrid
            // (ICD, danh sách thuốc, đợt dùng). Sau khi đóng luôn reload grid đơn thuốc
            // — không cần mở DonThuocDetailView nữa.
            DialogWindow<?> window = dialogWindows.view(this, "DonThuocQuickAddDialog")
                    .build();
            DonThuocQuickAddDialog view =
                    (DonThuocQuickAddDialog) window.getView();
            view.setChiTietDieuTri(chiTietDieuTri);
            window.addAfterCloseListener(closeEvent -> donThuocsDl.load());
            window.open();
        } catch (Exception ex) {
            log.error("Không thể mở dialog thêm đơn thuốc từ ChiTietDieuTri id={}", chiTietDieuTri.getId(), ex);
            notifications.create("Không thể tạo đơn thuốc: " + ex.getMessage())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    /**
     * Nhấn nút "Đồng bộ" trên tab ĐƠN THUỐC → hiển thị popup xác nhận trước khi
     * gọi API gửi đơn thuốc lên hệ thống quốc gia (Cổng liên thông BYT).
     *
     * <p>Flow:
     * <ol>
     *     <li>Validate trước (nếu không có đơn thì thoát ngay, không cần hỏi).</li>
     *     <li>Hiển thị dialog YES/NO xác nhận gửi lên hệ thống quốc gia.</li>
     *     <li>Nếu YES → gọi {@link #performDongBoDonThuoc()}.</li>
     *     <li>Nếu NO → huỷ, không gọi API.</li>
     * </ol>
     */
    @Subscribe("dongBoDonThuocButton")
    public void onDongBoDonThuocButtonClick(final ClickEvent<JmixButton> event) {
        // Validate trước - không có đơn để gửi thì không cần hiện popup hỏi.
        List<DonThuoc> tatCaDonThuoc = donThuocsDc.getItems().stream().collect(Collectors.toList());
        List<DonThuoc> danhSach = tatCaDonThuoc.stream()
                .filter(dt -> {
                    TrangThaiDonThuoc trangThai = dt.getTrangThaiEnum();
                    return trangThai == TrangThaiDonThuoc.CHO_GUI
                        || trangThai == TrangThaiDonThuoc.GUI_LOI;
                })
                .collect(Collectors.toList());

        if (tatCaDonThuoc.isEmpty()) {
            notifications.create("Không có đơn thuốc nào để đồng bộ.")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        if (danhSach.isEmpty()) {
            notifications.create("Không có đơn thuốc nào ở trạng thái chờ gửi hoặc gửi lỗi.\n"
                    + "(Các đơn ở trạng thái khác sẽ không được gửi).")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        // Hiển thị popup xác nhận gửi lên hệ thống quốc gia.
        // (Tránh gửi nhầm vì API BYT thật sẽ tạo mã đơn quốc gia, có thể ảnh hưởng đến bệnh nhân.)
        dialogs.createOptionDialog()
                .withHeader("Xác nhận gửi đơn thuốc lên hệ thống quốc gia")
                .withText(buildDongBoConfirmMessage(danhSach))
                .withActions(
                        new DialogAction(DialogAction.Type.NO),
                        new DialogAction(DialogAction.Type.YES)
                                .withHandler(e -> performDongBoDonThuoc()))
                .withWidth("480px")
                .open();
    }

    /**
     * Build message xác nhận gửi đơn - liệt kê các mã đơn sẽ được gửi để user review.
     */
    private String buildDongBoConfirmMessage(List<DonThuoc> danhSach) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn có chắc muốn gửi ")
          .append(danhSach.size())
          .append(" đơn thuốc sau lên hệ thống quốc gia (Cổng liên thông BYT)?\n\n");
        int max = Math.min(10, danhSach.size());
        for (int i = 0; i < max; i++) {
            DonThuoc dt = danhSach.get(i);
            sb.append("• ").append(dt.getMaDonThuoc());
            if (dt.getHoVaTenBenhNhan() != null) {
                sb.append(" - ").append(dt.getHoVaTenBenhNhan());
            }
            sb.append("\n");
        }
        if (danhSach.size() > max) {
            sb.append("... và ").append(danhSach.size() - max).append(" đơn khác.\n");
        }
        sb.append("\nLưu ý: Thao tác này sẽ tạo mã đơn thuốc quốc gia và không thể huỷ.");
        return sb.toString();
    }

    /**
     * Thực hiện gửi API đồng bộ đơn thuốc lên hệ thống quốc gia. Được gọi sau
     * khi user xác nhận YES trên popup.
     *
     * <p>Flow:
     * <ol>
     *     <li>Lấy cấu hình cơ sở KCB liên thông.</li>
     *     <li>Gọi API trực tiếp qua {@link LienThongGuiDonThuocService}.</li>
     *     <li>Cập nhật trạng thái đơn và hiển thị kết quả.</li>
     * </ol>
     */
    private void performDongBoDonThuoc() {
        List<DonThuoc> tatCaDonThuoc = donThuocsDc.getItems().stream().collect(Collectors.toList());
        List<DonThuoc> danhSach = tatCaDonThuoc.stream()
                .filter(dt -> {
                    TrangThaiDonThuoc trangThai = dt.getTrangThaiEnum();
                    return trangThai == TrangThaiDonThuoc.CHO_GUI
                        || trangThai == TrangThaiDonThuoc.GUI_LOI;
                })
                .collect(Collectors.toList());

        // Lấy cơ sở KCB liên thông đầu tiên (active)
        List<CoSoKhamChuaBenhLienThong> coSoList =
                dataManager.load(CoSoKhamChuaBenhLienThong.class)
                        .query("select e from ltcs_CoSoKhamChuaBenhLienThong e where e.active = true")
                        .list();
        if (coSoList.isEmpty()) {
            notifications.create("Chưa có cấu hình cơ sở KCB liên thông. Vui lòng kiểm tra.")
                    .withType(Notifications.Type.ERROR)
                    .show();
            return;
        }
        CoSoKhamChuaBenhLienThong coSo = coSoList.get(0);
        String password = coSoService.decryptPasswordOrNull(coSo);
        if (password == null || password.isBlank()) {
            notifications.create("Cơ sở KCB chưa có password. Vui lòng cấu hình.")
                    .withType(Notifications.Type.ERROR)
                    .show();
            return;
        }

        // Lấy thông tin đăng nhập bác sĩ từ application.properties.
        // Theo FSD §VI: API gửi đơn cần token từ /api/auth/dang-nhap-bac-si.
        LienThongPasswordPrompt.BacSiCredentials creds =
                lienThongPasswordPrompt.resolveBacSiCredentials(notifications);
        if (creds == null) {
            // user huỷ dialog hoặc thiếu config
            return;
        }

        dongBoDonThuocButton.setEnabled(false);
        notifications.create("Đang đồng bộ " + danhSach.size() + " đơn thuốc...")
                .withPosition(Notification.Position.TOP_END)
                .withDuration(2000)
                .show();

        int success = 0, fail = 0;
        // Chỉ theo dõi các đơn THÀNH CÔNG để liệt kê trong message thông báo.
        List<String> successCodes = new ArrayList<>();
        // Lỗi chỉ đếm + log; không đẩy raw JSON/Unicode vào notification (gây rối UI).
        List<String> failMuteMessages = new ArrayList<>();

        for (DonThuoc dt : danhSach) {
            // Load lại entity từ DB với fetch plan đầy đủ, tránh lazy fetch trên detached object
            UUID donThuocId = dt.getId();
            if (donThuocId == null) {
                fail++;
                failMuteMessages.add(dt.getMaDonThuoc() + ": chưa lưu");
                log.warn("[DongBo] Bỏ qua đơn chưa được lưu.");
                continue;
            }
            List<String> validationErrors = donThuocService.validateForIssue(donThuocId);
            if (!validationErrors.isEmpty()) {
                fail++;
                failMuteMessages.add(dt.getMaDonThuoc() + ": validation");
                log.warn("[DongBo] Đơn {} không hợp lệ: {}", dt.getMaDonThuoc(), validationErrors);
                continue;
            }
            // Load lại DonThuoc managed từ DB để gọi service gửi liên thông (cần lazy access bên trong)
            DonThuoc managedDt = dataManager.load(DonThuoc.class)
                    .id(donThuocId)
                    .fetchPlan(fp -> fp
                            .addFetchPlan("_base")
                            .add("chiTiets", b -> b.addFetchPlan("_base"))
                            .add("chanDoans", b -> b.addFetchPlan("_base"))
                            .add("dotDungs", b -> b.addFetchPlan("_base")))
                    .one();

            try {
                String idempotencyKey = managedDt.getIdempotencyKey() != null ? managedDt.getIdempotencyKey()
                        : UUID.randomUUID().toString();
                // maLienThongBacSi + password lấy từ application.properties
                // maLienThongCoSo + passwordCoSo lấy từ DB CoSoKhamChuaBenhLienThong
                LienThongGuiDonThuocService.GuiDonThuocResult result =
                        lienThongService.send(managedDt, idempotencyKey,
                                coSo.getMaLienThong(), password,
                                creds.maLienThongBacSi, creds.password);

                if (result.success) {
                    success++;
                    successCodes.add(dt.getMaDonThuoc());
                    log.info("[DongBo] Gửi thành công maDonThuoc={} httpStatus={}",
                            dt.getMaDonThuoc(), result.httpStatus);
                } else {
                    fail++;
                    failMuteMessages.add(dt.getMaDonThuoc() + ": HTTP " + result.httpStatus);
                    log.warn("[DongBo] Gửi thất bại maDonThuoc={} httpStatus={} msg={}",
                            dt.getMaDonThuoc(), result.httpStatus,
                            result.response != null ? result.response.message : "");
                }
            } catch (Exception ex) {
                fail++;
                failMuteMessages.add(dt.getMaDonThuoc() + ": exception");
                log.error("[DongBo] Lỗi khi gửi maDonThuoc=" + dt.getMaDonThuoc(), ex);
            }
        }

        dongBoDonThuocButton.setEnabled(true);
        donThuocsDl.load();

        // Liệt kê các mã đơn THÀNH CÔNG (định dạng gọn, không raw JSON).
        String successListStr = successCodes.isEmpty() ? "" :
                "\nThành công: " + String.join(", ", successCodes);

        if (success > 0 && fail == 0) {
            notifications.create("Đồng bộ thành công " + success + " đơn thuốc." + successListStr)
                    .withType(Notifications.Type.SUCCESS)
                    .withDuration(6000)
                    .show();
        } else if (success > 0 && fail > 0) {
            notifications.create("Đồng bộ: " + success + " thành công, " + fail + " thất bại. Xem log để chi tiết." + successListStr)
                    .withType(Notifications.Type.WARNING)
                    .withDuration(6000)
                    .show();
        } else {
            notifications.create("Đồng bộ thất bại " + fail + " đơn. Xem log để chi tiết.")
                    .withType(Notifications.Type.ERROR)
                    .withDuration(4000)
                    .show();
        }
    }

    @Subscribe("printToDieuTriButton")
    public void onPrintToDieuTriButtonClick(final ClickEvent<JmixButton> event) {
        if (getEditedEntity().getId() == null) {
            notifications.create("Vui lòng lưu phiếu điều trị trước khi in.")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        try {
            notifications.create("Đang tạo bản xem trước...")
                    .withPosition(Notification.Position.TOP_END)
                    .withDuration(3000)
                    .show();

            byte[] pdfBytes = toDieuTriPrintService.generatePdf(getEditedEntity());
            String fileName = "to-dieu-tri-" + getEditedEntity().getId() + ".pdf";

            DialogWindow<SoBenhAnPreviewDialogView> window = dialogWindows
                    .view(this, SoBenhAnPreviewDialogView.class)
                    .build();
            window.getView().setPreviewData(pdfBytes, pdfBytes, fileName);
            window.getView().setPreviewTitle("Xem trước tờ điều trị");
            window.getView().setPreviewFileName(fileName);
            window.setWidth("90%");
            window.setHeight("90%");
            window.open();
        } catch (Exception ex) {
            notifications.create("Không thể tạo bản xem trước tờ điều trị. Vui lòng thử lại.")
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    @Subscribe("printTheoDoiBNButton")
    public void onPrintTheoDoiBNButtonClick(final ClickEvent<JmixButton> event) {
        if (getEditedEntity().getId() == null) {
            notifications.create("Vui lòng lưu phiếu điều trị trước khi in.")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        try {
            notifications.create("Đang tạo bảng theo dõi bệnh nhân...")
                    .withPosition(Notification.Position.TOP_END)
                    .withDuration(3000)
                    .show();

            byte[] pdfBytes = theoDoiBNPrintService.generatePdf(getEditedEntity());
            String fileName = "theo-doi-bn-" + getEditedEntity().getId() + ".pdf";

            DialogWindow<SoBenhAnPreviewDialogView> window = dialogWindows
                    .view(this, SoBenhAnPreviewDialogView.class)
                    .build();
            window.getView().setPreviewData(pdfBytes, pdfBytes, fileName);
            window.getView().setPreviewTitle("Xem trước bảng theo dõi bệnh nhân");
            window.getView().setPreviewFileName(fileName);
            window.setWidth("90%");
            window.setHeight("90%");
            window.open();
        } catch (Exception ex) {
            notifications.create("Không thể tạo bảng theo dõi bệnh nhân. Vui lòng thử lại.")
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    private void configureToDieuTriGridColumns() {
        if (toDieuTriColumnsConfigured) {
            return;
        }
        setColumnRenderer("sttColumn", toDieuTri -> {
            Span span = uiComponents.create(Span.class);
            int stt = toDieuTrisDc.getItems().indexOf(toDieuTri) + 1;
            span.setText(stt > 0 ? String.valueOf(stt) : "");
            return span;
        });
        setColumnRenderer("ngayGioColumn", toDieuTri -> {
            Span span = uiComponents.create(Span.class);
            span.getStyle().set("white-space", "pre-line");
            span.setText(formatKhoangNgay(toDieuTri));
            return span;
        });
        setColumnRenderer("moTaColumn", toDieuTri -> {
            Span span = uiComponents.create(Span.class);
            span.getStyle().set("white-space", "pre-line");
            span.setText(safeText(toDieuTri.getMoTaDienBienBenh()));
            return span;
        });
        setColumnRenderer("tenDichVuColumn", toDieuTri -> {
            Span span = uiComponents.create(Span.class);
            span.getStyle()
                    .set("white-space", "pre-line")
                    .set("text-align", "left")
                    .set("display", "block")
                    .set("width", "100%")
                    .set("line-height", "1.4");
            span.setText(formatKyThuatText(toDieuTri, true));
            return span;
        });
        setColumnRenderer("thoiGianColumn", toDieuTri -> {
            Span span = uiComponents.create(Span.class);
            span.getStyle()
                    .set("white-space", "pre-line")
                    .set("text-align", "left")
                    .set("display", "block")
                    .set("width", "100%")
                    .set("line-height", "1.4");
            span.setText(formatKyThuatText(toDieuTri, false));
            return span;
        });
        setColumnRenderer("nguoiThucHienColumn", toDieuTri -> {
            Span span = uiComponents.create(Span.class);
            String hoTen = toDieuTri.getIdNguoiThucHien() != null
                    ? safeText(toDieuTri.getIdNguoiThucHien().getHoTen()) : "";
            span.setText(hoTen);
            return span;
        });
        setColumnRenderer("bacSiColumn", toDieuTri -> {
            Span span = uiComponents.create(Span.class);
            String hoTen = toDieuTri.getIdBacSiChiDinh() != null
                    ? safeText(toDieuTri.getIdBacSiChiDinh().getHoTen()) : "";
            span.setText(hoTen);
            return span;
        });
        Grid.Column<ToDieuTri> thaoTacColumn = toDieuTrisDataGrid.getColumnByKey("thaoTacColumn");
        if (thaoTacColumn != null) {
            thaoTacColumn.setRenderer(new ComponentRenderer<>(toDieuTri -> {
                HorizontalLayout layout = uiComponents.create(HorizontalLayout.class);
                layout.setSpacing(true);
                layout.add(
                        createIconActionButton(VaadinIcon.EDIT, "Sửa", () -> openToDieuTriDetail(toDieuTri, false)),
                        createIconActionButton(VaadinIcon.COPY, "Sao chép", () -> copyToDieuTri(toDieuTri)),
                        createIconActionButton(VaadinIcon.TRASH, "Xóa", () -> deleteToDieuTri(toDieuTri))
                );
                return layout;
            }));
        }
        toDieuTriColumnsConfigured = true;
    }

    private void setColumnRenderer(String key, Function<ToDieuTri, Span> supplier) {
        Grid.Column<ToDieuTri> column = toDieuTrisDataGrid.getColumnByKey(key);
        if (column != null) {
            column.setRenderer(new ComponentRenderer<>(supplier::apply));
        }
    }

    private JmixButton createIconActionButton(VaadinIcon icon, String tooltip, Runnable action) {
        JmixButton button = uiComponents.create(JmixButton.class);
        Icon vaadinIcon = icon.create();
        if (icon == VaadinIcon.TRASH) {
            vaadinIcon.setColor("var(--lumo-error-text-color)");
        }
        button.setIcon(vaadinIcon);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        button.getElement().setProperty("title", tooltip);
        button.addClickListener(e -> action.run());
        return button;
    }

    private void openToDieuTriDetail(ToDieuTri toDieuTri, boolean create) {
        DialogWindow<ToDieuTriDetailView> window;
        if (create) {
            window = dialogWindows.detail(this, ToDieuTri.class)
                    .withViewClass(ToDieuTriDetailView.class)
                    .newEntity()
                    .build();
            window.getView().getEditedEntity().setChiTietDieuTri(getEditedEntity());
        } else {
            window = dialogWindows.detail(this, ToDieuTri.class)
                    .withViewClass(ToDieuTriDetailView.class)
                    .editEntity(toDieuTri)
                    .build();
        }
        window.addAfterCloseListener(event -> {
            if (event.closedWith(StandardOutcome.SAVE)) {
                toDieuTrisDl.load();
                autoFillTongKetDieuTriFields();
            }
        });
        window.open();
    }

    private void copyToDieuTri(ToDieuTri source) {
        if (source == null || source.getId() == null) {
            return;
        }
        ToDieuTri loaded = dataManager.load(ToDieuTri.class)
                .id(source.getId())
                .fetchPlan(fp -> fp
                        .addFetchPlan(FetchPlan.BASE)
                        .add("kyThuatList", k -> k
                                .addFetchPlan(FetchPlan.BASE)
                                .add("idDichVu", d -> d.addFetchPlan(FetchPlan.BASE))))
                .one();

        ToDieuTri copy = metadata.create(ToDieuTri.class);
        copy.setChiTietDieuTri(getEditedEntity());
        copy.setTuNgay(loaded.getTuNgay());
        copy.setDenNgay(loaded.getDenNgay());
        copy.setMoTaDienBienBenh(loaded.getMoTaDienBienBenh());
        copy.setIdNguoiThucHien(loaded.getIdNguoiThucHien());
        copy.setIdBacSiChiDinh(loaded.getIdBacSiChiDinh());
        copy.setGhiChu(loaded.getGhiChu());

        List<ToDieuTriKyThuat> kyThuatCopies = new ArrayList<>();
        if (loaded.getKyThuatList() != null) {
            for (ToDieuTriKyThuat kt : loaded.getKyThuatList()) {
                ToDieuTriKyThuat ktCopy = metadata.create(ToDieuTriKyThuat.class);
                ktCopy.setToDieuTri(copy);
                ktCopy.setIdDichVu(kt.getIdDichVu());
                ktCopy.setThoiGianPhut(kt.getThoiGianPhut());
                ktCopy.setGhiChu(kt.getGhiChu());
                kyThuatCopies.add(ktCopy);
            }
        }
        copy.setKyThuatList(kyThuatCopies);
        dataManager.save(copy);
        toDieuTrisDl.load();
        notifications.create("Đã sao chép dòng điều trị.")
                .withType(Notifications.Type.SUCCESS)
                .show();
    }

    private void deleteToDieuTri(ToDieuTri toDieuTri) {
        if (toDieuTri == null || toDieuTri.getId() == null) {
            return;
        }
        dialogs.createOptionDialog()
                .withHeader("Xác nhận xóa")
                .withText("Bạn có chắc muốn xóa dòng điều trị này?")
                .withActions(
                        new DialogAction(DialogAction.Type.NO),
                        new DialogAction(DialogAction.Type.YES).withHandler(e -> {
                            dataManager.remove(toDieuTri);
                            toDieuTrisDl.load();
                        })
                )
                .open();
    }

    private String formatKhoangNgay(ToDieuTri toDieuTri) {
        String tu = formatDate(toDieuTri.getTuNgay());
        String den = formatDate(toDieuTri.getDenNgay());
        if (!tu.isBlank() && !den.isBlank()) {
            return tu + " Đến " + den;
        }
        return tu.isBlank() ? den : tu;
    }

    private String formatKyThuatText(ToDieuTri toDieuTri, boolean tenDichVu) {
        if (toDieuTri.getKyThuatList() == null || toDieuTri.getKyThuatList().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (ToDieuTriKyThuat kt : toDieuTri.getKyThuatList()) {
            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            sb.append(index).append(". ");
            if (tenDichVu) {
                String name = kt.getIdDichVu() != null ? safeText(kt.getIdDichVu().getTenDichVu()) : "";
                sb.append(name);
            } else {
                sb.append(kt.getThoiGianPhut() != null ? kt.getThoiGianPhut() : "");
            }
            index++;
        }
        return sb.toString();
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return DATE_FORMATTER.format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    private String safeText(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

//    @Subscribe
//    public void onValidation(final ValidationEvent event) {
//        String validationError = buildTrongSoValidationError();
//        if (validationError != null) {
//            event.getErrors().add(validationError);
//        }
//    }

//    private void recalculatePaymentFields() {
//        long tongTien = 0L;
//        for (ChiTietDichVu item : chiTietDichVuDc.getItems()) {
//            long gia = item.getIdDichVu() != null && item.getIdDichVu().getGia() != null
//                    ? item.getIdDichVu().getGia()
//                    : 0L;
//            long soBuoi = item.getSoLuong() != null ? item.getSoLuong() : 0L;
//            tongTien += gia * soBuoi;
//        }
//
//        BigDecimal tongTienBd = BigDecimal.valueOf(tongTien);
////        BigDecimal khuyenMaiPercent = parseFlexibleDecimal(khuyenMaiField.getTypedValue())
////                .orElse(BigDecimal.ZERO);
//        long daThanhToanTong = 0L;
//        Date ngayThanhToanCuoi = null;
//        for (LichSuThanhToan lichSuThanhToan : lichSuThanhToansDc.getItems()) {
//            daThanhToanTong += lichSuThanhToan.getDaThanhToan() != null ? lichSuThanhToan.getDaThanhToan() : 0L;
//            if (lichSuThanhToan.getThanhToanLuc() != null
//                    && (ngayThanhToanCuoi == null || lichSuThanhToan.getThanhToanLuc().after(ngayThanhToanCuoi))) {
//                ngayThanhToanCuoi = lichSuThanhToan.getThanhToanLuc();
//            }
//        }
//        BigDecimal daThanhToan = BigDecimal.valueOf(daThanhToanTong);
//
//        BigDecimal tongSauKhuyenMaiBd = tongTienBd.subtract(
//                tongTienBd.multiply(khuyenMaiPercent)
//                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
//        );
//        long tongSauKhuyenMai = tongSauKhuyenMaiBd.setScale(0, RoundingMode.HALF_UP).longValue();
//        int phaiDong = BigDecimal.valueOf(tongSauKhuyenMai)
//                .subtract(daThanhToan)
//                .setScale(0, RoundingMode.HALF_UP)
//                .intValue();
//
//        getEditedEntity().setTongTien(tongTien);
//        getEditedEntity().setTongTienSauKhuyenMai(tongSauKhuyenMai);
//        getEditedEntity().setDaThanhToan(daThanhToanTong);
//        getEditedEntity().setPhaiDong(phaiDong);
//        getEditedEntity().setNgayThanhToan(ngayThanhToanCuoi);
//
////        tongTienField.setTypedValue(tongTien);
////        tongTienSauKhuyenMaiField.setTypedValue(tongSauKhuyenMai);
////        daThanhToanField.setTypedValue(daThanhToanTong);
////        phaiDongField.setTypedValue(phaiDong);
////        ngayThanhToanField.setTypedValue(ngayThanhToanCuoi);
//    }

    private String buildTrongSoValidationError() {
        Map<NhomDichVu, BigDecimal> trongSoTheoNhom = getTrongSoTheoNhom();
        Set<NhomDichVu> nhomDuocChiDinh = getNhomDichVuDuocChiDinh();

        Set<String> nhomKhongHopLe = new LinkedHashSet<>();
        for (Map.Entry<NhomDichVu, BigDecimal> entry : trongSoTheoNhom.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0
                    && !nhomDuocChiDinh.contains(entry.getKey())) {
                nhomKhongHopLe.add(getTenNhomDichVu(entry.getKey()));
            }
        }
        if (!nhomKhongHopLe.isEmpty()) {
            return "Trọng số chỉ được nhập cho nhóm đã có trong phần chỉ định dịch vụ. "
                    + "Nhóm chưa được chỉ định: " + String.join(", ", nhomKhongHopLe) + ".";
        }

        BigDecimal tongTrongSo = BigDecimal.ZERO;
        for (BigDecimal value : trongSoTheoNhom.values()) {
            tongTrongSo = tongTrongSo.add(value);
        }

        BigDecimal hundred = BigDecimal.valueOf(100);
        int compareResult = tongTrongSo.compareTo(hundred);
        if (compareResult > 0) {
            BigDecimal vuot = tongTrongSo.subtract(hundred);
            return "Tổng trọng số đang vượt " + formatPercent(vuot)
                    + "% (hiện tại " + formatPercent(tongTrongSo) + "%). Vui lòng điều chỉnh về 100%.";
        }
        if (compareResult < 0) {
            BigDecimal thieu = hundred.subtract(tongTrongSo);
            return "Tổng trọng số đang thiếu " + formatPercent(thieu)
                    + "% (hiện tại " + formatPercent(tongTrongSo) + "%). Vui lòng điều chỉnh về 100%.";
        }
        return null;
    }

    private Map<NhomDichVu, BigDecimal> getTrongSoTheoNhom() {
        Map<NhomDichVu, BigDecimal> result = new EnumMap<>(NhomDichVu.class);
        result.put(NhomDichVu.VAT_LY_TRI_LIEU, toBigDecimal(getEditedEntity().getTrongSoVatLyTriLieu()));
        result.put(NhomDichVu.VAN_DONG_TRI_LIEU, toBigDecimal(getEditedEntity().getTrongSoVanDongTriLieu()));
        result.put(NhomDichVu.KEO_NAN_TRI_LIEU, toBigDecimal(getEditedEntity().getTrongSoKeoNanTriLieu()));
        result.put(NhomDichVu.XOA_BOP_TRI_LIEU, toBigDecimal(getEditedEntity().getTrongSoXoaBopTriLieu()));
        result.put(NhomDichVu.KHAM_LUONG_GIA, toBigDecimal(getEditedEntity().getTrongSoKhamLuongGia()));
        return result;
    }

    private Set<NhomDichVu> getNhomDichVuDuocChiDinh() {
        Set<NhomDichVu> result = new LinkedHashSet<>();
        for (ChiTietDichVu item : chiTietDichVuDc.getItems()) {
            if (item.getIdDichVu() != null && item.getIdDichVu().getNhomDichVu() != null) {
                result.add(item.getIdDichVu().getNhomDichVu());
            }
        }
        return result;
    }

    private Optional<BigDecimal> parseFlexibleDecimal(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String normalized = value.trim().replace(',', '.');
        try {
            return Optional.of(new BigDecimal(normalized));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : BigDecimal.ZERO;
    }

    private String formatPercent(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private String getTenNhomDichVu(NhomDichVu nhomDichVu) {
        return switch (nhomDichVu) {
            case VAT_LY_TRI_LIEU -> "Vật lý trị liệu";
            case VAN_DONG_TRI_LIEU -> "Vận động trị liệu";
            case KEO_NAN_TRI_LIEU -> "Kéo nắn trị liệu";
            case XOA_BOP_TRI_LIEU -> "Xoa bóp trị liệu";
            case KHAM_LUONG_GIA -> "Khám lượng giá";
        };
    }

    private void visibleChuanDoan(){
        dsChanDoanIcdRaVienField.setVisible(!getEditedEntity().getDsChanDoanIcd().isEmpty());
        chuanDoanRaVienField.setVisible(getEditedEntity().getDsChanDoanIcd().isEmpty());
    }


    private void autoFillTongKetDieuTriFields() {
        ChiTietDieuTri chiTietDieuTri = getEditedEntity();
        if (chiTietDieuTri == null) {
            return;
        }

        // dienBienBenh giờ được auto-fill từ kbBoPhan trong handleTongKetTabSelected()
        // (chỉ khi user mở tab Tổng kết điều trị và field trống) — không fill ở đây nữa.

        if (chiTietDieuTri.getChuanDoanRaVien() == null || chiTietDieuTri.getChuanDoanRaVien().isBlank()) {
            String chuanDoan = chiTietDieuTri.getChuanDoan();
            chiTietDieuTri.setChuanDoanRaVien(chuanDoan != null ? chuanDoan : "");
        }
    }

    /**
     * Ẩn/hiện hai trường "Chẩn đoán ban đầu" và "Chẩn đoán ICD" theo quy tắc:
     * <ul>
     *     <li>Nếu {@code dsChanDoanIcd} có giá trị → hiện dsChanDoanIcdField, ẩn chuanDoanField.</li>
     *     <li>Nếu {@code dsChanDoanIcd} rỗng và {@code chuanDoan} có giá trị → ẩn dsChanDoanIcdField, hiện chuanDoanField.</li>
     *     <li>Nếu cả hai đều rỗng → hiện chuanDoanField, ẩn dsChanDoanIcdField.</li>
     * </ul>
     */
    private void toggleDiagnosisFields() {
        if (chuanDoanField == null || dsChanDoanIcdField == null) {
            return;
        }
        ChiTietDieuTri chiTietDieuTri = getEditedEntity();
        boolean hasIcd = chiTietDieuTri != null
                && chiTietDieuTri.getDsChanDoanIcd() != null
                && !chiTietDieuTri.getDsChanDoanIcd().isEmpty();
        boolean hasChuanDoan = chiTietDieuTri != null
                && chiTietDieuTri.getChuanDoan() != null
                && !chiTietDieuTri.getChuanDoan().isBlank();

        if (hasIcd) {
            chuanDoanField.setVisible(false);
            dsChanDoanIcdField.setVisible(true);
        } else if (hasChuanDoan) {
            chuanDoanField.setVisible(true);
            dsChanDoanIcdField.setVisible(false);
        } else {
            chuanDoanField.setVisible(true);
            dsChanDoanIcdField.setVisible(false);
        }
    }

    private DateRange resolveDateRangeFromToDieuTri(ChiTietDieuTri chiTietDieuTri) {
        if (chiTietDieuTri.getId() == null) {
            return null;
        }
        List<ToDieuTri> lines = dataManager.load(ToDieuTri.class)
                .query("select e from ToDieuTri e where e.chiTietDieuTri = :ctdt order by e.tuNgay asc, e.id asc")
                .parameter("ctdt", chiTietDieuTri)
                .list();
        if (lines.isEmpty()) {
            return null;
        }
        Date start = null;
        Date end = null;
        for (ToDieuTri line : lines) {
            if (line.getTuNgay() != null && (start == null || line.getTuNgay().before(start))) {
                start = line.getTuNgay();
            }
            if (line.getDenNgay() != null && (end == null || line.getDenNgay().after(end))) {
                end = line.getDenNgay();
            }
        }
        if (start == null && end == null) {
            return null;
        }
        return new DateRange(start, end);
    }

    /** Khoảng ngày bắt đầu / kết thúc trả về từ {@link #resolveDateRangeFromToDieuTri}. */
    private record DateRange(Date tuNgay, Date denNgay) { }

    @Install(to = "fileDinhKemDataGrid.create", subject = "newEntitySupplier")
    private ChiTietDieuTriFileDinhKem fileDinhKemDataGridCreateNewEntitySupplier() {
        ChiTietDieuTriFileDinhKem fileDinhKem = metadata.create(ChiTietDieuTriFileDinhKem.class);
        fileDinhKem.setChiTietDieuTri(getEditedEntity());
        return fileDinhKem;
    }

    @Subscribe("canNangField")
    public void onCanNangFieldValueChange(final HasValue.ValueChangeEvent<?> event) {
        updateBmiField();
    }

    @Subscribe("chieuCaoField")
    public void onChieuCaoFieldValueChange(final HasValue.ValueChangeEvent<?> event) {
        updateBmiField();
    }

    private void updateBmiField() {
        if (canNangField == null || chieuCaoField == null || bmiField == null) {
            return;
        }
        Double canNang = parseDouble(canNangField.getValue());
        Double chieuCao = parseDouble(chieuCaoField.getValue());
        if (canNang == null || chieuCao == null || chieuCao <= 0) {
            bmiField.setValue("");
            return;
        }
        double chieuCaoMet = chieuCao / 100.0;
        double bmi = canNang / (chieuCaoMet * chieuCaoMet);
        double rounded = Math.round(bmi * 100.0) / 100.0;
        bmiField.setValue(String.valueOf(rounded));
    }

    private static Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
//        recalculatePaymentFields();
        paymentSummaryService.applyTotals(getEditedEntity());
    }

//    @Subscribe
//    public void onAfterSave(final AfterSaveEvent event) {
//        if (getEditedEntity().getId() != null) {
//            tinhKpiChiTietService.regenerateForChiTietDieuTri(getEditedEntity().getId());
//        }
//    }
}
