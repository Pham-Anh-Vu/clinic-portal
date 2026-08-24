package com.company.clinicportal.view.benhnhan;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.NhanSu;
import com.company.clinicportal.entity.ToDieuTri;
import com.company.clinicportal.entity.ToDieuTriKyThuat;
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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
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
//@DialogMode(height = "100%", width = "80%")
public class SoBenhAnDetailView extends StandardDetailView<BenhNhan> {
    private static final Logger log = LoggerFactory.getLogger(SoBenhAnDetailView.class);
    private static final String SO_BENH_AN_REPORT_TEMPLATE_DOC = "/reports/29.-Benh-an-ngoai-tru-PHCN-in (3).docx";
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
    private com.company.clinicportal.service.ImageResourceLoader imageResourceLoader;
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
            HorizontalLayout actions = uiComponents.create(HorizontalLayout.class);
            actions.setSpacing(true);
            actions.setPadding(false);

            JmixButton chiTietBtn = uiComponents.create(JmixButton.class);
            chiTietBtn.setText("Chi tiết");
            chiTietBtn.addClickListener(e -> openChiTietDieuTriDetail(chiTietDieuTri));
            actions.add(chiTietBtn);

            JmixButton editBtn = uiComponents.create(JmixButton.class);
            editBtn.setText("Sửa");
            editBtn.addClickListener(e -> openChiTietDieuTriEdit(chiTietDieuTri));
            actions.add(editBtn);

            return actions;
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

    @Override
    protected void processBeforeEnterInternal(BeforeEnterEvent event) {
        super.processBeforeEnterInternal(event);
        loadBenhNhanFromRoute(event.getRouteParameters());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        loadBenhNhanFromRoute(event.getRouteParameters());
        super.beforeEnter(event);
    }

    private void loadBenhNhanFromRoute(RouteParameters params) {
        Optional<String> idOpt = params.get("id");
        if (idOpt.isEmpty()) {
            return;
        }
        try {
            Long id = Long.valueOf(idOpt.get());
            if (idBenhNhan == null || !id.equals(idBenhNhan.getId())) {
                BenhNhan benhNhan = dataManager.load(BenhNhan.class).id(id).one();
                setIdBenhNhan(benhNhan);
            }
        } catch (NumberFormatException e) {
            // ignore — URL :id không hợp lệ, view sẽ mở với idBenhNhan null
        }
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
        dialogWindow.setWidth("60%");
        dialogWindow.setHeight("90%");

        dialogWindow.addAfterCloseListener(event1 -> loadChiTietDieuTriList());

        dialogWindow.open();
    }

