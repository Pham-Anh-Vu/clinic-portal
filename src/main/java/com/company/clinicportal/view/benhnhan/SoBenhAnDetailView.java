package com.company.clinicportal.view.benhnhan;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.view.chitietdieutri.ChiTietDieuTriListView;
import com.company.clinicportal.view.chitietdieutri.ChiTietDieuTriSBADetailView;
import com.company.clinicportal.view.chitietdieutri.PhieuChiDinhDetailView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.DialogOutcome;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "so-benh-ans/:id", layout = MainView.class)
@ViewController(id = "SoBenhAn.detail")
@ViewDescriptor(path = "so-benh-an-detail-view.xml")
@EditedEntityContainer("benhNhanDc")
@DialogMode(height = "100%", width = "80%")
public class SoBenhAnDetailView extends StandardDetailView<BenhNhan> {
    private static final String SO_BENH_AN_REPORT_TEMPLATE = "/reports/29. Benh an ngoai tru PHCN-in.doc";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired
    private DataManager dataManager;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Messages messages;
    @ViewComponent
    private HorizontalLayout trangThaiBox;
    @ViewComponent
    private HorizontalLayout thoiGianTaiKhamBox;
    @ViewComponent
    private HorizontalLayout ngayKhamBenhBox;

    private BenhNhan idBenhNhan = null;
    @ViewComponent
    private DataGrid<ChiTietDieuTri> chiTietDieuTrisDataGrid;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private Notifications notifications;
    @ViewComponent
    private InstanceLoader<BenhNhan> benhNhanDl;
    @ViewComponent
    private JmixButton editBN;
    @ViewComponent
    private TypedTextField<String> tuoiField;

    public void setIdBenhNhan(BenhNhan idBenhNhan) {
        this.idBenhNhan = idBenhNhan;
    }

    @ViewComponent
    private CollectionLoader<ChiTietDieuTri> chiTietDieuTrisDl;
    @ViewComponent
    private CollectionContainer<ChiTietDieuTri> chiTietDieuTrisDc;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if (idBenhNhan != null) {
            benhNhanDl.setEntityId(idBenhNhan.getId());
            benhNhanDl.load();
        }

        chiTietDieuTrisDl.setParameter("idBenhNhan", idBenhNhan);
        chiTietDieuTrisDl.load();
        applyPaymentSummaryForList();
        refreshPatientInfoSection();

