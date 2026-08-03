package com.company.clinicportal.view.benhnhan;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.NhanSu;
import com.company.clinicportal.service.ChiTietDieuTriPaymentSummaryService;
import com.company.clinicportal.service.LibreOfficeDocumentConversionService;
import com.company.clinicportal.service.WordTemplateFillService;
import com.company.clinicportal.view.chitietdieutri.ChiTietDieuTriDetailView;
import com.company.clinicportal.view.chitietdieutri.ChiTietDieuTriListView;
import com.company.clinicportal.view.chitietdieutri.ChiTietDieuTriSBADetailView;
import com.company.clinicportal.view.chitietdieutri.PhieuChiDinhDetailView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@Route(value = "so-benh-ans/:id", layout = MainView.class)
@ViewController(id = "SoBenhAn.detail")
@ViewDescriptor(path = "so-benh-an-detail-view.xml")
@EditedEntityContainer("benhNhanDc")
@DialogMode(height = "100%", width = "80%")
public class SoBenhAnDetailView extends StandardDetailView<BenhNhan> {
    private static final Logger log = LoggerFactory.getLogger(SoBenhAnDetailView.class);
    private static final String SO_BENH_AN_REPORT_TEMPLATE_DOC = "/reports/29.-Benh-an-ngoai-tru-PHCN-in (1).docx";
    private static final String TO_DIEU_TRI_TEMPLATE_HTML = "/reports/tờ điều trị BN BCB.html";
    private static final java.util.Set<String> RAW_HTML_PLACEHOLDERS = java.util.Set.of(
            "${chiTietDichVuRows}",
            "${ROWS}"
    );
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    @Autowired
    private DataManager dataManager;
    @Autowired
    private ChiTietDieuTriPaymentSummaryService paymentSummaryService;
    @Autowired
    private WordTemplateFillService wordTemplateFillService;
    @Autowired
    private LibreOfficeDocumentConversionService libreOfficeDocumentConversionService;
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
//    @ViewComponent
//    private DataGrid<BuoiDieuTri> buoiDieuTrisDataGrid;
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
    public void onInit(final InitEvent event) {
        chiTietDieuTrisDataGrid.addComponentColumn(chiTietDieuTri -> {
            JmixButton button = uiComponents.create(JmixButton.class);
            button.setText("Chi tiết");
            button.addClickListener(e -> openChiTietDieuTriDetail(chiTietDieuTri));
            return button;
        }).setHeader("Thao tác").setAutoWidth(true);

//        buoiDieuTrisDataGrid.addComponentColumn(buoiDieuTri -> {
//            JmixButton button = uiComponents.create(JmixButton.class);
//            button.setText("Chi tiết");
//            button.addClickListener(e -> {
//                if (buoiDieuTri != null && buoiDieuTri.getIdChiTietDieuTri() != null) {
//                    openChiTietDieuTriDetail(buoiDieuTri.getIdChiTietDieuTri());
//                }
//            });
//            return button;
//        }).setHeader("Thao tác").setAutoWidth(true);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if (idBenhNhan != null) {
            benhNhanDl.setEntityId(idBenhNhan.getId());
            benhNhanDl.load();
        }

