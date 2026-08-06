package com.company.clinicportal.view.chitietdieutri;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.ChiTietDieuTriFileDinhKem;
import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.entity.LichSuThanhToan;
import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.entity.ToDieuTri;
import com.company.clinicportal.entity.ToDieuTriKyThuat;
import com.company.clinicportal.enumentity.NhomDichVu;
import com.company.clinicportal.enumentity.TinhTheoGia;
import com.company.clinicportal.enumentity.TrangThaiBuoiDieuTri;
import com.company.clinicportal.enumentity.TrangThaiDonThuoc;
import com.company.clinicportal.service.ChiTietDieuTriPaymentSummaryService;
import com.company.clinicportal.service.DonThuocService;
import com.company.clinicportal.service.TinhKpiChiTietService;
import com.company.clinicportal.service.ToDieuTriPrintService;
import com.company.clinicportal.view.benhnhan.SoBenhAnPreviewDialogView;
import com.company.clinicportal.view.buoidieutri.BuoiDieuTriListView;
import com.company.clinicportal.view.chitietdichvu.ChiTietDichVuDetailView;
import com.company.clinicportal.view.donthuoc.DonThuocDetailView;
import com.company.clinicportal.view.donthuoc.DonThuocQuickAddDialog;
import com.company.clinicportal.view.lichsuthanhtoan.LichSuThanhToanDetailView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
import io.jmix.flowui.component.textfield.TypedTextField;
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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "chi-tiet-dieu-tri-sbas/:id", layout = MainView.class)
@ViewController(id = "ChiTietDieuTriSBA.detail")
@ViewDescriptor(path = "chi-tiet-dieu-tri-sba-detail-view.xml")
@EditedEntityContainer("chiTietDieuTriDc")
@DialogMode(height = "100%", width = "80%")
public class ChiTietDieuTriSBADetailView extends StandardDetailView<ChiTietDieuTri> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy");
    private static final Logger log = LoggerFactory.getLogger(ChiTietDieuTriSBADetailView.class);
    private boolean toDieuTriColumnsConfigured = false;

    private BenhNhan idBenhNhan = null;
    @ViewComponent
    private DataGrid<ChiTietDichVu> chiTietDichVusDataGrid;
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
    @ViewComponent
    private DataGrid<LichSuThanhToan> lichSuThanhToansDataGrid;
    @ViewComponent
    private CollectionLoader<LichSuThanhToan> lichSuThanhToansDl;
    @ViewComponent
    private TypedTextField<Long> tongTienField;
    @ViewComponent
    private TypedTextField<String> khuyenMaiField;
    @ViewComponent
    private TypedTextField<Long> daThanhToanField;
    @ViewComponent
    private TypedTextField<Long> tongTienSauKhuyenMaiField;
    @ViewComponent
    private TypedTextField<Integer> phaiDongField;
    @ViewComponent
    private TypedDatePicker<Date> ngayThanhToanField;
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
    @Autowired
    private TinhKpiChiTietService tinhKpiChiTietService;
    @Autowired
    private ChiTietDieuTriPaymentSummaryService paymentSummaryService;
    @Autowired
    private DonThuocService donThuocService;
    @Autowired
    private com.company.clinicportal.lienthong.LienThongGuiDonThuocService lienThongService;
    @Autowired
    private com.company.clinicportal.lienthong.CoSoKhamChuaBenhLienThongService coSoService;
    @Autowired
    private com.company.clinicportal.lienthong.SecretCipher secretCipher;
    @Autowired
    private com.company.clinicportal.lienthong.LienThongPasswordPrompt lienThongPasswordPrompt;

    /** Tên bác sĩ mặc định khi tạo đơn thuốc nhanh từ chi tiết phiếu điều trị. */
    private static final String DEFAULT_TEN_BAC_SI = "BS. Đặng Thị Hà";

    @ViewComponent
    private DataGrid<DonThuoc> donThuocsDataGrid;
    @ViewComponent
    private CollectionLoader<DonThuoc> donThuocsDl;
    @ViewComponent
    private CollectionContainer<DonThuoc> donThuocsDc;

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
    @ViewComponent
    private JmixButton addDonThuocButton;
    @ViewComponent
    private JmixButton dongBoDonThuocButton;

    public void setIdBenhNhan(BenhNhan idBenhNhan) {
        this.idBenhNhan = idBenhNhan;
    }


    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        autoFillTongKetDieuTriFields();

        khuyenMaiField.setValueChangeMode(ValueChangeMode.EAGER);
        khuyenMaiField.addValueChangeListener(event1 -> recalculatePaymentFields());

        if (getEditedEntity().getId() != null) {
            lichSuThanhToansDl.setParameter("idChiTietDieuTri", getEditedEntity());
            lichSuThanhToansDl.load();
        }

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        // ✅ Column Giá
        Grid.Column<ChiTietDichVu> giaColumn = chiTietDichVusDataGrid.addColumn(chiTiet -> {

                    if (chiTiet.getIdDichVu() == null) return "";

                    Long gia = TinhTheoGia.LE.equals(chiTiet.getTinhTheoGia())
                            ? chiTiet.getIdDichVu().getGiaBuoiLe()
                            : chiTiet.getIdDichVu().getGia();

                    return gia != null ? formatter.format(gia) : "0";

                }).setHeader("Giá")
                .setKey("giaColumn")
                .setAutoWidth(true)
                .setSortable(true);

        // ✅ Column Thao tác
        Grid.Column<ChiTietDichVu> actionColumn = chiTietDichVusDataGrid.addComponentColumn(chiTietDichVu -> {
                    JmixButton button = uiComponents.create(JmixButton.class);
                    button.setText("Chi tiết");

                    button.addClickListener(e -> {
                        if (chiTietDichVu != null && chiTietDichVu.getId() != null) {

                            DialogWindow<View<?>> window =
                                    dialogWindows.view(this, "BuoiDieuTri.list")
                                            .build();

                            BuoiDieuTriListView view = (BuoiDieuTriListView) window.getView();
                            view.setIdChiTietDichVu(chiTietDichVu.getId());

                            window.open();
                        }
                    });

                    return button;
                }).setHeader("Thao tác")
                .setKey("actionColumn")
                .setAutoWidth(true);

        // ✅ Set đúng thứ tự column
        chiTietDichVusDataGrid.setColumnOrder(
                chiTietDichVusDataGrid.getColumnByKey("tenDichVu"),
                chiTietDichVusDataGrid.getColumnByKey("nhomDichVu"),
                chiTietDichVusDataGrid.getColumnByKey("ghiChu"),
                giaColumn,
                chiTietDichVusDataGrid.getColumnByKey("soLuong"),
                chiTietDichVusDataGrid.getColumnByKey("ngayBatDau"),
                actionColumn
        );

        if (getEditedEntity() != null) {
            configureToDieuTriGridColumns();
            toDieuTrisDl.setParameter("idChiTietDieuTri", getEditedEntity());
            toDieuTrisDl.load();
        }

        // Load danh sách đơn thuốc của chi tiết phiếu điều trị hiện tại và cấu hình cột thao tác.
        if (getEditedEntity() != null && getEditedEntity().getId() != null) {
            donThuocsDl.setParameter("idChiTietDieuTri", getEditedEntity());
            donThuocsDl.load();
        }
        configureDonThuocGridColumns();

        recalculatePaymentFields();
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
        openButton.setIcon(com.vaadin.flow.component.icon.VaadinIcon.EYE.create());
        openButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        openButton.setTitle("Mở xem");
        openButton.addClickListener(e -> openDonThuocDetail(donThuoc));

        JmixButton editButton = uiComponents.create(JmixButton.class);
        editButton.setIcon(com.vaadin.flow.component.icon.VaadinIcon.EDIT.create());
        editButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        editButton.setTitle("Sửa");
        editButton.addClickListener(e -> openDonThuocDetail(donThuoc));

        JmixButton delButton = uiComponents.create(JmixButton.class);
        delButton.setIcon(com.vaadin.flow.component.icon.VaadinIcon.TRASH.create());
        delButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        delButton.setTitle("Xoá");
        delButton.addClickListener(e -> confirmAndDeleteDonThuoc(donThuoc));

        layout.add(openButton, editButton, delButton);
        return layout;
    }

    /** PostLoad handler để đảm bảo cột thao tác luôn được cấu hình sau khi data load. */
    @Subscribe(id = "donThuocsDl", target = Target.DATA_LOADER)
    public void onDonThuocsDlPostLoad(final io.jmix.flowui.model.CollectionLoader.PostLoadEvent<DonThuoc> event) {
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

    @Subscribe(id = "lichSuThanhToansDl", target = Target.DATA_LOADER)
    public void onLichSuThanhToansDlPostLoad(final CollectionLoader.PostLoadEvent<LichSuThanhToan> event) {
        recalculatePaymentFields();
    }

    @Subscribe("chiTietDichVusDataGrid.create")
    public void onChiTietDichVusDataGridCreate(final ActionPerformedEvent event) {
        DialogWindow<ChiTietDichVuDetailView> window =
                dialogWindows.detail(this, ChiTietDichVu.class)
                        .withViewClass(ChiTietDichVuDetailView.class)
                        .newEntity()
                        .build();

        window.addAfterCloseListener(event1 -> {
            if (event1.closedWith(StandardOutcome.SAVE)) {
                ChiTietDichVu saved = event1.getView().getEditedEntity();
                saved.setIdChiTietPhieuDieuTri(getEditedEntity());
                saved.setCreatedAt(LocalDateTime.now());
                ChiTietDichVu persisted = dataManager.save(saved);

                Long soLuong = persisted.getSoLuong();
                Date ngayBatDau = persisted.getNgayBatDau();
                Long khoangCachBuoiDieuTri = persisted.getKhoangCachBuoiDieuTri();

                if (soLuong != null && soLuong > 0
                        && ngayBatDau != null
                        && khoangCachBuoiDieuTri != null && khoangCachBuoiDieuTri > 0
                        && persisted.getIdChiTietPhieuDieuTri() != null
                        && persisted.getIdChiTietPhieuDieuTri().getIdBenhNhan() != null) {

                    SaveContext saveContext = new SaveContext();

                    for (int i = 0; i < soLuong; i++) {
                        BuoiDieuTri buoiDieuTri = dataManager.create(BuoiDieuTri.class);
                        buoiDieuTri.setIdChiTietDichVu(persisted);
                        buoiDieuTri.setIdChiTietDieuTri(getEditedEntity());
                        buoiDieuTri.setIdBenhNhan(persisted.getIdChiTietPhieuDieuTri().getIdBenhNhan());

                        Calendar ngayThucHienCal = Calendar.getInstance();
                        ngayThucHienCal.setTime(ngayBatDau);
                        ngayThucHienCal.add(Calendar.DAY_OF_MONTH, (int) (khoangCachBuoiDieuTri * i));
                        buoiDieuTri.setNgayThucHien(ngayThucHienCal.getTime());
                        buoiDieuTri.setTrangThai(TrangThaiBuoiDieuTri.CHUA_THUC_HIEN);

                        saveContext.saving(buoiDieuTri);
                    }

if (!saveContext.getEntitiesToSave().isEmpty()) {
                    dataManager.save(saveContext);
                    if (getEditedEntity().getId() != null) {
                        tinhKpiChiTietService.regenerateForChiTietDieuTri(getEditedEntity().getId());
                    }
                }
            }

            chiTietDichVuDc.getMutableItems().add(loadChiTietDichVuForGrid(persisted.getId()));
            recalculatePaymentFields();
        }
        });
        window.open();
    }

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
    public void onAddDonThuocButtonClick(final com.vaadin.flow.component.ClickEvent<JmixButton> event) {
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
            com.company.clinicportal.view.donthuoc.DonThuocQuickAddDialog view =
                    (com.company.clinicportal.view.donthuoc.DonThuocQuickAddDialog) window.getView();
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
     * Nhấn nút "Đồng bộ" trên tab ĐƠN THUỐC → gọi API gửi đơn liên thông NGAY LẬP TỨC
     * (không qua scheduler 30s).
     *
     * <p>Flow:
     * <ol>
     *     <li>Validate đơn thuốc.</li>
     *     <li>Lấy cấu hình cơ sở KCB liên thông.</li>
     *     <li>Gọi API trực tiếp qua {@link LienThongGuiDonThuocService}.</li>
     *     <li>Cập nhật trạng thái đơn và hiển thị kết quả.</li>
     * </ol>
     */
    @Subscribe("dongBoDonThuocButton")
    public void onDongBoDonThuocButtonClick(final com.vaadin.flow.component.ClickEvent<JmixButton> event) {
        // Chỉ lấy đơn thuốc có trạng thái CHO_GUI hoặc GUI_LOI (không gửi NHAP)
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

        // Lấy cơ sở KCB liên thông đầu tiên (active)
        List<com.company.clinicportal.lienthong.entity.CoSoKhamChuaBenhLienThong> coSoList =
                dataManager.load(com.company.clinicportal.lienthong.entity.CoSoKhamChuaBenhLienThong.class)
                        .query("select e from ltcs_CoSoKhamChuaBenhLienThong e where e.active = true")
                        .list();
        if (coSoList.isEmpty()) {
            notifications.create("Chưa có cấu hình cơ sở KCB liên thông. Vui lòng kiểm tra.")
                    .withType(Notifications.Type.ERROR)
                    .show();
            return;
        }
        com.company.clinicportal.lienthong.entity.CoSoKhamChuaBenhLienThong coSo = coSoList.get(0);
        String password = coSoService.decryptPasswordOrNull(coSo);
        if (password == null || password.isBlank()) {
            notifications.create("Cơ sở KCB chưa có password. Vui lòng cấu hình.")
                    .withType(Notifications.Type.ERROR)
                    .show();
            return;
        }

        // Lấy thông tin đăng nhập bác sĩ từ application.properties.
        // Theo FSD §VI: API gửi đơn cần token từ /api/auth/dang-nhap-bac-si.
        com.company.clinicportal.lienthong.LienThongPasswordPrompt.BacSiCredentials creds =
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
        java.util.List<String> successCodes = new java.util.ArrayList<>();
        // Lỗi chỉ đếm + log; không đẩy raw JSON/Unicode vào notification (gây rối UI).
        java.util.List<String> failMuteMessages = new java.util.ArrayList<>();

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
                        : java.util.UUID.randomUUID().toString();
                // maLienThongBacSi + password lấy từ application.properties
                // maLienThongCoSo + passwordCoSo lấy từ DB CoSoKhamChuaBenhLienThong
                com.company.clinicportal.lienthong.LienThongGuiDonThuocService.GuiDonThuocResult result =
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
    public void onPrintToDieuTriButtonClick(final com.vaadin.flow.component.ClickEvent<JmixButton> event) {
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
            window.setWidth("90%");
            window.setHeight("90%");
            window.open();
        } catch (Exception ex) {
            notifications.create("Không thể tạo bản xem trước tờ điều trị. Vui lòng thử lại.")
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
            span.getStyle().set("white-space", "pre-line");
            span.setText(formatKyThuatText(toDieuTri, true));
            return span;
        });
        setColumnRenderer("thoiGianColumn", toDieuTri -> {
            Span span = uiComponents.create(Span.class);
            span.getStyle().set("white-space", "pre-line");
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

    private void setColumnRenderer(String key, java.util.function.Function<ToDieuTri, Span> supplier) {
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
        for (ToDieuTriKyThuat kt : toDieuTri.getKyThuatList()) {
            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            if (tenDichVu) {
                String name = kt.getIdDichVu() != null ? safeText(kt.getIdDichVu().getTenDichVu()) : "";
                sb.append("• ").append(name);
            } else {
                sb.append("• ").append(kt.getThoiGianPhut() != null ? kt.getThoiGianPhut() : "");
            }
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

    @Subscribe("khuyenMaiField")
    public void onKhuyenMaiFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<TypedTextField<String>, String> event) {
        recalculatePaymentFields();
    }

    @Subscribe("khuyenMaiField")
    public void onKhuyenMaiFieldTypedValueChange(final SupportsTypedValue.TypedValueChangeEvent<TypedTextField<String>, String> event) {
        recalculatePaymentFields();
    }

    @Subscribe
    public void onValidation(final ValidationEvent event) {
        String validationError = buildTrongSoValidationError();
        if (validationError != null) {
            event.getErrors().add(validationError);
        }
    }

    private void recalculatePaymentFields() {
        long tongTien = 0L;
        for (ChiTietDichVu item : chiTietDichVuDc.getItems()) {
            long gia = item.getIdDichVu() != null && item.getIdDichVu().getGia() != null
                    ? item.getIdDichVu().getGia()
                    : 0L;
            long soBuoi = item.getSoLuong() != null ? item.getSoLuong() : 0L;
            tongTien += gia * soBuoi;
        }

        BigDecimal tongTienBd = BigDecimal.valueOf(tongTien);
        BigDecimal khuyenMaiPercent = parseFlexibleDecimal(khuyenMaiField.getTypedValue())
                .orElse(BigDecimal.ZERO);
        long daThanhToanTong = 0L;
        Date ngayThanhToanCuoi = null;
        for (LichSuThanhToan lichSuThanhToan : lichSuThanhToansDc.getItems()) {
            daThanhToanTong += lichSuThanhToan.getDaThanhToan() != null ? lichSuThanhToan.getDaThanhToan() : 0L;
            if (lichSuThanhToan.getThanhToanLuc() != null
                    && (ngayThanhToanCuoi == null || lichSuThanhToan.getThanhToanLuc().after(ngayThanhToanCuoi))) {
                ngayThanhToanCuoi = lichSuThanhToan.getThanhToanLuc();
            }
        }
        BigDecimal daThanhToan = BigDecimal.valueOf(daThanhToanTong);

        BigDecimal tongSauKhuyenMaiBd = tongTienBd.subtract(
                tongTienBd.multiply(khuyenMaiPercent)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        );
        long tongSauKhuyenMai = tongSauKhuyenMaiBd.setScale(0, RoundingMode.HALF_UP).longValue();
        int phaiDong = BigDecimal.valueOf(tongSauKhuyenMai)
                .subtract(daThanhToan)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        getEditedEntity().setTongTien(tongTien);
        getEditedEntity().setTongTienSauKhuyenMai(tongSauKhuyenMai);
        getEditedEntity().setDaThanhToan(daThanhToanTong);
        getEditedEntity().setPhaiDong(phaiDong);
        getEditedEntity().setNgayThanhToan(ngayThanhToanCuoi);

        tongTienField.setTypedValue(tongTien);
        tongTienSauKhuyenMaiField.setTypedValue(tongSauKhuyenMai);
        daThanhToanField.setTypedValue(daThanhToanTong);
        phaiDongField.setTypedValue(phaiDong);
        ngayThanhToanField.setTypedValue(ngayThanhToanCuoi);
    }

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

    @Install(to = "lichSuThanhToansDataGrid.create", subject = "newEntitySupplier")
    private LichSuThanhToan lichSuThanhToansDataGridCreateNewEntitySupplier() {
        LichSuThanhToan lichSuThanhToan = metadata.create(LichSuThanhToan.class);
        lichSuThanhToan.setIdChiTietDieuTri(getEditedEntity());
        return lichSuThanhToan;
    }

    private void autoFillTongKetDieuTriFields() {
        ChiTietDieuTri chiTietDieuTri = getEditedEntity();
        if (chiTietDieuTri == null) {
            return;
        }

        if (chiTietDieuTri.getDienBienBenh() == null || chiTietDieuTri.getDienBienBenh().isBlank()) {
            chiTietDieuTri.setDienBienBenh(resolveDienBienBenhFromToDieuTri(chiTietDieuTri));
        }
        if (chiTietDieuTri.getChuanDoanRaVien() == null || chiTietDieuTri.getChuanDoanRaVien().isBlank()) {
            String chuanDoan = chiTietDieuTri.getChuanDoan();
            chiTietDieuTri.setChuanDoanRaVien(chuanDoan != null ? chuanDoan : "");
        }
    }

    private String resolveDienBienBenhFromToDieuTri(ChiTietDieuTri chiTietDieuTri) {
        if (chiTietDieuTri.getId() == null) {
            return "";
        }
        List<ToDieuTri> lines = dataManager.load(ToDieuTri.class)
                .query("select e from ToDieuTri e where e.chiTietDieuTri = :ctdt order by e.tuNgay asc, e.id asc")
                .parameter("ctdt", chiTietDieuTri)
                .list();

        StringBuilder sb = new StringBuilder();
        for (ToDieuTri line : lines) {
            String moTa = line.getMoTaDienBienBenh();
            if (moTa == null || moTa.isBlank()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append("\n\n");
            }
            sb.append(moTa.trim());
        }
        return sb.toString();
    }

    @Install(to = "fileDinhKemDataGrid.create", subject = "newEntitySupplier")
    private ChiTietDieuTriFileDinhKem fileDinhKemDataGridCreateNewEntitySupplier() {
        ChiTietDieuTriFileDinhKem fileDinhKem = metadata.create(ChiTietDieuTriFileDinhKem.class);
        fileDinhKem.setChiTietDieuTri(getEditedEntity());
        return fileDinhKem;
    }

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        recalculatePaymentFields();
        paymentSummaryService.applyTotals(getEditedEntity());
    }

    @Subscribe
    public void onAfterSave(final AfterSaveEvent event) {
        if (getEditedEntity().getId() != null) {
            tinhKpiChiTietService.regenerateForChiTietDieuTri(getEditedEntity().getId());
        }
    }
}
