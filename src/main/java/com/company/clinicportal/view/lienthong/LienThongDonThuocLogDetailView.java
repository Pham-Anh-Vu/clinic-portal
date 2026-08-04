package com.company.clinicportal.view.lienthong;

import com.company.clinicportal.lienthong.entity.LienThongDonThuocLog;
import com.company.clinicportal.view.main.MainView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Route(value = "lienthong/don-thuoc-log/:id", layout = MainView.class)
@ViewController(id = "ltcs_LienThongDonThuocLog.detail")
@ViewDescriptor(path = "lien-thong-don-thuoc-log-detail-view.xml")
@EditedEntityContainer("lienThongDonThuocLogDc")
public class LienThongDonThuocLogDetailView extends StandardDetailView<LienThongDonThuocLog> {

    @Autowired
    @Qualifier("lienThongObjectMapper")
    private ObjectMapper objectMapper;

    @ViewComponent
    private CodeEditor requestEditor;
    @ViewComponent
    private CodeEditor responseEditor;
    @ViewComponent
    private Html summary;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        LienThongDonThuocLog log = getEditedEntity();
        if (log == null) return;
        if (summary != null) {
            String masked = log.getMaDonThuoc() == null ? "(?)" : log.getMaDonThuoc();
            String html = "<div style='padding:0.5em;background:var(--lumo-contrast-5pct);border-radius:6px'>"
                    + "API: <b>" + safe(log.getApi()) + "</b>"
                    + " • Mã đơn: <b>" + safe(masked) + "</b>"
                    + " • Status: <b>" + (log.getHttpStatus() == null ? "?" : log.getHttpStatus()) + "</b>"
                    + " • Outcome: <b>" + safe(log.getOutcome()) + "</b>"
                    + " • Retryable: <b>" + (Boolean.TRUE.equals(log.getRetryable()) ? "yes" : "no") + "</b>"
                    + "</div>";
            summary.getElement().setProperty("innerHTML", html);
        }
        if (requestEditor != null) requestEditor.setValue(pretty(log.getRequestMasked()));
        if (responseEditor != null) responseEditor.setValue(pretty(log.getResponseMasked()));
    }

    private String pretty(String raw) {
        if (raw == null || raw.isBlank()) return "(empty)";
        try {
            Object o = objectMapper.readValue(raw, Object.class);
            return objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return raw;
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}