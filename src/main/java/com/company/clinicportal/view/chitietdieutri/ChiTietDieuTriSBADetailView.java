package com.company.clinicportal.view.chitietdieutri;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.ChiTietDieuTriFileDinhKem;
import com.company.clinicportal.entity.ToDieuTri;
import com.company.clinicportal.entity.ToDieuTriKyThuat;
// TẠM ẨN: logic Lịch sử thanh toán
// import com.company.clinicportal.entity.LichSuThanhToan;
// TẠM ẨN: logic KPI (trọng số)
// import com.company.clinicportal.enumentity.NhomDichVu;
import com.company.clinicportal.enumentity.TinhTheoGia;
import com.company.clinicportal.enumentity.TrangThaiBuoiDieuTri;
import com.company.clinicportal.service.ToDieuTriPrintService;
import com.company.clinicportal.view.buoidieutri.BuoiDieuTriListView;
import com.company.clinicportal.view.chitietdichvu.ChiTietDichVuDetailView;
// TẠM ẨN: logic Lịch sử thanh toán
// import com.company.clinicportal.view.lichsuthanhtoan.LichSuThanhToanDetailView;
import com.company.clinicportal.view.main.MainView;
// TẠM ẨN: logic Thanh toán
// import com.company.clinicportal.service.ChiTietDieuTriPaymentSummaryService;
// TẠM ẨN: logic KPI
// import com.company.clinicportal.service.TinhKpiChiTietService;
// TẠM ẨN: logic Thanh toán (khuyến mãi)
// import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
// TẠM ẨN: logic Thanh toán
// import io.jmix.flowui.component.SupportsTypedValue;
// import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
// TẠM ẨN: logic Thanh toán
// import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
// TẠM ẨN: logic Lịch sử thanh toán
// import io.jmix.flowui.model.CollectionLoader;
// import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

// TẠM ẨN: logic KPI & Thanh toán
// import java.math.BigDecimal;
// import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.ByteArrayInputStream;
import java.util.*;