        loadChiTietDieuTriList();
        refreshPatientInfoSection();
    }

    private void loadChiTietDieuTriList() {
        if (idBenhNhan == null) {
            return;
        }
        chiTietDieuTrisDl.setParameter("idBenhNhan", idBenhNhan);
        chiTietDieuTrisDl.load();
    }

    private void openChiTietDieuTriDetail(ChiTietDieuTri chiTietDieuTri) {
        DialogWindow<ChiTietDieuTriSBADetailView> windows = dialogWindows.view(this, ChiTietDieuTriSBADetailView.class).build();
        windows.getView().setIdBenhNhan(idBenhNhan);
        Long phieuId = chiTietDieuTri.getId();
        if (phieuId != null) {
            ChiTietDieuTri toEdit = dataManager.load(ChiTietDieuTri.class).id(phieuId).one();
            windows.getView().setEntityToEdit(toEdit);
        } else {
            windows.getView().setEntityToEdit(chiTietDieuTri);
        }
        windows.addAfterCloseListener(closeEvent -> {
            // Chỉ đồng bộ DB / reload grid khi user lưu; đóng bằng X không cần refresh.
            if (!closeEvent.closedWith(StandardOutcome.SAVE)) {
                return;
            }
            if (phieuId != null) {
                paymentSummaryService.refreshPaymentSummary(phieuId);
            }
            loadChiTietDieuTriList();
        });
        windows.open();
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

        dialogWindow.addAfterCloseListener(event1 -> loadChiTietDieuTriList());

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

    @Subscribe("printReportButton2")
    public void onPrintReportButton2Click(final com.vaadin.flow.component.ClickEvent<JmixButton> event) {
        ChiTietDieuTri selected = chiTietDieuTrisDataGrid.getSingleSelectedItem();
        if (selected == null) {
            notifications.create("Vui lòng chọn 1 phiếu trong danh sách để in báo cáo.")
                    .withThemeVariant(NotificationVariant.LUMO_WARNING)
                    .withPosition(Notification.Position.TOP_END)
                    .withDuration(3000)
                    .show();
            return;
        }

        try {
            notifications.create("Đang tạo bản xem trước...")
                    .withPosition(Notification.Position.TOP_END)
                    .withDuration(3000)
                    .show();

            byte[] wordBytes = generateSoBenhAnDoc(selected);
            String fileName = buildSoBenhAnDocFileName(selected);
            byte[] pdfBytes = libreOfficeDocumentConversionService.convertDocumentToPdf(wordBytes);

            DialogWindow<SoBenhAnPreviewDialogView> window = dialogWindows
                    .view(this, SoBenhAnPreviewDialogView.class)
                    .build();
            window.getView().setPreviewData(pdfBytes, wordBytes, fileName);
            window.setWidth("90%");
            window.setHeight("90%");
            window.open();
        } catch (Exception ex) {
            log.error("Không thể tạo bản xem trước sổ bệnh án cho ChiTietDieuTri id={}", selected.getId(), ex);
            notifications.create("Không thể tạo bản xem trước. Kiểm tra LibreOffice đã cài và cấu hình đúng chưa.")
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    @Subscribe("printReportButton")
    public void onPrintReportButtonClick(final com.vaadin.flow.component.ClickEvent<JmixButton> event) {
        ChiTietDieuTri selected = chiTietDieuTrisDataGrid.getSingleSelectedItem();
        if (selected == null) {
            notifications.create("Vui lòng chọn 1 phiếu trong danh sách để in báo cáo.")
                    .withThemeVariant(NotificationVariant.LUMO_WARNING)
                    .withPosition(Notification.Position.TOP_END)
                    .withDuration(3000)
                    .show();
            return;
        }

        try {
            byte[] fileBytes = generateSoBenhAnDoc(selected);
            String fileName = buildSoBenhAnDocFileName(selected);
            StreamResource streamResource = new StreamResource(fileName, () -> new ByteArrayInputStream(fileBytes));
            streamResource.setContentType("application/msword");
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
            log.error("Không thể tạo/tải sổ bệnh án cho ChiTietDieuTri id={}", selected.getId(), ex);
            notifications.create("Không thể tải sổ bệnh án. Vui lòng kiểm tra mẫu Word.")
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    private byte[] generateSoBenhAnDoc(ChiTietDieuTri chiTietDieuTri) throws Exception {
        Map<String, String> values = buildSoBenhAnPlaceholderValues(chiTietDieuTri);
        return wordTemplateFillService.fillTemplate(SO_BENH_AN_REPORT_TEMPLATE_DOC, values);
    }

    private Map<String, String> buildSoBenhAnPlaceholderValues(ChiTietDieuTri chiTietDieuTri) {
        ChiTietDieuTri ctdt = chiTietDieuTri;
        if (chiTietDieuTri.getId() != null) {
            ctdt = dataManager.load(ChiTietDieuTri.class)
                    .id(chiTietDieuTri.getId())
                    .fetchPlan(fp -> fp
                            .addFetchPlan("_base")
                            .add("idPhieuDieuTri", p -> p.addFetchPlan("_base")))
                    .optional()
                    .orElse(chiTietDieuTri);
        }

        BenhNhan benhNhan = null;
        if (ctdt.getIdBenhNhan() != null && ctdt.getIdBenhNhan().getId() != null) {
            benhNhan = dataManager.load(BenhNhan.class)
                    .id(ctdt.getIdBenhNhan().getId())
                    .optional()
                    .orElse(null);
        }

        List<ChiTietDichVu> chiTietDichVus = dataManager.load(ChiTietDichVu.class)
                .query("select e from ChiTietDichVu e where e.idChiTietPhieuDieuTri = :ctdt order by e.id")
                .parameter("ctdt", ctdt)
                .list();

        Set<String> tenDichVus = new LinkedHashSet<>();
        Set<String> bacSis = new LinkedHashSet<>();
        for (ChiTietDichVu chiTietDichVu : chiTietDichVus) {
            if (chiTietDichVu.getIdDichVu() != null && chiTietDichVu.getIdDichVu().getTenDichVu() != null) {
                tenDichVus.add(chiTietDichVu.getIdDichVu().getTenDichVu());
            }
            if (chiTietDichVu.getIdBacSi() != null && chiTietDichVu.getIdBacSi().getHoTen() != null) {
                bacSis.add(chiTietDichVu.getIdBacSi().getHoTen());
            }
        }

        String tenDichVuText = String.join("; ", tenDichVus);
        String bacSiText = !bacSis.isEmpty()
                ? String.join("; ", bacSis)
                : safeText(ctdt.getIdNhanSu() != null ? ctdt.getIdNhanSu().getHoTen() : null);

        List<Long> chiTietIds = chiTietDichVus.stream()
                .map(ChiTietDichVu::getId)
                .toList();

        List<BuoiDieuTri> allBuoiDieuTris = Collections.emptyList();
        if (!chiTietIds.isEmpty()) {
            allBuoiDieuTris = dataManager.load(BuoiDieuTri.class)
                    .query("select e from BuoiDieuTri e where e.idChiTietDichVu.id in :ids order by e.ngayThucHien, e.gioBatDau, e.id")
                    .parameter("ids", chiTietIds)
                    .list();
        }

        StringBuilder rows = new StringBuilder();
        for (BuoiDieuTri buoi : allBuoiDieuTris) {
            ChiTietDichVu dv = buoi.getIdChiTietDichVu();
            String tenDichVu = dv != null && dv.getIdDichVu() != null
                    ? safeText(dv.getIdDichVu().getTenDichVu())
                    : "";
            String bsChiDinh = dv != null && dv.getIdBacSi() != null
                    ? safeText(dv.getIdBacSi().getHoTen())
                    : safeText(ctdt.getIdNhanSu() != null ? ctdt.getIdNhanSu().getHoTen() : null);
            String nguoiThucHien = safeText(buoi.getHoTenNhanSuThucHien());

            rows.append(formatNgayGioBuoiDieuTri(buoi)).append("||")
                    .append(safeText(ctdt.getDienBienBenh())).append("||")
                    .append(tenDichVu).append("||")
                    .append(safeText(calcMinutes(buoi.getGioBatDau(), buoi.getGioKetThuc()))).append("||")
                    .append(nguoiThucHien).append("||")
                    .append(bsChiDinh)
                    .append("\n");
        }
        if (rows.length() == 0) {
            rows.append("(Chưa có buổi điều trị)||||||\n");
        }

        Map<String, String> values = new HashMap<>();
        values.put("${ChiTietDieuTri.chuanDoan}", safeText(ctdt.getChuanDoan()));
        values.put("${ChiTietDieuTri.chuanDoanRaVien}", safeText(ctdt.getChuanDoanRaVien()));
        values.put("${ChiTietDieuTri.daXuLy}", safeText(ctdt.getDaXuLy()));
        values.put("${ChiTietDieuTri.dienBienBenh}", safeText(ctdt.getDienBienBenh()));
        values.put("${ChiTietDieuTri.huongDieuTri}", tenDichVuText);
        values.put("${ChiTietDieuTri.tinhTrangBenhNhan}", safeText(ctdt.getTinhTrangBenhNhan()));
        values.put("${ChiTietDieuTri.kbBoPhan}", safeText(ctdt.getKbBoPhan()));
        values.put("${ChiTietDieuTri.ketQuaCanLamSang}", safeText(ctdt.getKetQuaCanLamSang()));
        values.put("${ChiTietDieuTri.khamBenhQuaTrinhBenh}", safeText(ctdt.getKhamBenhQuaTrinhBenh()));
        values.put("${ChiTietDieuTri.khamBenhTienSuBenh}", safeText(ctdt.getKhamBenhTienSuBenh()));
        values.put("${ChiTietDieuTri.khamBenhToanThan}", safeText(ctdt.getKhamBenhToanThan()));
        values.put("${ChiTietDieuTri.lyDoVaoVien}", safeText(ctdt.getLyDoVaoVien()));
        values.put("${ChiTietDieuTri.ngayBatDau}", formatDate(ctdt.getNgayBatDau()));
        values.put("${ChiTietDieuTri.ngayKetThuc}", formatDate(ctdt.getNgayKetThuc()));
        Date ngayKhamBenh = resolveNgayKhamBenh(ctdt, benhNhan);
        values.put("${ChiTietDieuTri.idPhieuDieuTri.ngayKham}", formatDate(ngayKhamBenh));
        putChuKyDateValues(values, ngayKhamBenh);

        values.put("${ChiTietDieuTri.idBenhNhan.hoVaTen}",
                safeText(benhNhan != null ? benhNhan.getHoVaTen() : null));
        values.put("${ChiTietDieuTri.idBenhNhan.gioiTinh}",
                formatGioiTinh(benhNhan != null ? benhNhan.getGioiTinh() : null));
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

        values.put("${chiTietDichVuRows}", rows.toString());
        return values;
    }

    private String buildSoBenhAnDocFileName(ChiTietDieuTri chiTietDieuTri) {
        String baseName = safeText(chiTietDieuTri.getTenPhieuDieuTri());
        if (baseName.isBlank()) {
            baseName = "so-benh-an";
        }
        String normalized = baseName.chars()
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining())
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .trim();
        return normalized + ".doc";
    }

    private void appendToDieuTriTableRow(StringBuilder rows,
                                         String ngayGio,
                                         String dienBien,
                                         String tenDichVu,
                                         String thoiGianPhut,
                                         String nguoiThucHien,
                                         String bacSiChiDinh) {
        rows.append("<tr>")
                .append("<td style=\"white-space: pre-wrap; vertical-align: top;\"><div class=\"doc-row\">&bull; ").append(escapeHtml(ngayGio)).append("</div></td>")
                .append("<td style=\"white-space: pre-wrap; vertical-align: top;\"><div class=\"doc-row\">&bull; ").append(escapeHtml(dienBien)).append("</div></td>")
                .append("<td style=\"white-space: pre-wrap; vertical-align: top;\"><div class=\"doc-row\">&bull; ").append(escapeHtml(tenDichVu)).append("</div></td>")
                .append("<td style=\"white-space: pre-wrap; vertical-align: top; text-align: center;\"><div class=\"doc-row\">&bull; ").append(escapeHtml(thoiGianPhut)).append("</div></td>")
                .append("<td style=\"white-space: pre-wrap; vertical-align: top;\"><div class=\"doc-row\">&bull; ").append(escapeHtml(nguoiThucHien)).append("</div></td>")
                .append("<td style=\"white-space: pre-wrap; vertical-align: top;\"><div class=\"doc-row\">&bull; ").append(escapeHtml(bacSiChiDinh)).append("</div></td>")
                .append("</tr>");
    }

    private byte[] generateToDieuTriPdf(ChiTietDieuTri chiTietDieuTri) throws Exception {
        ChiTietDieuTri ctdt = chiTietDieuTri;
        if (chiTietDieuTri.getId() != null) {
            ctdt = dataManager.load(ChiTietDieuTri.class)
                    .id(chiTietDieuTri.getId())
                    .optional()
                    .orElse(chiTietDieuTri);
        }

        BenhNhan benhNhan = null;
        if (ctdt.getIdBenhNhan() != null && ctdt.getIdBenhNhan().getId() != null) {
            benhNhan = dataManager.load(BenhNhan.class)
                    .id(ctdt.getIdBenhNhan().getId())
                    .optional()
                    .orElse(null);
        }

        List<BuoiDieuTri> buoiDieuTris = dataManager.load(BuoiDieuTri.class)
                .query("select e from BuoiDieuTri e where e.idChiTietDieuTri = :ctdt order by e.ngayThucHien, e.gioBatDau, e.id")
                .parameter("ctdt", ctdt)
                .list();

        StringBuilder rows = new StringBuilder();
        for (BuoiDieuTri buoi : buoiDieuTris) {
            ChiTietDichVu dv = buoi.getIdChiTietDichVu();
            String tenDichVu = dv != null && dv.getIdDichVu() != null ? dv.getIdDichVu().getTenDichVu() : "";
            String bsChiDinh = dv != null && dv.getIdBacSi() != null ? dv.getIdBacSi().getHoTen() : safeText(ctdt.getIdNhanSu() != null ? ctdt.getIdNhanSu().getHoTen() : null);
            String nguoiThucHien = safeText(buoi.getHoTenNhanSuThucHien());

            appendToDieuTriTableRow(
                    rows,
                    formatNgayGioBuoiDieuTri(buoi),
                    safeText(ctdt.getDienBienBenh()),
                    tenDichVu,
                    safeText(calcMinutes(buoi.getGioBatDau(), buoi.getGioKetThuc())),
                    nguoiThucHien,
                    bsChiDinh
            );
        }
        if (rows.isEmpty()) {
            rows.append("<tr><td colspan=\"6\" style=\"text-align:center;\">(Chưa có buổi điều trị)</td></tr>");
        }

        Map<String, String> values = new HashMap<>();
        values.put("${BenhNhan.hoVaTen}", safeText(benhNhan != null ? benhNhan.getHoVaTen() : null));
        values.put("${BenhNhan.ngaySinh}", formatDate(benhNhan != null ? benhNhan.getNgaySinh() : null));
        values.put("${BenhNhan.tuoi}", safeText(benhNhan != null ? BenhNhan.calculateTuoi(benhNhan.getNgaySinh()) : null));
        values.put("${BenhNhan.gioiTinh}", formatGioiTinh(benhNhan != null ? benhNhan.getGioiTinh() : null));
        values.put("${BenhNhan.diaChi}", safeText(benhNhan != null ? benhNhan.getDiaChi() : null));
        values.put("${ChiTietDieuTri.chuanDoan}", safeText(ctdt.getChuanDoan()));
        values.put("${ROWS}", rows.toString());

        String htmlTemplate = loadHtmlTemplate(TO_DIEU_TRI_TEMPLATE_HTML);
        String html = applyTemplateValues(htmlTemplate, values, RAW_HTML_PLACEHOLDERS);
        return htmlToPdfBytes(html);
    }

    private String loadHtmlTemplate(String templateResource) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(templateResource)) {
            if (inputStream == null) {
                throw new IOException("Template not found: " + templateResource);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String applyTemplateValues(String htmlTemplate, Map<String, String> values) {
        return applyTemplateValues(htmlTemplate, values, java.util.Set.of());
    }

    private String applyTemplateValues(String htmlTemplate, Map<String, String> values, java.util.Set<String> rawKeys) {
        String result = htmlTemplate;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() != null ? entry.getValue() : "";
            if (rawKeys != null && rawKeys.contains(key)) {
                result = result.replace(key, value);
            } else {
                result = result.replace(key, escapeHtml(value));
            }
        }
        return result;
    }

    private byte[] htmlToPdfBytes(String html) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(Jsoup.parse(html));
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            registerWindowsFonts(builder);
            builder.withW3cDocument(w3cDoc, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        }
    }

    private void registerWindowsFonts(PdfRendererBuilder builder) {
        // Help OpenHTMLtoPDF render Vietnamese reliably on Windows machines.
        // If fonts are missing, OpenHTMLtoPDF falls back and may drop diacritics.
        File fontsDir = new File("C:/Windows/Fonts");
        if (!fontsDir.isDirectory()) return;

        registerFontIfExists(builder, new File(fontsDir, "times.ttf"), "Times New Roman");
        registerFontIfExists(builder, new File(fontsDir, "timesbd.ttf"), "Times New Roman");
        registerFontIfExists(builder, new File(fontsDir, "timesi.ttf"), "Times New Roman");
        registerFontIfExists(builder, new File(fontsDir, "timesbi.ttf"), "Times New Roman");

        registerFontIfExists(builder, new File(fontsDir, "arial.ttf"), "Arial");
        registerFontIfExists(builder, new File(fontsDir, "arialbd.ttf"), "Arial");
        registerFontIfExists(builder, new File(fontsDir, "ariali.ttf"), "Arial");
        registerFontIfExists(builder, new File(fontsDir, "arialbi.ttf"), "Arial");

        registerFontIfExists(builder, new File(fontsDir, "tahoma.ttf"), "Tahoma");
    }

    private void registerFontIfExists(PdfRendererBuilder builder, File file, String family) {
        if (file.isFile()) {
            builder.useFont(file, family);
        }
    }


    private String resolveNguoiThucHien(BuoiDieuTri buoi) {
        if (buoi == null) return "";
        String hoTen = safeText(buoi.getHoTenNhanSuThucHien());
        if (!hoTen.isBlank()) {
            return hoTen;
        }
        return java.util.stream.Stream.of(buoi.getIdNhanSuStaging(), buoi.getIdNhanSu2Staging())
                .filter(Objects::nonNull)
                .map(NhanSu::getHoTen)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining("; "));
    }

    private String escapeHtml(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private String safeText(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

    private String formatDate(Date date) {
        return date != null ? DATE_FORMAT.format(date) : "";
    }

    private void putChuKyDateValues(Map<String, String> values, Date ngayKhamBenh) {
        Date effectiveDate = ngayKhamBenh != null ? ngayKhamBenh : new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(effectiveDate);
        values.put("${chuKy.ngay}", String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        values.put("${chuKy.thang}", String.valueOf(calendar.get(Calendar.MONTH) + 1));
        values.put("${chuKy.nam}", String.valueOf(calendar.get(Calendar.YEAR)));
    }

    private Date resolveNgayKhamBenh(ChiTietDieuTri chiTietDieuTri, BenhNhan benhNhan) {
        if (chiTietDieuTri != null
                && chiTietDieuTri.getIdPhieuDieuTri() != null
                && chiTietDieuTri.getIdPhieuDieuTri().getNgayKham() != null) {
            return chiTietDieuTri.getIdPhieuDieuTri().getNgayKham();
        }
        if (benhNhan != null && benhNhan.getNgayKhamBenh() != null) {
            return benhNhan.getNgayKhamBenh();
        }
        return null;
    }

    private String formatNgayGioBuoiDieuTri(BuoiDieuTri buoi) {
        if (buoi == null) return "";
        String ngay = formatDate(buoi.getNgayThucHien());
        String gio = buoi.getGioBatDau() != null ? TIME_FORMAT.format(buoi.getGioBatDau()) : "";
        if (!ngay.isBlank() && !gio.isBlank()) return ngay + " " + gio;
        return (ngay + " " + gio).trim();
    }

    private Integer calcMinutes(Date start, Date end) {
        if (start == null || end == null) return null;
        long diffMs = end.getTime() - start.getTime();
        if (diffMs <= 0) return null;
        return (int) Math.round(diffMs / 60000.0);
    }

    private String formatGioiTinh(Object gioiTinh) {
        if (gioiTinh == null) return "";
        String raw = String.valueOf(gioiTinh).trim();
        if (raw.equalsIgnoreCase("NU")) return "Nữ";
        if (raw.equalsIgnoreCase("NAM")) return "Nam";
        if (raw.equalsIgnoreCase("KHAC") || raw.equalsIgnoreCase("OTHER")) return "Khác";
        return raw;
    }
}