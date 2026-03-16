package com.company.clinicportal.view.dmdichvu;

import com.company.clinicportal.entity.DmDichVu;
import com.company.clinicportal.enumentity.NhomDichVu;
import com.company.clinicportal.service.DmDichVuImportService;
import com.company.clinicportal.service.DmDichVuPreviewItem;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.Messages;
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
import java.util.stream.Collectors;

@ViewController("DmDichVuImportDialog")
@ViewDescriptor("dm-dich-vu-import-dialog-view.xml")
@DialogMode(width = "80em", height = "60em", resizable = true)
public class DmDichVuImportDialogView extends StandardView {

    @ViewComponent
    private Upload upload;

    @ViewComponent
    private JmixButton importButton;

    @ViewComponent
    private DataGrid<DmDichVuPreviewItem> previewDataGrid;

    @ViewComponent
    private CollectionContainer<DmDichVuPreviewItem> previewDc;

    @Autowired
    private DmDichVuImportService importService;

    @Autowired
    private Notifications notifications;

    @Autowired
    private Messages messages;

    @Autowired
    private UiComponents uiComponents;

    private MemoryBuffer memoryBuffer;
    private boolean fileUploaded = false;
    private List<DmDichVuPreviewItem> previewItems;
    private byte[] fileBytes;

    @Subscribe
    public void onInit(InitEvent event) {
        memoryBuffer = new MemoryBuffer();
        upload.setReceiver(memoryBuffer);
        upload.setAcceptedFileTypes(".xlsx", ".xls");
        upload.setMaxFileSize(10485760); // 10MB
        upload.addClassName("dmdv-inline-upload");
        
        upload.addSucceededListener(e -> {
            fileUploaded = true;
            try {
                // Read file to bytes for later use
                InputStream inputStream = memoryBuffer.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > -1) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
                fileBytes = baos.toByteArray();
                
                // Parse preview
                ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
                List<DmDichVuPreviewItem> rawPreviewItems = importService.parsePreview(bais);
                previewItems = rawPreviewItems.stream()
                        .filter(item -> !item.isHasError())
                        .collect(Collectors.toList());
                previewDc.setItems(previewItems);
                
                // Enable import button if there are valid items
                importButton.setEnabled(!previewItems.isEmpty());
                
            } catch (Exception ex) {
                notifications.create("Lỗi khi đọc file: " + ex.getMessage())
                        .withType(Notifications.Type.ERROR)
                        .show();
                fileUploaded = false;
            }
        });
        
        importButton.setEnabled(false);
        
        // Setup column renderers
        setupColumnRenderers();
    }
    
    private void setupColumnRenderers() {
        // Row number column
        previewDataGrid.addColumn(DmDichVuPreviewItem::getRowNumber)
                .setHeader("Dòng")
                .setAutoWidth(true)
                .setKey("rowNumberColumn");
        
        // Ten dich vu column
        previewDataGrid.addColumn(item -> item.getEntity() != null ? item.getEntity().getTenDichVu() : "")
                .setHeader("Tên dịch vụ")
                .setAutoWidth(true)
                .setKey("tenDichVuColumn");
        
        // Nhom dich vu column
        previewDataGrid.addColumn(item -> {
            if (item.getEntity() != null && item.getEntity().getNhomDichVu() != null) {
                return messages.getMessage(item.getEntity().getNhomDichVu());
            }
            return "";
        })
        .setHeader("Nhóm dịch vụ")
        .setAutoWidth(true)
        .setKey("nhomDichVuColumn");
        
        // Mo ta column
        previewDataGrid.addColumn(item -> item.getEntity() != null ? (item.getEntity().getMoTa() != null ? item.getEntity().getMoTa() : "") : "")
                .setHeader("Mô tả")
                .setAutoWidth(true)
                .setKey("moTaColumn");
        
        // Gia column
        previewDataGrid.addColumn(item -> {
            if (item.getEntity() != null && item.getEntity().getGia() != null) {
                return String.valueOf(item.getEntity().getGia());
            }
            return "";
        })
        .setHeader("Giá")
        .setAutoWidth(true)
        .setKey("giaColumn");
        
        // Error column with color
        previewDataGrid.addColumn(new ComponentRenderer<>(item -> {
            Span span = uiComponents.create(Span.class);
            if (item.isHasError()) {
                span.setText(item.getErrorMessage());
                span.getStyle().set("color", "var(--lumo-error-color)");
            } else {
                span.setText("✓");
                span.getStyle().set("color", "var(--lumo-success-color)");
            }
            return span;
        }))
        .setHeader("Trạng thái")
        .setAutoWidth(true)
        .setKey("errorColumn");
    }

    @Subscribe("importButton")
    public void onImportButtonClick(final com.vaadin.flow.component.ClickEvent<JmixButton> event) {
        if (!fileUploaded || fileBytes == null) {
            notifications.create("Vui lòng chọn và tải lên file Excel để import")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        try {
            InputStream inputStream = new ByteArrayInputStream(fileBytes);
            DmDichVuImportService.ImportResult result = importService.importFromExcel(inputStream);

            notifications.create(String.format("Import thành công! Đã import %d bản ghi.", result.getSuccessCount()))
                    .withThemeVariant(NotificationVariant.LUMO_SUCCESS)
                    .withPosition(Notification.Position.TOP_END)
                    .show();

            // Close dialog and refresh parent view
            close(StandardOutcome.CLOSE);

        } catch (Exception e) {
            notifications.create("Lỗi khi import file: " + e.getMessage())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    @Subscribe("cancelButton")
    public void onCancelButtonClick(final com.vaadin.flow.component.ClickEvent<JmixButton> event) {
        close(StandardOutcome.CLOSE);
    }

    @Subscribe("downloadTemplateButton")
    public void onDownloadTemplateButtonClick(final com.vaadin.flow.component.ClickEvent<JmixButton> event) {
        try (InputStream templateInputStream = getClass().getResourceAsStream("/reports/dmDichVu.xlsx")) {
            if (templateInputStream == null) {
                notifications.create("Không tìm thấy file mẫu dmDichVu.xlsx")
                        .withType(Notifications.Type.ERROR)
                        .show();
                return;
            }

            byte[] templateBytes = templateInputStream.readAllBytes();
            StreamResource streamResource = new StreamResource("dmDichVu.xlsx",
                    () -> new ByteArrayInputStream(templateBytes));
            StreamRegistration registration = VaadinSession.getCurrent()
                    .getResourceRegistry()
                    .registerResource(streamResource);
            UI.getCurrent().getPage().open(registration.getResourceUri().toString());
        } catch (Exception ex) {
            notifications.create("Không thể tải file mẫu: " + ex.getMessage())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }
}