@Route(value = "chi-tiet-dieu-tri-sbas/:id", layout = MainView.class)
@ViewController(id = "ChiTietDieuTriSBA.detail")
@ViewDescriptor(path = "chi-tiet-dieu-tri-sba-detail-view.xml")
@EditedEntityContainer("chiTietDieuTriDc")
@DialogMode(height = "100%", width = "80%")
public class ChiTietDieuTriSBADetailView extends StandardDetailView<ChiTietDieuTri> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy");
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
    // TẠM ẨN: khối Lịch sử thanh toán
    // @ViewComponent
    // private DataGrid<LichSuThanhToan> lichSuThanhToansDataGrid;
    // @ViewComponent
    // private CollectionLoader<LichSuThanhToan> lichSuThanhToansDl;
    // TẠM ẨN: khối Thông tin thanh toán
    // @ViewComponent
    // private TypedTextField<Long> tongTienField;
    // @ViewComponent
    // private TypedTextField<String> khuyenMaiField;
    // @ViewComponent
    // private TypedTextField<Long> daThanhToanField;
    // @ViewComponent
    // private TypedTextField<Long> tongTienSauKhuyenMaiField;
    // @ViewComponent
    // private TypedTextField<Integer> phaiDongField;
    // @ViewComponent
    // private TypedDatePicker<Date> ngayThanhToanField;
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
    // TẠM ẨN: khối Lịch sử thanh toán
    // @ViewComponent
    // private CollectionContainer<LichSuThanhToan> lichSuThanhToansDc;
    // TẠM ẨN: logic KPI
    // @Autowired
    // private TinhKpiChiTietService tinhKpiChiTietService;
    // TẠM ẨN: logic Thanh toán
    // @Autowired
    // private ChiTietDieuTriPaymentSummaryService paymentSummaryService;

    public void setIdBenhNhan(BenhNhan idBenhNhan) {
        this.idBenhNhan = idBenhNhan;
    }


    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        // TẠM ẨN: logic Thanh toán (khuyến mãi, tính tổng tiền)
        // khuyenMaiField.setValueChangeMode(ValueChangeMode.EAGER);
        // khuyenMaiField.addValueChangeListener(event1 -> recalculatePaymentFields());
        //
        // if (getEditedEntity().getId() != null) {
        //     lichSuThanhToansDl.setParameter("idChiTietDieuTri", getEditedEntity());
        //     lichSuThanhToansDl.load();
        // }

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

        // TẠM ẨN: logic Thanh toán
        // recalculatePaymentFields();
    }

    // TẠM ẨN: logic Lịch sử thanh toán
    // @Subscribe(id = "lichSuThanhToansDl", target = Target.DATA_LOADER)
    // public void onLichSuThanhToansDlPostLoad(final CollectionLoader.PostLoadEvent<LichSuThanhToan> event) {
    //     recalculatePaymentFields();
    // }

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
                        // TẠM ẨN: logic KPI (tính lại sau khi thêm dịch vụ)
                        // if (getEditedEntity().getId() != null) {
                        //     tinhKpiChiTietService.regenerateForChiTietDieuTri(getEditedEntity().getId());
                        // }
                    }
                }

                chiTietDichVuDc.getMutableItems().add(loadChiTietDichVuForGrid(persisted.getId()));
                // TẠM ẨN: logic Thanh toán
                // recalculatePaymentFields();
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

    @Subscribe("printToDieuTriButton")
    public void onPrintToDieuTriButtonClick(final com.vaadin.flow.component.ClickEvent<JmixButton> event) {
        if (getEditedEntity().getId() == null) {
            notifications.create("Vui lòng lưu phiếu điều trị trước khi in.")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        try {
            byte[] pdfBytes = toDieuTriPrintService.generatePdf(getEditedEntity());
            String fileName = "to-dieu-tri-" + getEditedEntity().getId() + ".pdf";
            StreamResource streamResource = new StreamResource(fileName, () -> new ByteArrayInputStream(pdfBytes));
            streamResource.setContentType("application/pdf");
            StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(streamResource);
            UI.getCurrent().getPage().executeJs(
                    "const link = document.createElement('a');"
                            + "link.href = $0;"
                            + "link.download = $1;"
                            + "document.body.appendChild(link);"
                            + "link.click();"
                            + "link.remove();",
                    registration.getResourceUri().toString(),
                    fileName
            );
        } catch (Exception ex) {
            notifications.create("Không thể in tờ điều trị. Vui lòng thử lại.")
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

    // TẠM ẨN: logic Thanh toán (khuyến mãi)
    // @Subscribe("khuyenMaiField")
    // public void onKhuyenMaiFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<TypedTextField<String>, String> event) {
    //     recalculatePaymentFields();
    // }
    //
    // @Subscribe("khuyenMaiField")
    // public void onKhuyenMaiFieldTypedValueChange(final SupportsTypedValue.TypedValueChangeEvent<TypedTextField<String>, String> event) {
    //     recalculatePaymentFields();
    // }

    // TẠM ẨN: logic KPI (validate trọng số 100%)
    // @Subscribe
    // public void onValidation(final ValidationEvent event) {
    //     String validationError = buildTrongSoValidationError();
    //     if (validationError != null) {
    //         event.getErrors().add(validationError);
    //     }
    // }

    // TẠM ẨN: logic Thanh toán (tính tổng tiền, đã thanh toán, phải đóng)
    // private void recalculatePaymentFields() { ... }

    // TẠM ẨN: logic KPI (validate trọng số)
    // private String buildTrongSoValidationError() { ... }
    // private Map<NhomDichVu, BigDecimal> getTrongSoTheoNhom() { ... }
    // private Set<NhomDichVu> getNhomDichVuDuocChiDinh() { ... }
    // private Optional<BigDecimal> parseFlexibleDecimal(String value) { ... }
    // private BigDecimal toBigDecimal(Double value) { ... }
    // private String formatPercent(BigDecimal value) { ... }
    // private String getTenNhomDichVu(NhomDichVu nhomDichVu) { ... }

    // TẠM ẨN: logic Lịch sử thanh toán
    // @Install(to = "lichSuThanhToansDataGrid.create", subject = "newEntitySupplier")
    // private LichSuThanhToan lichSuThanhToansDataGridCreateNewEntitySupplier() {
    //     LichSuThanhToan lichSuThanhToan = metadata.create(LichSuThanhToan.class);
    //     lichSuThanhToan.setIdChiTietDieuTri(getEditedEntity());
    //     return lichSuThanhToan;
    // }

    @Install(to = "fileDinhKemDataGrid.create", subject = "newEntitySupplier")
    private ChiTietDieuTriFileDinhKem fileDinhKemDataGridCreateNewEntitySupplier() {
        ChiTietDieuTriFileDinhKem fileDinhKem = metadata.create(ChiTietDieuTriFileDinhKem.class);
        fileDinhKem.setChiTietDieuTri(getEditedEntity());
        return fileDinhKem;
    }

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        // TẠM ẨN: logic Thanh toán
        // recalculatePaymentFields();
        // paymentSummaryService.applyTotals(getEditedEntity());
    }

    @Subscribe
    public void onAfterSave(final AfterSaveEvent event) {
        // TẠM ẨN: logic KPI (tính lại sau khi lưu phiếu)
        // if (getEditedEntity().getId() != null) {
        //     tinhKpiChiTietService.regenerateForChiTietDieuTri(getEditedEntity().getId());
        // }
    }
}
