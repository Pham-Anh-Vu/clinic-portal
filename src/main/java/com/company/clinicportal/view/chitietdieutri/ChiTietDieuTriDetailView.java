package com.company.clinicportal.view.chitietdieutri;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.LichSuThanhToan;
import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.enumentity.NhomDichVu;
import com.company.clinicportal.enumentity.TrangThaiBuoiDieuTri;
import com.company.clinicportal.view.buoidieutri.BuoiDieuTriDetailView;
import com.company.clinicportal.view.buoidieutri.BuoiDieuTriListView;
import com.company.clinicportal.view.chitietdichvu.ChiTietDichVuDetailView;
import com.company.clinicportal.view.lichsuthanhtoan.LichSuThanhToanDetailView;
import com.company.clinicportal.view.main.MainView;
import com.company.clinicportal.service.TinhKpiChiTietService;
import com.company.clinicportal.service.BuoiDieuTriService;
import com.company.clinicportal.enumentity.CaLamViec;
import com.company.clinicportal.entity.TinhKpi;
import com.company.clinicportal.entity.GiaKpi;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.jmix.core.querycondition.PropertyCondition;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Route(value = "chi-tiet-dieu-tris/:id", layout = MainView.class)
@ViewController(id = "ChiTietDieuTri.detail")
@ViewDescriptor(path = "chi-tiet-dieu-tri-detail-view.xml")
@EditedEntityContainer("chiTietDieuTriDc")
@DialogMode(width = "80%", height = "100%")
public class ChiTietDieuTriDetailView extends StandardDetailView<ChiTietDieuTri> {
    @ViewComponent
    private DataGrid<ChiTietDichVu> chiTietDichVusDataGrid;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private DialogWindows dialogWindows;
    @ViewComponent
    private CollectionLoader<LichSuThanhToan> lichSuThanhToansDl;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Messages messages;
    @ViewComponent
    private HorizontalLayout trangThaiBox;
    @ViewComponent
    private DataGrid<LichSuThanhToan> lichSuThanhToansDataGrid;
    @ViewComponent
    private DataGrid<BuoiDieuTri> buoiDieuTrisDataGrid;
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
    @ViewComponent
    private CollectionPropertyContainer<ChiTietDichVu> chiTietDichVuDc;
    @ViewComponent
    private CollectionContainer<LichSuThanhToan> lichSuThanhToansDc;
    @ViewComponent
    private InstanceLoader<ChiTietDieuTri> chiTietDieuTriDl;
    @ViewComponent
    private CollectionLoader<BuoiDieuTri> buoiDieuTrisDl;
    @Autowired
    private TinhKpiChiTietService tinhKpiChiTietService;
    @ViewComponent
    private CollectionContainer<BuoiDieuTri> buoiDieuTrisDc;

    @Subscribe
    public void onInit(InitEvent event) {
        khuyenMaiField.setValueChangeMode(ValueChangeMode.EAGER);
        khuyenMaiField.addValueChangeListener(event1 -> recalculatePaymentFields());

        chiTietDichVusDataGrid.addComponentColumn(chiTietDichVu -> {
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
        }).setHeader("Thao tác").setAutoWidth(true);

        buoiDieuTrisDataGrid.addComponentColumn(buoiDieuTri -> {
            JmixButton button = uiComponents.create(JmixButton.class);
            button.setText("Chi tiết");
            button.addClickListener(e -> openBuoiDieuTriDetail(buoiDieuTri, false));
            return button;
        }).setHeader("Thao tác").setAutoWidth(true);

        lichSuThanhToansDataGrid.addComponentColumn(lichSuThanhToan -> {
            JmixButton button = uiComponents.create(JmixButton.class);
            button.setText("Chi tiết");
            button.addClickListener(e -> {
                DialogWindow<LichSuThanhToanDetailView> window =
                        dialogWindows.detail(this, LichSuThanhToan.class)
                                .withViewClass(LichSuThanhToanDetailView.class)
                                .editEntity(lichSuThanhToan)
                                .build();
                window.addAfterCloseListener(eTT -> {
                    if (getEditedEntity().getId() != null) {
                        lichSuThanhToansDl.setParameter("idChiTietDieuTri", getEditedEntity());
                        lichSuThanhToansDl.load();
                    }
                });
                window.getView().setChiTietDieuTri(lichSuThanhToan.getIdChiTietDieuTri());
                window.open();
            });
            return button;
        }).setHeader("Thao tác").setAutoWidth(true);
    }



    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if (getEditedEntity().getId() != null) {
            lichSuThanhToansDl.setParameter("idChiTietDieuTri", getEditedEntity());
            lichSuThanhToansDl.load();
        }

