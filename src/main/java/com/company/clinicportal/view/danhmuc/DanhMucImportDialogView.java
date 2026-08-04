package com.company.clinicportal.view.danhmuc;

import com.company.clinicportal.entity.DmThuoc;
import com.company.clinicportal.entity.Icd10;
import com.company.clinicportal.service.CatalogTemplateService;
import com.company.clinicportal.service.DmThuocImportService;
import com.company.clinicportal.service.Icd10ImportService;
import com.company.clinicportal.service.ImportPreviewRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Dialog import chung cho DmThuoc và Icd10.
 * Loại import được truyền qua param {@code kind} = "thuoc" | "icd".
 */
@ViewController("ltcs_DanhMucImportDialog")
@ViewDescriptor("danh-muc-import-dialog-view.xml")
@DialogMode(width = "80em", height = "60em", resizable = true)
public class DanhMucImportDialogView extends StandardView {

    public static final String KIND_THUOC = "thuoc";
    public static final String KIND_ICD = "icd";

    @ViewComponent
    private Upload upload;
    @ViewComponent
    private JmixButton importButton;
    @ViewComponent
    private JmixButton downloadTemplateButton;
    @ViewComponent
    private DataGrid<ImportPreviewRow> previewDataGrid;
    @ViewComponent
    private CollectionContainer<ImportPreviewRow> previewDc;
    @ViewComponent
    private Span dialogTitle;

    @Autowired
    private DmThuocImportService dmThuocImportService;
    @Autowired
    private Icd10ImportService icd10ImportService;
    @Autowired
    private com.company.clinicportal.service.CatalogTemplateService catalogTemplateService;
    @Autowired
    private Notifications notifications;
    @Autowired
    private UiComponents uiComponents;

    private MemoryBuffer memoryBuffer;
    private byte[] fileBytes;
    private boolean fileUploaded;

    private String kind = KIND_THUOC;

    public void setKind(String kind) {
        this.kind = kind == null ? KIND_THUOC : kind;
    }

    @Subscribe
    public void onInit(InitEvent event) {
        memoryBuffer = new MemoryBuffer();
        upload.setReceiver(memoryBuffer);
        upload.setAcceptedFileTypes(".xlsx");
        upload.setMaxFileSize(10 * 1024 * 1024);

        dialogTitle.setText(KIND_ICD.equals(kind) ? "Import danh mục ICD-10" : "Import danh mục thuốc");

        upload.addSucceededListener(e -> {
            fileUploaded = true;
            try {
                InputStream is = memoryBuffer.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n;
                while ((n = is.read(buf)) > -1) baos.write(buf, 0, n);
                fileBytes = baos.toByteArray();

                ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
                List<ImportPreviewRow> items = KIND_ICD.equals(kind)
                        ? icd10ImportService.parsePreview(bais)
                        : dmThuocImportService.parsePreview(bais);

                List<ImportPreviewRow> valid = items.stream()
                        .filter(ImportPreviewRow::getOk)
                        .toList();
                previewDc.setItems(valid);
                importButton.setEnabled(!valid.isEmpty());
                if (valid.isEmpty()) {
                    notifications.create("Không có dòng hợp lệ để import.")
                            .withType(Notifications.Type.WARNING).show();
                }
            } catch (Exception ex) {
                notifications.create("Lỗi khi đọc file: " + ex.getMessage())
                        .withType(Notifications.Type.ERROR).show();
                fileUploaded = false;
            }
        });

        importButton.setEnabled(false);
        setupColumns();
    }

    private void setupColumns() {
        previewDataGrid.addColumn(ImportPreviewRow::getRowNumber)
                .setHeader("Dòng").setAutoWidth(true);
        previewDataGrid.addColumn(ImportPreviewRow::getCode)
                .setHeader("Mã").setAutoWidth(true);
        previewDataGrid.addColumn(new ComponentRenderer<>(item -> {
            Span s = uiComponents.create(Span.class);
            if (Boolean.TRUE.equals(item.getOk())) {
                s.setText("✓");
                s.getStyle().set("color", "var(--lumo-success-color)");
            } else {
                s.setText(item.getMessage());
                s.getStyle().set("color", "var(--lumo-error-color)");
            }
            return s;
        })).setHeader("Trạng thái").setAutoWidth(true);
    }

    @Subscribe("importButton")
    public void onImportButtonClick(com.vaadin.flow.component.ClickEvent<JmixButton> e) {
        if (!fileUploaded || fileBytes == null) {
            notifications.create("Vui lòng tải file Excel trước.")
                    .withType(Notifications.Type.WARNING).show();
            return;
        }
        try {
            InputStream is = new ByteArrayInputStream(fileBytes);
            DmThuocImportService.ImportSummary summary = KIND_ICD.equals(kind)
                    ? icd10ImportService.importFromExcel(is, null)
                    : dmThuocImportService.importFromExcel(is, null);

            notifications.create(String.format("Import xong: %d dòng thành công, %d lỗi.",
                            summary.getInserted(), summary.getErrors().size()))
                    .withType(Notifications.Type.SUCCESS).show();
            close(StandardOutcome.CLOSE);
        } catch (Exception ex) {
            notifications.create("Lỗi khi import: " + ex.getMessage())
                    .withType(Notifications.Type.ERROR).show();
        }
    }

    @Subscribe("cancelButton")
    public void onCancelButtonClick(com.vaadin.flow.component.ClickEvent<JmixButton> e) {
        close(StandardOutcome.CLOSE);
    }

    @Subscribe("downloadTemplateButton")
    public void onDownloadTemplateClick(com.vaadin.flow.component.ClickEvent<JmixButton> e) {
        try {
            String templateKind = KIND_ICD.equals(kind)
                    ? CatalogTemplateService.TEMPLATE_ICD10
                    : CatalogTemplateService.TEMPLATE_DM_THUOC;
            byte[] bytes = catalogTemplateService.buildTemplate(templateKind);
            String fileName = KIND_ICD.equals(kind)
                    ? "template-icd10.xlsx"
                    : "template-dm-thuoc.xlsx";
            com.vaadin.flow.server.StreamResource resource = new com.vaadin.flow.server.StreamResource(
                    fileName, () -> new java.io.ByteArrayInputStream(bytes));
            resource.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            com.vaadin.flow.component.html.Anchor a = new com.vaadin.flow.component.html.Anchor(resource, "");
            a.getElement().setAttribute("download", true);
            a.getElement().setAttribute("hidden", true);
            getUI().ifPresent(ui -> ui.add(a));
            a.getElement().executeJs("this.click();");
            // Cleanup ngay sau khi tải
            a.addDetachListener(ev -> a.removeFromParent());
        } catch (Exception ex) {
            notifications.create("Lỗi khi tạo template: " + ex.getMessage())
                    .withType(Notifications.Type.ERROR).show();
        }
    }
}