        chiTietDieuTrisDataGrid.addComponentColumn(chiTietDieuTri -> {
            JmixButton button = uiComponents.create(JmixButton.class);
            button.setText("Chi tiết");
            button.addClickListener(e -> {
                DialogWindow<ChiTietDieuTriSBADetailView> windows = dialogWindows.view(this, ChiTietDieuTriSBADetailView.class).build();
                windows.getView().setIdBenhNhan(idBenhNhan);
                windows.open();
            });
            return button;
        }).setHeader("Thao tác").setAutoWidth(true);
    }

    private void applyPaymentSummaryForList() {
        for (ChiTietDieuTri chiTietDieuTri : chiTietDieuTrisDc.getItems()) {
            Number tongTienNumber = dataManager.loadValue(
                            "select coalesce(sum(dv.idDichVu.gia * dv.soLuong), 0) from ChiTietDichVu dv where dv.idChiTietPhieuDieuTri = :ctdt",
                            Number.class
                    )
                    .parameter("ctdt", chiTietDieuTri)
                    .one();

            long tongTien = tongTienNumber != null ? tongTienNumber.longValue() : 0L;
            double khuyenMaiPercent = chiTietDieuTri.getKhuyenMai() != null ? chiTietDieuTri.getKhuyenMai() : 0D;
            long daThanhToan = dataManager.loadValue(
                            "select coalesce(sum(e.daThanhToan), 0) from LichSuThanhToan e where e.idChiTietDieuTri = :ctdt",
                            Long.class
                    )
                    .parameter("ctdt", chiTietDieuTri)
                    .one();
            Date ngayThanhToanCuoi = dataManager.loadValue(
                            "select max(e.thanhToanLuc) from LichSuThanhToan e where e.idChiTietDieuTri = :ctdt",
                            Date.class
                    )
                    .parameter("ctdt", chiTietDieuTri)
                    .optional()
                    .orElse(null);

            BigDecimal tongTienBd = BigDecimal.valueOf(tongTien);
            long tongSauKhuyenMai = tongTienBd.subtract(
                            tongTienBd.multiply(BigDecimal.valueOf(khuyenMaiPercent))
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    )
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
            int phaiDong = BigDecimal.valueOf(tongSauKhuyenMai)
                    .subtract(BigDecimal.valueOf(daThanhToan))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();

            chiTietDieuTri.setTongTien(tongTien);
            chiTietDieuTri.setTongTienSauKhuyenMai(tongSauKhuyenMai);
            chiTietDieuTri.setDaThanhToan(daThanhToan);
            chiTietDieuTri.setPhaiDong(phaiDong);
            chiTietDieuTri.setNgayThanhToan(ngayThanhToanCuoi);
        }
    }

    @Subscribe("chiTietDieuTrisDataGrid.create")
    public void onChiTietDieuTrisDataGridCreate(final ActionPerformedEvent event) {
        DialogWindow<PhieuChiDinhDetailView> dialogWindow =  dialogWindows.detail(this, ChiTietDieuTri.class)
                .withViewClass(PhieuChiDinhDetailView.class)
                .newEntity()
                .build();
        dialogWindow.getView().setIdBenhNhan(idBenhNhan.getId());
        dialogWindow.setWidth("80%");
        dialogWindow.setHeight("100%");

        // Reload datagrid after dialog closes
        dialogWindow.addAfterCloseListener(event1 -> {
            if(idBenhNhan != null){
                chiTietDieuTrisDl.setParameter("idBenhNhan", idBenhNhan);
                chiTietDieuTrisDl.load();
                applyPaymentSummaryForList();
            }
        });

        dialogWindow.open();
    }

    @Subscribe("editBN")
    public void onEditBNClick(final com.vaadin.flow.component.ClickEvent<JmixButton> event) {
        DialogWindow<TrangThaiBenhNhanDetailView> window = dialogWindows.detail(this, BenhNhan.class)
                .editEntity(getEditedEntity()) // chỉnh sửa entity hiện tại
                .withViewClass(TrangThaiBenhNhanDetailView.class)
                .build();
        window.addAfterCloseListener(e1 -> {
            benhNhanDl.load();
            refreshPatientInfoSection();
        });
        window.open();
    }

    private void refreshPatientInfoSection() {
        tuoiField.setValue(BenhNhan.calculateTuoi(getEditedEntity().getNgaySinh()));

        Span span = uiComponents.create(Span.class);
        Span spanTG = uiComponents.create(Span.class);
        Span spanNK = uiComponents.create(Span.class);

        if (getEditedEntity().getTrangThaiKhamBenh() != null) {
            var trangThai = getEditedEntity().getTrangThaiKhamBenh();
            span.setText(messages.getMessage(trangThai));
            span.addClassName(trangThai.toString());
        }

        if (getEditedEntity().getThoiGianTaiKham() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            spanTG.setText(sdf.format(getEditedEntity().getThoiGianTaiKham()));
        }

        if (getEditedEntity().getNgayKhamBenh() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            spanNK.setText(sdf.format(getEditedEntity().getNgayKhamBenh()));
        }

        Span label = uiComponents.create(Span.class);
        label.setText("Trạng thái: ");
        label.addClassName("trang-thai-label");

        Span labelTG = uiComponents.create(Span.class);
        labelTG.setText("Thời gian tái khám: ");
        labelTG.addClassName("trang-thai-label");

        Span labelNK = uiComponents.create(Span.class);
        labelNK.setText("Ngày khám bệnh: ");
        labelNK.addClassName("trang-thai-label");

        trangThaiBox.removeAll();
        trangThaiBox.add(label, span);

        thoiGianTaiKhamBox.removeAll();
        thoiGianTaiKhamBox.add(labelTG, spanTG);

        ngayKhamBenhBox.removeAll();
        ngayKhamBenhBox.add(labelNK, spanNK);
    }

    @Subscribe("printReportButton")
    public void onPrintReportButtonClick(final com.vaadin.flow.component.ClickEvent<JmixButton> event) {
        ChiTietDieuTri selected = chiTietDieuTrisDataGrid.getSingleSelectedItem();
        if (selected == null) {
            notifications.create("Vui lòng chọn 1 phiếu trong danh sách để in báo cáo.")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        ChiTietDieuTri chiTietDieuTri = selected;
        try {
            byte[] fileBytes = generateSoBenhAnReportDoc(chiTietDieuTri);
            String fileName = buildReportFileName(chiTietDieuTri);
            StreamResource streamResource = new StreamResource(fileName, () -> new ByteArrayInputStream(fileBytes));
            StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(streamResource);
            UI.getCurrent().getPage().open(registration.getResourceUri().toString());
        } catch (Exception ex) {
            notifications.create("Không thể tạo file báo cáo. Vui lòng kiểm tra mẫu in.")
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    private byte[] generateSoBenhAnReportDoc(ChiTietDieuTri chiTietDieuTri) throws IOException {
        BenhNhan benhNhan = null;
        if (chiTietDieuTri.getIdBenhNhan() != null && chiTietDieuTri.getIdBenhNhan().getId() != null) {
            benhNhan = dataManager.load(BenhNhan.class)
                    .id(chiTietDieuTri.getIdBenhNhan().getId())
                    .optional()
                    .orElse(null);
        }

        List<ChiTietDichVu> chiTietDichVus = dataManager.load(ChiTietDichVu.class)
                .query("select e from ChiTietDichVu e where e.idChiTietPhieuDieuTri = :ctdt order by e.id")
                .parameter("ctdt", chiTietDieuTri)
                .list();

        Set<String> tenDichVus = new LinkedHashSet<>();
        Set<String> ngayBatDauDichVus = new LinkedHashSet<>();
        Set<String> bacSis = new LinkedHashSet<>();
        for (ChiTietDichVu chiTietDichVu : chiTietDichVus) {
            if (chiTietDichVu.getIdDichVu() != null && chiTietDichVu.getIdDichVu().getTenDichVu() != null) {
                tenDichVus.add(chiTietDichVu.getIdDichVu().getTenDichVu());
            }
            if (chiTietDichVu.getNgayBatDau() != null) {
                ngayBatDauDichVus.add(DATE_FORMAT.format(chiTietDichVu.getNgayBatDau()));
            }
            if (chiTietDichVu.getIdBacSi() != null && chiTietDichVu.getIdBacSi().getHoTen() != null) {
                bacSis.add(chiTietDichVu.getIdBacSi().getHoTen());
            }
        }

        String tenDichVuText = String.join("; ", tenDichVus);
        String ngayBatDauText = String.join("; ", ngayBatDauDichVus);
        String bacSiText = !bacSis.isEmpty()
                ? String.join("; ", bacSis)
                : safeText(chiTietDieuTri.getIdNhanSu() != null ? chiTietDieuTri.getIdNhanSu().getHoTen() : null);

        Map<String, String> values = new HashMap<>();
        values.put("${ChiTietDieuTri.chuanDoan}", safeText(chiTietDieuTri.getChuanDoan()));
        values.put("${ChiTietDieuTri.chuanDoanRaVien}", safeText(chiTietDieuTri.getChuanDoanRaVien()));
        values.put("${ChiTietDieuTri.daXuLy}", safeText(chiTietDieuTri.getDaXuLy()));
        values.put("${ChiTietDieuTri.dienBienBenh}", safeText(chiTietDieuTri.getDienBienBenh()));
        values.put("${ChiTietDieuTri.huongDieuTri}", safeText(chiTietDieuTri.getHuongDieuTri()));
        values.put("${ChiTietDieuTri.kbBoPhan}", safeText(chiTietDieuTri.getKbBoPhan()));
        values.put("${ChiTietDieuTri.ketQuaCanLamSang}", safeText(chiTietDieuTri.getKetQuaCanLamSang()));
        values.put("${ChiTietDieuTri.khamBenhQuaTrinhBenh}", safeText(chiTietDieuTri.getKhamBenhQuaTrinhBenh()));
        values.put("${ChiTietDieuTri.khamBenhTienSuBenh}", safeText(chiTietDieuTri.getKhamBenhTienSuBenh()));
        values.put("${ChiTietDieuTri.khamBenhToanThan}", safeText(chiTietDieuTri.getKhamBenhToanThan()));
        values.put("${ChiTietDieuTri.lyDoVaoVien}", safeText(chiTietDieuTri.getLyDoVaoVien()));
        values.put("${ChiTietDieuTri.ngayBatDau}", formatDate(chiTietDieuTri.getNgayBatDau()));
        values.put("${ChiTietDieuTri.ngayKetThuc}", formatDate(chiTietDieuTri.getNgayKetThuc()));
        values.put("${ChiTietDieuTri.idPhieuDieuTri.ngayKham}",
                formatDate(benhNhan != null ? benhNhan.getNgayKhamBenh() : null));

        values.put("${ChiTietDieuTri.idBenhNhan.hoVaTen}",
                safeText(benhNhan != null ? benhNhan.getHoVaTen() : null));
        values.put("${ChiTietDieuTri.idBenhNhan.gioiTinh}",
                safeText(benhNhan != null ? benhNhan.getGioiTinh() : null));
        values.put("${ChiTietDieuTri.idBenhNhan.ngaySinh}",
                formatDate(benhNhan != null ? benhNhan.getNgaySinh() : null));
        values.put("${ChiTietDieuTri.idBenhNhan.tuoi}",
                safeText(benhNhan != null ? BenhNhan.calculateTuoi(benhNhan.getNgaySinh()) : null));
        values.put("${ChiTietDieuTri.idBenhNhan.diaChi}",
                safeText(benhNhan != null ? benhNhan.getDiaChi() : null));
        values.put("${ChiTietDieuTri.idBenhNhan.hoTenNguoiThan}",
                safeText(benhNhan != null ? benhNhan.getHoTenNguoiThan() : null));
        values.put("${ChiTietDieuTri.idBenhNhan.sdtNguoiThan}",
                safeText(benhNhan != null ? benhNhan.getSdtNguoiThan() : null));

        values.put("${idDichVu.tenDichVu}", tenDichVuText);
        values.put("${idBacSi.hoTen}", bacSiText);
        values.put("${ngayBatDau}", ngayBatDauText);

        try (InputStream inputStream = getClass().getResourceAsStream(SO_BENH_AN_REPORT_TEMPLATE)) {
            if (inputStream == null) {
                throw new IOException("Template not found: " + SO_BENH_AN_REPORT_TEMPLATE);
            }
            HWPFDocument document = new HWPFDocument(inputStream);
            Range range = document.getRange();
            for (Map.Entry<String, String> entry : values.entrySet()) {
                range.replaceText(entry.getKey(), entry.getValue());
            }
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                document.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    private String buildReportFileName(ChiTietDieuTri chiTietDieuTri) {
        String baseName = safeText(chiTietDieuTri.getTenPhieuDieuTri());
        if (baseName.isBlank()) {
            baseName = "bao-cao-so-benh-an";
        }
        String normalized = baseName.chars()
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining())
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .trim();
        return normalized + ".doc";
    }

    private String safeText(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

    private String formatDate(Date date) {
        return date != null ? DATE_FORMAT.format(date) : "";
    }
}