    private void openChiTietDieuTriEdit(ChiTietDieuTri chiTietDieuTri) {
        Long phieuId = chiTietDieuTri == null ? null : chiTietDieuTri.getId();
        if (phieuId == null) {
            notifications.create("Không thể sửa phiếu chưa được lưu.")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        DialogWindow<PhieuChiDinhDetailView> dialogWindow = dialogWindows.detail(this, ChiTietDieuTri.class)
                .withViewClass(PhieuChiDinhDetailView.class)
                .editEntity(chiTietDieuTri)
                .build();
        dialogWindow.setWidth("60%");
        dialogWindow.setHeight("90%");
        dialogWindow.addAfterCloseListener(closeEvent -> {
            if (!closeEvent.closedWith(StandardOutcome.SAVE)) {
                return;
            }
            paymentSummaryService.refreshPaymentSummary(phieuId);
            loadChiTietDieuTriList();
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
        // Build values + danh sách base64 ảnh chữ ký theo từng row (cùng thứ tự rows).
        java.util.List<String> chuKyByRow = new java.util.ArrayList<>();
        Map<String, String> values = buildSoBenhAnPlaceholderValues(chiTietDieuTri, chuKyByRow);

        String resourcePath = SO_BENH_AN_REPORT_TEMPLATE_DOC;
        try (java.io.InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Không tìm thấy mẫu Word: " + resourcePath);
            }
            // Gọi overload có truyền list base64 để WordTemplateFillService
            // chèn ảnh chữ ký vào cell "BS chỉ định" của từng row.
            return wordTemplateFillService.fillDocxTemplate(in, values, chuKyByRow);
        }
    }

    private Map<String, String> buildSoBenhAnPlaceholderValues(ChiTietDieuTri chiTietDieuTri) {
        return buildSoBenhAnPlaceholderValues(chiTietDieuTri, new java.util.ArrayList<>());
    }

    /**
     * Build map placeholder cho template DOCX sổ bệnh án.
     * Đồng thời build danh sách base64 ảnh chữ ký tương ứng với từng row
     * (cùng thứ tự với rows được nạp vào {@code ${chiTietDichVuRows}}).
     */
    private Map<String, String> buildSoBenhAnPlaceholderValues(ChiTietDieuTri chiTietDieuTri,
                                                               java.util.List<String> chuKyByRowOut) {
        ChiTietDieuTri ctdt = chiTietDieuTri;
        if (chiTietDieuTri.getId() != null) {
            ctdt = dataManager.load(ChiTietDieuTri.class)
                    .id(chiTietDieuTri.getId())
                    .fetchPlan(fp -> fp
                            .addFetchPlan("_base")
                            .add("idPhieuDieuTri", p -> p.addFetchPlan("_base"))
                            .add("dsChanDoanIcd", p -> p.addFetchPlan("_base")))
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

        List<ToDieuTri> toDieuTris = dataManager.load(ToDieuTri.class)
                .query("select e from ToDieuTri e where e.chiTietDieuTri = :ctdt order by e.tuNgay asc, e.id asc")
                .parameter("ctdt", ctdt)
                .fetchPlan(fp -> fp
                        .addFetchPlan("_base")
                        .add("idNguoiThucHien", nguoiThucHienFp -> nguoiThucHienFp.addFetchPlan("_base"))
                        .add("idBacSiChiDinh", bacSiFp -> bacSiFp.addFetchPlan("_base"))
                        .add("kyThuatList", kyThuatListFp -> kyThuatListFp
                                .addFetchPlan("_base")
                                .add("idDichVu", dichVuFp -> dichVuFp.addFetchPlan("_base"))))
                .list();

        Set<String> tenDichVus = new LinkedHashSet<>();
        Set<String> bacSis = new LinkedHashSet<>();
        for (ToDieuTri toDieuTri : toDieuTris) {
            if (toDieuTri.getIdBacSiChiDinh() != null && toDieuTri.getIdBacSiChiDinh().getHoTen() != null) {
                bacSis.add(toDieuTri.getIdBacSiChiDinh().getHoTen());
            }
            if (toDieuTri.getKyThuatList() != null) {
                for (ToDieuTriKyThuat kyThuat : toDieuTri.getKyThuatList()) {
                    if (kyThuat.getIdDichVu() != null && kyThuat.getIdDichVu().getTenDichVu() != null) {
                        tenDichVus.add(kyThuat.getIdDichVu().getTenDichVu());
                    }
                }
            }
        }

        String tenDichVuText = String.join("; ", tenDichVus);
        String bacSiText = !bacSis.isEmpty()
                ? String.join("; ", bacSis)
                : safeText(ctdt.getIdNhanSu() != null ? ctdt.getIdNhanSu().getHoTen() : null);

        StringBuilder rows = new StringBuilder();
        for (ToDieuTri toDieuTri : toDieuTris) {
            StringBuilder ngayGioBuilder = new StringBuilder();
            if (toDieuTri.getTuNgay() != null) {
                ngayGioBuilder.append(formatDate(toDieuTri.getTuNgay()));
            }
            if (toDieuTri.getDenNgay() != null) {
                if (ngayGioBuilder.length() > 0) {
                    ngayGioBuilder.append(" - ");
                }
                ngayGioBuilder.append(formatDate(toDieuTri.getDenNgay()));
            }
            String ngayGio = ngayGioBuilder.toString();

            String dienBien = safeText(toDieuTri.getMoTaDienBienBenh());

            String tenDichVu;
            int tongPhut = 0;
            Set<String> tenDichVuSet = new LinkedHashSet<>();
            if (toDieuTri.getKyThuatList() != null && !toDieuTri.getKyThuatList().isEmpty()) {
                for (ToDieuTriKyThuat kyThuat : toDieuTri.getKyThuatList()) {
                    if (kyThuat.getIdDichVu() != null && kyThuat.getIdDichVu().getTenDichVu() != null) {
                        tenDichVuSet.add(kyThuat.getIdDichVu().getTenDichVu());
                    }
                    if (kyThuat.getThoiGianPhut() != null) {
                        tongPhut += kyThuat.getThoiGianPhut();
                    }
                }
            }
            tenDichVu = String.join("; ", tenDichVuSet);
            String soPhutText = tongPhut > 0 ? String.valueOf(tongPhut) : "";

            String nguoiThucHien = safeText(toDieuTri.getIdNguoiThucHien() != null
                    ? toDieuTri.getIdNguoiThucHien().getHoTen()
                    : null);

            String bsChiDinh = safeText(toDieuTri.getIdBacSiChiDinh() != null
                    ? toDieuTri.getIdBacSiChiDinh().getHoTen()
                    : null);
            if (bsChiDinh.isBlank()) {
                bsChiDinh = safeText(ctdt.getIdNhanSu() != null ? ctdt.getIdNhanSu().getHoTen() : null);
            }

            // === MỚI: build base64 ảnh chữ ký cho row này ===
            // Chỉ chèn khi có tên bác sĩ (không chèn ảnh vào row trống).
            // Hiện dùng ảnh tĩnh /reports/anh-chu-ky.jpg cho mọi BS chỉ định
            // (giống cách ToDieuTriPrintService đã làm).
            if (!bsChiDinh.isBlank()) {
                chuKyByRowOut.add(imageResourceLoader.getDefaultChuKyBase64());
            } else {
                chuKyByRowOut.add(null);
            }

            rows.append(ngayGio).append("||")
                    .append(dienBien).append("||")
                    .append(tenDichVu).append("||")
                    .append(soPhutText).append("||")
                    .append(nguoiThucHien).append("||")
                    .append(bsChiDinh)
                    .append("\n");
        }
        if (rows.length() == 0) {
            rows.append("(Chưa có tờ điều trị)|||||\n");
        }

        Map<String, String> values = new HashMap<>();
        values.put("${ChiTietDieuTri.chuanDoan}", resolveChuanDoanForReport(ctdt));
        values.put("${ChiTietDieuTri.chuanDoanRaVien}", safeText(ctdt.getChuanDoanRaVien()));
        values.put("${ChiTietDieuTri.daXuLy}", safeText(ctdt.getDaXuLy()));
        values.put("${ChiTietDieuTri.dienBienBenh}", safeText(ctdt.getDienBienBenh()));
        values.put("${ChiTietDieuTri.huongDieuTri}", safeText(ctdt.getHuongDieuTri()));
        values.put("${ChiTietDieuTri.tinhTrangBenhNhan}", safeText(ctdt.getTinhTrangBenhNhan()));
        values.put("${ChiTietDieuTri.kbBoPhan}", safeText(ctdt.getKbBoPhan()));
        values.put("${ChiTietDieuTri.ketQuaCanLamSang}", safeText(ctdt.getKetQuaCanLamSang()));
        values.put("${ChiTietDieuTri.khamBenhQuaTrinhBenh}", safeText(ctdt.getKhamBenhQuaTrinhBenh()));
        values.put("${ChiTietDieuTri.khamBenhTienSuBenh}", safeText(ctdt.getKhamBenhTienSuBenh()));
        values.put("${ChiTietDieuTri.khamBenhToanThan}", safeText(ctdt.getKhamBenhToanThan()));
        values.put("${ChiTietDieuTri.mach}", safeText(ctdt.getMach()));
        values.put("${ChiTietDieuTri.nhietDo}", safeText(ctdt.getNhietDo()));
        values.put("${ChiTietDieuTri.huyetAp}", safeText(ctdt.getHuyetAp()));
        values.put("${ChiTietDieuTri.nhipTho}", safeText(ctdt.getNhipTho()));
        values.put("${ChiTietDieuTri.canNang}", safeText(ctdt.getCanNang()));
        values.put("${ChiTietDieuTri.chieuCao}", safeText(ctdt.getChieuCao()));
        values.put("${ChiTietDieuTri.bmi}", safeText(calculateBmi(ctdt.getCanNang(), ctdt.getChieuCao())));
        values.put("${ChiTietDieuTri.lyDoVaoVien}", safeText(ctdt.getLyDoVaoVien()));
        values.put("${ChiTietDieuTri.ngayBatDau}", formatDate(resolveSoBenhAnDateStart(ctdt)));
        values.put("${ChiTietDieuTri.ngayKetThuc}", formatDate(resolveSoBenhAnDateEnd(ctdt)));
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
        values.put("${ChiTietDieuTri.chuanDoan}", resolveChuanDoanForReport(ctdt));
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

    /**
     * Tính BMI từ cân nặng (kg) và chiều cao (cm) - dùng khi in sổ bệnh án để không
     * phụ thuộc vào transient getter {@code getBmi()} trên entity (tránh lỗi
     * "Unknown property 'bmi'" ở một số tình huống binding của Jmix).
     * Trả về null nếu thiếu dữ liệu hoặc chiều cao không hợp lệ.
     */
    private Double calculateBmi(Double canNang, Double chieuCao) {
        if (canNang == null || chieuCao == null || chieuCao <= 0) {
            return null;
        }
        double chieuCaoMet = chieuCao / 100.0;
        double bmi = canNang / (chieuCaoMet * chieuCaoMet);
        return Math.round(bmi * 100.0) / 100.0;
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

    /**
     * Ưu tiên giá trị chuẩn đoán ICD cho placeholder {@code ${ChiTietDieuTri.chuanDoan}} trong báo cáo.
     * Nếu có danh sách ICD (mã + tên) → fill chuỗi "{Mã ICD} - {Tên bệnh}" ghép bằng ", ".
     * Nếu không có ICD → fallback sang trường chuẩn đoán thường.
     */
    private String resolveChuanDoanForReport(ChiTietDieuTri ctdt) {
        if (ctdt == null) {
            return "";
        }
        String icdText = ctdt.getDsChanDoanIcdText();
        if (icdText != null && !icdText.isBlank()) {
            return icdText;
        }
        return safeText(ctdt.getChuanDoan());
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

    /**
     * Tính ngày bắt đầu cho sổ bệnh án theo đúng logic ở tab tổng kết điều trị
     * (xem {@code ChiTietDieuTriSBADetailView.handleTongKetTabSelected} +
     * {@code resolveDateRangeFromToDieuTri}).
     * <p>
     * Quy tắc:
     * <ul>
     *     <li>Nếu {@code ctdt.ngayBatDau} đã có → giữ nguyên.</li>
     *     <li>Nếu trống → lấy {@code min(tuNgay)} của các {@code ToDieuTri}
     *         thuộc phiếu (ưu tiên dòng có ngày sớm nhất).</li>
     *     <li>Nếu không có {@code ToDieuTri} nào hoặc không có ngày hợp lệ
     *         → trả về {@code null} (giữ chỗ trống).</li>
     * </ul>
     */
    private Date resolveSoBenhAnDateStart(ChiTietDieuTri ctdt) {
        if (ctdt == null) {
            return null;
        }
        if (ctdt.getNgayBatDau() != null) {
            return ctdt.getNgayBatDau();
        }
        Date fromToDieuTri = resolveMinTuNgayFromToDieuTri(ctdt);
        return fromToDieuTri;
    }

    /**
     * Tính ngày kết thúc cho sổ bệnh án theo đúng logic ở tab tổng kết điều trị.
     * <ul>
     *     <li>Nếu {@code ctdt.ngayKetThuc} đã có → giữ nguyên.</li>
     *     <li>Nếu trống → lấy {@code max(denNgay)} của các {@code ToDieuTri}.</li>
     *     <li>Nếu không có dữ liệu → trả về {@code null}.</li>
     * </ul>
     */
    private Date resolveSoBenhAnDateEnd(ChiTietDieuTri ctdt) {
        if (ctdt == null) {
            return null;
        }
        if (ctdt.getNgayKetThuc() != null) {
            return ctdt.getNgayKetThuc();
        }
        Date fromToDieuTri = resolveMaxDenNgayFromToDieuTri(ctdt);
        return fromToDieuTri;
    }

    private Date resolveMinTuNgayFromToDieuTri(ChiTietDieuTri chiTietDieuTri) {
        if (chiTietDieuTri == null || chiTietDieuTri.getId() == null) {
            return null;
        }
        List<ToDieuTri> lines = dataManager.load(ToDieuTri.class)
                .query("select e from ToDieuTri e where e.chiTietDieuTri = :ctdt order by e.tuNgay asc, e.id asc")
                .parameter("ctdt", chiTietDieuTri)
                .list();
        Date start = null;
        for (ToDieuTri line : lines) {
            if (line.getTuNgay() != null && (start == null || line.getTuNgay().before(start))) {
                start = line.getTuNgay();
            }
        }
        return start;
    }

    private Date resolveMaxDenNgayFromToDieuTri(ChiTietDieuTri chiTietDieuTri) {
        if (chiTietDieuTri == null || chiTietDieuTri.getId() == null) {
            return null;
        }
        List<ToDieuTri> lines = dataManager.load(ToDieuTri.class)
                .query("select e from ToDieuTri e where e.chiTietDieuTri = :ctdt order by e.tuNgay asc, e.id asc")
                .parameter("ctdt", chiTietDieuTri)
                .list();
        Date end = null;
        for (ToDieuTri line : lines) {
            if (line.getDenNgay() != null && (end == null || line.getDenNgay().after(end))) {
                end = line.getDenNgay();
            }
        }
        return end;
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