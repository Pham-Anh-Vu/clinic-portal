package com.company.clinicportal.view.lienthong;

import com.company.clinicportal.lienthong.CoSoKhamChuaBenhLienThongService;
import com.company.clinicportal.lienthong.entity.CoSoKhamChuaBenhLienThong;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "lienthong/cs-kcb/:id", layout = MainView.class)
@ViewController(id = "ltcs_CoSoKhamChuaBenhLienThong.detail")
@ViewDescriptor(path = "co-so-kcb-lien-thong-detail-view.xml")
@EditedEntityContainer("coSoKhamChuaBenhLienThongDc")
public class CoSoKhamChuaBenhLienThongDetailView extends StandardDetailView<CoSoKhamChuaBenhLienThong> {

    @ViewComponent
    private TextField maLienThongField;
    @ViewComponent
    private TextField maCoSoKcbField;
    @ViewComponent
    private TextField tenCoSoField;
    @ViewComponent
    private ComboBox<String> environmentField;
    @ViewComponent
    private PasswordField passwordField;
    @ViewComponent
    private Checkbox activeField;

    @Autowired
    private CoSoKhamChuaBenhLienThongService service;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private Notifications notifications;

    @Subscribe
    public void onInit(InitEvent event) {
        environmentField.setItems(List.of("sandbox", "production"));
    }

    @Subscribe
    public void onInitEntity(InitEntityEvent<CoSoKhamChuaBenhLienThong> event) {
        activeField.setValue(true);
        environmentField.setValue("sandbox");
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        boolean isNew = entityStates.isNew(getEditedEntity());
        maLienThongField.setReadOnly(!isNew);
        // Mật khẩu chỉ nhập khi tạo mới; khi sửa có thể để trống để giữ nguyên giá trị cũ.
        passwordField.setVisible(isNew);
    }

    @Subscribe
    public void onBeforeSave(BeforeSaveEvent event) {
        var entity = getEditedEntity();
        if (passwordField.isVisible() && passwordField.getValue() != null
                && !passwordField.getValue().isBlank()) {
            service.encryptPasswordIfNeeded(entity, passwordField.getValue());
        }
        service.touchTimestamps(entity);
    }

    @Subscribe
    public void onAfterSave(AfterSaveEvent event) {
        notifications.create("Đã lưu cấu hình liên thông.")
                .withPosition(Notification.Position.TOP_END)
                .show();
    }
}
