package com.company.clinicportal.lienthong;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import io.jmix.flowui.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper lấy thông tin đăng nhập bác sĩ liên thông BYT.
 *
 * <p>Theo FSD §VI (API đăng nhập bác sĩ):
 * <pre>
 *   POST /api/auth/dang-nhap-bac-si
 *   { ma_lien_thong_co_so_kham_chua_benh, ma_lien_thong_bac_si, password }
 * </pre></p>
 *
 * <p>Nguồn thông tin (theo yêu cầu):
 * <ul>
 *     <li>{@code ma_lien_thong_bac_si}, {@code password}: lấy từ
 *         {@code application.properties} (key {@code clinicportal.lienthong.bac-si.*}).</li>
 *     <li>{@code ma_lien_thong_co_so_kham_chua_benh}: lấy từ DB bảng
 *         {@code CoSoKhamChuaBenhLienThong} (do caller truyền vào).</li>
 * </ul>
 *
 * <p>Nếu password chưa cấu hình trong properties, sẽ mở dialog để user nhập 1 lần
 * rồi cache trong-memory theo session. Không persist password để tránh lộ.</p>
 */
@Component
public class LienThongPasswordPrompt {

    private static final Logger log = LoggerFactory.getLogger(LienThongPasswordPrompt.class);

    private final LienThongProperties properties;

    /** Cache password user-nhập-tay theo maLienThongBacSi trong session hiện tại. */
    private final ConcurrentHashMap<String, String> runtimeCache = new ConcurrentHashMap<>();

    public LienThongPasswordPrompt(LienThongProperties properties) {
        this.properties = properties;
    }

    /**
     * Trả về (maLienThongBacSi, password) để gọi API đăng nhập bác sĩ.
     *
     * @return pair chứa mã + password; {@code null} nếu không lấy được và user huỷ dialog.
     */
    public BacSiCredentials resolveBacSiCredentials(Notifications notifications) {
        String ma = properties.getBacSi() == null ? null
                : properties.getBacSi().getMaLienThongBacSi();
        String pw = properties.getBacSi() == null ? null
                : properties.getBacSi().getPassword();

        boolean maFromProps = ma != null && !ma.isBlank();
        boolean pwFromProps = pw != null && !pw.isBlank();

        if (maFromProps && pwFromProps) {
            return new BacSiCredentials(ma, pw);
        }

        // Thiếu mã → báo lỗi luôn, không cho nhập tay (vì mã là định danh)
        if (!maFromProps) {
            if (notifications != null) {
                notifications.create("Chưa cấu hình ma_lien_thong_bac_si trong application.properties.")
                        .withType(Notifications.Type.ERROR).show();
            }
            log.error("Thiếu clinicportal.lienthong.bac-si.ma-lien-thong-bac-si trong application.properties.");
            return null;
        }

        // Có mã từ properties nhưng thiếu password → thử cache runtime
        String cachedPw = runtimeCache.get(ma);
        if (cachedPw != null && !cachedPw.isBlank()) {
            return new BacSiCredentials(ma, cachedPw);
        }

        // Mở dialog nhập password (lần đầu)
        String prompted = openPromptDialog(ma, notifications);
        if (prompted == null) return null;
        runtimeCache.put(ma, prompted);
        return new BacSiCredentials(ma, prompted);
    }

    /** Xoá cache runtime. */
    public void clearRuntimeCache() {
        runtimeCache.clear();
    }

    public void invalidateRuntime(String maLienThongBacSi) {
        if (maLienThongBacSi != null) runtimeCache.remove(maLienThongBacSi);
    }

    private String openPromptDialog(String maLienThongBacSi, Notifications notifications) {
        UI ui = UI.getCurrent();
        if (ui == null) {
            log.error("Không có UI hiện tại, không thể mở dialog password liên thông.");
            return null;
        }

        final String[] result = { null };

        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setDraggable(false);
        dialog.setHeaderTitle("Mật khẩu liên thông BYT");
        dialog.setWidth("420px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        H4 title = new H4("Đăng nhập bác sĩ - donthuocquocgia.vn");
        title.getStyle().set("margin", "0");

        Span info = new Span("Bác sĩ: " + maLienThongBacSi);
        info.getStyle().set("color", "var(--lumo-secondary-text-color)");
        info.getStyle().set("font-size", "var(--lumo-font-size-s)");

        Span warn = new Span("Token có hiệu lực 7 ngày. Mật khẩu chỉ lưu trong phiên làm việc này.");
        warn.getStyle().set("color", "var(--lumo-warning-text-color)");
        warn.getStyle().set("font-size", "var(--lumo-font-size-xs)");

        PasswordField pwField = new PasswordField("Mật khẩu");
        pwField.setWidthFull();
        pwField.setAutofocus(true);

        Button okBtn = new Button("Đồng bộ", e -> {
            String v = pwField.getValue();
            if (v == null || v.isBlank()) {
                if (notifications != null) {
                    notifications.create("Chưa nhập mật khẩu.")
                            .withType(Notifications.Type.WARNING).show();
                }
                return;
            }
            result[0] = v;
            dialog.close();
        });
        okBtn.addThemeNames("primary");
        okBtn.setDisableOnClick(true);

        Button cancelBtn = new Button("Huỷ", e -> {
            result[0] = null;
            dialog.close();
        });

        HorizontalLayout actions = new HorizontalLayout(cancelBtn, okBtn);
        actions.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        actions.setWidthFull();
        actions.setSpacing(true);

        content.add(title, info, warn, pwField, actions);
        dialog.add(content);

        pwField.addKeyDownListener(com.vaadin.flow.component.Key.ENTER, e -> okBtn.click());

        ui.add(dialog);
        dialog.open();

        return result[0];
    }

    /** Holder trả về cặp (mã, password) bác sĩ. */
    public static class BacSiCredentials {
        public final String maLienThongBacSi;
        public final String password;
        public BacSiCredentials(String ma, String pw) {
            this.maLienThongBacSi = ma;
            this.password = pw;
        }
    }
}
