package com.company.clinicportal.view.benhnhan;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;

@ViewController("SoBenhAnPreviewDialog")
@ViewDescriptor("so-benh-an-preview-dialog-view.xml")
@DialogMode(width = "90%", height = "90%", resizable = true)
public class SoBenhAnPreviewDialogView extends StandardView {

    @ViewComponent
    private Div previewFrame;
    @ViewComponent
    private JmixButton downloadWordButton;

    @Autowired
    private Notifications notifications;

    private byte[] pdfBytes;
    private byte[] wordBytes;
    private String wordFileName;
    private String previewTitle;
    private String previewFileName;

    public void setPreviewData(byte[] pdfBytes, byte[] wordBytes, String wordFileName) {
        this.pdfBytes = pdfBytes;
        this.wordBytes = wordBytes;
        this.wordFileName = wordFileName;
    }

    public void setPreviewTitle(String previewTitle) {
        this.previewTitle = previewTitle;
    }

    /**
     * Tên file hiển thị cho iframe xem trước (chỉ áp dụng cho PDF preview).
     * Mặc định "so-benh-an-preview.pdf" nếu caller không truyền.
     */
    public void setPreviewFileName(String previewFileName) {
        this.previewFileName = previewFileName;
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        // previewTitle có thể được set sau onInit; phần apply title sẽ chạy lại trong onReady.
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            notifications.create("Không có dữ liệu xem trước.")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        if (previewTitle != null && !previewTitle.isBlank()) {
            setPageTitle(previewTitle);
        }

        String iframeFileName = (previewFileName != null && !previewFileName.isBlank())
                ? previewFileName
                : "so-benh-an-preview.pdf";
        StreamResource streamResource = new StreamResource(
                iframeFileName,
                () -> new ByteArrayInputStream(pdfBytes)
        );
        streamResource.setContentType("application/pdf");
        StreamRegistration registration = VaadinSession.getCurrent()
                .getResourceRegistry()
                .registerResource(streamResource);

        Element iframeElement = new Element("iframe");
        iframeElement.setAttribute("src", registration.getResourceUri().toString());
        iframeElement.getStyle().set("width", "100%");
        iframeElement.getStyle().set("height", "100%");
        iframeElement.getStyle().set("border", "none");

        previewFrame.removeAll();
        previewFrame.getElement().appendChild(iframeElement);

        if (wordBytes == null || wordBytes.length == 0) {
            downloadWordButton.setVisible(false);
        } else if (isPdfDownload()) {
            downloadWordButton.setText("Tải PDF");
        }
    }

    @Subscribe("downloadWordButton")
    public void onDownloadWordButtonClick(final com.vaadin.flow.component.ClickEvent<JmixButton> event) {
        if (wordBytes == null || wordBytes.length == 0) {
            notifications.create("Không có file để tải.")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        String fileName = wordFileName != null && !wordFileName.isBlank() ? wordFileName : "so-benh-an.doc";
        StreamResource streamResource = new StreamResource(fileName, () -> new ByteArrayInputStream(wordBytes));
        streamResource.setContentType(isPdfDownload() ? "application/pdf" : "application/msword");
        StreamRegistration registration = VaadinSession.getCurrent()
                .getResourceRegistry()
                .registerResource(streamResource);
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
    }

    @Subscribe("closeButton")
    public void onCloseButtonClick(final com.vaadin.flow.component.ClickEvent<JmixButton> event) {
        close(StandardOutcome.CLOSE);
    }

    private boolean isPdfDownload() {
        return wordFileName != null && wordFileName.toLowerCase().endsWith(".pdf");
    }
}