        autoFillTongKetDieuTriFields();

        buoiDieuTrisDl.setParameter("idChiTietDieuTri", getEditedEntity());
        buoiDieuTrisDl.load();

        Span span = uiComponents.create(Span.class);
        var benhNhan = getEditedEntity().getIdBenhNhan();
        if (benhNhan != null && benhNhan.getTrangThaiKhamBenh() != null) {
            var trangThai = benhNhan.getTrangThaiKhamBenh();
            span.setText(messages.getMessage(trangThai));
            span.addClassName(trangThai.toString()); // gán class CSS (VD: DANG_DT, DA_DT, KHONG_DT)
        }

        Span label = uiComponents.create(Span.class);
        label.setText("Trạng thái: ");
        label.addClassName("trang-thai-label");

        trangThaiBox.removeAll();
        trangThaiBox.add(label, span);
        recalculatePaymentFields();
    }

    @Subscribe(id = "lichSuThanhToansDl", target = Target.DATA_LOADER)
    public void onLichSuThanhToansDlPostLoad(final CollectionLoader.PostLoadEvent<LichSuThanhToan> event) {
        recalculatePaymentFields();
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

    private void autoFillTongKetDieuTriFields() {
        ChiTietDieuTri chiTietDieuTri = getEditedEntity();
        if (chiTietDieuTri == null) {
            return;
        }

        if (chiTietDieuTri.getDienBienBenh() == null || chiTietDieuTri.getDienBienBenh().isBlank()) {
            chiTietDieuTri.setDienBienBenh(resolveDienBienBenh(chiTietDieuTri));
        }
        if (chiTietDieuTri.getChuanDoanRaVien() == null || chiTietDieuTri.getChuanDoanRaVien().isBlank()) {
            chiTietDieuTri.setChuanDoanRaVien(resolveChuanDoanRaVien(chiTietDieuTri));
        }
    }

    private String resolveDienBienBenh(ChiTietDieuTri chiTietDieuTri) {
        String dienBienBenh = firstNonBlank(
                chiTietDieuTri.getKhamBenhQuaTrinhBenh(),
                chiTietDieuTri.getLyDoVaoVien(),
                extractPhieuDieuTriGhiChu(chiTietDieuTri),
                getBenhNhanTrangThaiText(chiTietDieuTri.getIdBenhNhan())
        );
        return dienBienBenh != null ? dienBienBenh : "";
    }

    private String resolveChuanDoanRaVien(ChiTietDieuTri chiTietDieuTri) {
        String chuanDoanRaVien = firstNonBlank(
                chiTietDieuTri.getChuanDoan(),
                extractPhieuDieuTriGhiChu(chiTietDieuTri)
        );
        return chuanDoanRaVien != null ? chuanDoanRaVien : "";
    }

    private String extractPhieuDieuTriGhiChu(ChiTietDieuTri chiTietDieuTri) {
        PhieuDieuTri phieuDieuTri = chiTietDieuTri.getIdPhieuDieuTri();
        return phieuDieuTri != null ? phieuDieuTri.getGhiChu() : null;
    }

    private String getBenhNhanTrangThaiText(BenhNhan benhNhan) {
        if (benhNhan == null || benhNhan.getTrangThaiKhamBenh() == null) {
            return null;
        }
        return messages.getMessage(benhNhan.getTrangThaiKhamBenh());
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
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

    @Install(to = "buoiDieuTrisDataGrid.create", subject = "newEntitySupplier")
    private BuoiDieuTri buoiDieuTrisDataGridCreateNewEntitySupplier() {
        BuoiDieuTri buoiDieuTri = metadata.create(BuoiDieuTri.class);
        buoiDieuTri.setIdChiTietDieuTri(getEditedEntity());
        buoiDieuTri.setIdBenhNhan(getEditedEntity().getIdBenhNhan());
        return buoiDieuTri;
    }

//    @Install(to = "buoiDieuTrisDataGrid.sttColumn", subject = "renderer")
//    private ComponentRenderer<Span, BuoiDieuTri> buoiDieuTrisDataGridSttColumnRenderer() {
//        return new ComponentRenderer<>(buoiDieuTri -> {
//            Span span = uiComponents.create(Span.class);
//            int stt = buoiDieuTrisDc.getItems().indexOf(buoiDieuTri) + 1;
//            span.setText(stt > 0 ? String.valueOf(stt) : "");
//            return span;
//        });
//    }
//
//    @Install(to = "buoiDieuTrisDataGrid.ngayGioColumn", subject = "renderer")
//    private ComponentRenderer<Span, BuoiDieuTri> buoiDieuTrisDataGridNgayGioColumnRenderer() {
//        return new ComponentRenderer<>(buoiDieuTri -> {
//            Span span = uiComponents.create(Span.class);
//            span.setText(formatBuoiDieuTriNgayGio(buoiDieuTri));
//            return span;
//        });
//    }
//
//    @Install(to = "buoiDieuTrisDataGrid.thoiGianColumn", subject = "renderer")
//    private ComponentRenderer<Span, BuoiDieuTri> buoiDieuTrisDataGridThoiGianColumnRenderer() {
//        return new ComponentRenderer<>(buoiDieuTri -> {
//            Span span = uiComponents.create(Span.class);
//            span.setText(formatBuoiDieuTriThoiGian(buoiDieuTri));
//            return span;
//        });
//    }
//
//    @Install(to = "buoiDieuTrisDataGrid.thaoTacColumn", subject = "renderer")
//    private ComponentRenderer<HorizontalLayout, BuoiDieuTri> buoiDieuTrisDataGridThaoTacColumnRenderer() {
//        return new ComponentRenderer<>(buoiDieuTri -> {
//            HorizontalLayout layout = uiComponents.create(HorizontalLayout.class);
//            layout.setSpacing(true);
//
//            JmixButton detailButton = uiComponents.create(JmixButton.class);
//            detailButton.setText("Chi tiết");
//            detailButton.addClickListener(e -> openBuoiDieuTriDetail(buoiDieuTri));
//
//            JmixButton editButton = uiComponents.create(JmixButton.class);
//            editButton.setText("Sửa");
//            editButton.addClickListener(e -> openBuoiDieuTriDetail(buoiDieuTri));
//
//            JmixButton deleteButton = uiComponents.create(JmixButton.class);
//            deleteButton.setText("Xóa");
//            deleteButton.addClickListener(e -> deleteBuoiDieuTri(buoiDieuTri));
//
//            layout.add(detailButton, editButton, deleteButton);
//            return layout;
//        });
//    }

    private void openBuoiDieuTriDetail(BuoiDieuTri buoiDieuTri) {
        if (buoiDieuTri == null || buoiDieuTri.getId() == null) {
            return;
        }
        DialogWindow<BuoiDieuTriDetailView> window = dialogWindows.detail(this, BuoiDieuTri.class)
                .withViewClass(BuoiDieuTriDetailView.class)
                .editEntity(buoiDieuTri)
                .build();
        window.addAfterCloseListener(event -> buoiDieuTrisDl.load());
        window.open();
    }

    

    private void openBuoiDieuTriDetail(BuoiDieuTri buoiDieuTri, boolean newEntity) {
        DialogWindow<BuoiDieuTriDetailView> window = dialogWindows.detail(this, BuoiDieuTri.class)
                .withViewClass(BuoiDieuTriDetailView.class)
                .editEntity(buoiDieuTri)
                .build();
        window.addAfterCloseListener(event -> buoiDieuTrisDl.load());
        if (newEntity) {
            window.getView().getEditedEntity().setIdChiTietDieuTri(getEditedEntity());
            window.getView().getEditedEntity().setIdBenhNhan(getEditedEntity().getIdBenhNhan());
        }
        window.open();
    }

    private void deleteBuoiDieuTri(BuoiDieuTri buoiDieuTri) {
        if (buoiDieuTri != null && buoiDieuTri.getId() != null) {
            dataManager.remove(buoiDieuTri);
            buoiDieuTrisDl.load();
        }
    }

    private String formatBuoiDieuTriNgayGio(BuoiDieuTri buoiDieuTri) {
        if (buoiDieuTri == null || buoiDieuTri.getNgayThucHien() == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String ngay = formatter.format(buoiDieuTri.getNgayThucHien().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        String gioBatDau = formatTime(buoiDieuTri.getGioBatDau());
        String gioKetThuc = formatTime(buoiDieuTri.getGioKetThuc());
        if (!gioBatDau.isBlank() && !gioKetThuc.isBlank()) {
            return ngay + " " + gioBatDau + " - " + gioKetThuc;
        }
        if (!gioBatDau.isBlank()) {
            return ngay + " " + gioBatDau;
        }
        return ngay;
    }

    private String formatBuoiDieuTriThoiGian(BuoiDieuTri buoiDieuTri) {
        if (buoiDieuTri == null || buoiDieuTri.getGioBatDau() == null || buoiDieuTri.getGioKetThuc() == null) {
            return "";
        }
        long minutes = (buoiDieuTri.getGioKetThuc().getTime() - buoiDieuTri.getGioBatDau().getTime()) / 60000L;
        return minutes > 0 ? String.valueOf(minutes) : "";
    }

    private String formatTime(Date time) {
        if (time == null) {
            return "";
        }
        return DateTimeFormatter.ofPattern("HH:mm")
                .format(time.toInstant().atZone(ZoneId.systemDefault()).toLocalTime());
    }

    @Subscribe
    public void onAfterSave(final AfterSaveEvent event) {
        if (getEditedEntity().getId() != null) {
            tinhKpiChiTietService.regenerateForChiTietDieuTri(getEditedEntity().getId());
        }
    }

    @Subscribe(id = "buoiDieuTrisDl", target = Target.DATA_LOADER)
    public void onBuoiDieuTrisDlPostLoad(final CollectionLoader.PostLoadEvent<BuoiDieuTri> event) {
        // dữ liệu hiển thị cho tab Tờ điều trị
    }

    @Subscribe("buoiDieuTrisDataGrid.create")
    public void onBuoiDieuTrisDataGridCreate(final ActionPerformedEvent event) {
        BuoiDieuTri buoiDieuTri = metadata.create(BuoiDieuTri.class);
        buoiDieuTri.setIdChiTietDieuTri(getEditedEntity());
        buoiDieuTri.setIdBenhNhan(getEditedEntity().getIdBenhNhan());
        openBuoiDieuTriDetail(buoiDieuTri);
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

                if (soLuong != null && soLuong > 0 &&
                        ngayBatDau != null &&
                        khoangCachBuoiDieuTri != null && khoangCachBuoiDieuTri > 0 &&
                        persisted.getIdChiTietPhieuDieuTri() != null &&
                        persisted.getIdChiTietPhieuDieuTri().getIdBenhNhan() != null) {

                    SaveContext saveContext = new SaveContext();

                    for (int i = 0; i < soLuong; i++) {
                        BuoiDieuTri buoiDieuTri = dataManager.create(BuoiDieuTri.class);
                        buoiDieuTri.setIdChiTietDichVu(persisted);
                        buoiDieuTri.setIdChiTietDieuTri(getEditedEntity());
                        buoiDieuTri.setIdBenhNhan(persisted.getIdChiTietPhieuDieuTri().getIdBenhNhan());

                        Calendar ngayThucHienCal = Calendar.getInstance();
                        ngayThucHienCal.setTime(ngayBatDau);
                        ngayThucHienCal.add(Calendar.DAY_OF_MONTH, (int) (khoangCachBuoiDieuTri * i));
                        Date ngayThucHien = ngayThucHienCal.getTime();
                        buoiDieuTri.setNgayThucHien(ngayThucHien);
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

                chiTietDieuTriDl.load();
                recalculatePaymentFields();
            }
        });
        window.open();
    }

    @Subscribe("chiTietDichVusDataGrid.edit")
    public void onChiTietDichVusDataGridEdit(final ActionPerformedEvent event) {
        // mặc định action list_edit đã xử lý, giữ hook cho mở rộng sau
    }

}