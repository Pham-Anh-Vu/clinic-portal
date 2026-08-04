package com.company.clinicportal.view.donthuoc;

import com.company.clinicportal.entity.DonThuocChiTiet;
import com.company.clinicportal.entity.DmThuoc;
import com.company.clinicportal.service.DonThuocService;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

/**
 * Modal nhập/sửa 1 dòng thuốc trong đơn thuốc.
 *
 * <p>Mở từ {@link DonThuocQuickAddDialog} qua {@code dialogWindows.detail(...)}
 * với {@code .editEntity(ct)} — entity được Jmix attach vào dataContainer đúng cách,
 * UI fields (kể cả readonly) refresh tự động khi container.setProperty.</p>
 *
 * <p>Khi user chọn DmThuoc, gọi {@link DonThuocService#pickDrug(InstanceContainer, DmThuoc)}
 * để validate (không trùng) và điền snapshot field qua container.setProperty
 * (không phải plain setter) → readonly fields refresh OK.</p>
 */
@Route(value = "don-thuoc-chi-tiet-edit", layout = MainView.class)
@ViewController(id = "DonThuocChiTietEditDialog")
@ViewDescriptor(path = "don-thuoc-chi-tiet-edit-dialog.xml")
@EditedEntityContainer("chiTietDc")
@DialogMode(width = "44em", height = "auto")
public class DonThuocChiTietEditDialog extends StandardDetailView<DonThuocChiTiet> {

    @Autowired
    private DonThuocService donThuocService;
    @Autowired
    private Notifications notifications;

    @ViewComponent
    private EntityPicker<DonThuocChiTiet> dmThuocField;
    @ViewComponent
    private TypedTextField<BigDecimal> soLuongField;
    @ViewComponent
    private JmixButton saveBtn;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        refreshSaveButton();
    }

    @Subscribe("dmThuocField")
    public void onDmThuocFieldValueChange(HasValue.ValueChangeEvent<DmThuoc> e) {
        if (!e.isFromClient() || e.getValue() == null) {
            refreshSaveButton();
            return;
        }
        @SuppressWarnings("unchecked")
        InstanceContainer<DonThuocChiTiet> container =
                (InstanceContainer<DonThuocChiTiet>) getViewData().getContainer("chiTietDc");
        DonThuocChiTiet ct = container.getItem();
        try {
            donThuocService.pickDrug(ct, e.getValue());
            // Re-fire container event bằng setItem để UI refresh readonly fields
            // (entity là POJO không có PropertyChangeListener).
            container.setItem(ct);
            notifications.create("Đã điền thông tin thuốc")
                    .withType(Notifications.Type.SUCCESS).show();
        } catch (IllegalStateException ex) {
            notifications.create(ex.getMessage())
                    .withType(Notifications.Type.WARNING).show();
            container.setItem(ct);
            ct.setDmThuoc(null);
        }
        refreshSaveButton();
    }

    @Subscribe("soLuongField")
    public void onSoLuongValueChange(HasValue.ValueChangeEvent<?> e) {
        if (e.isFromClient()) refreshSaveButton();
    }

    /** Bật/tắt nút Save dựa trên DmThuoc + số lượng. */
    private void refreshSaveButton() {
        DonThuocChiTiet ct = getEditedEntity();
        boolean ok = ct != null && ct.getDmThuoc() != null && ct.getSoLuong() != null;
        saveBtn.setEnabled(ok);
    }

    /** Validate cuối trước khi Jmix save. */
    @Subscribe
    public void onValidation(ValidationEvent event) {
        DonThuocChiTiet ct = getEditedEntity();
        if (ct == null) return;
        if (ct.getDmThuoc() == null) {
            event.getErrors().add("Vui lòng chọn thuốc");
        }
        if (ct.getSoLuong() == null) {
            event.getErrors().add("Vui lòng nhập số lượng");
        }
    }

    /** Sau khi Jmix save xong, thông báo. */
    @Subscribe
    public void onAfterSave(AfterSaveEvent event) {
        notifications.create("Đã lưu dòng thuốc")
                .withType(Notifications.Type.SUCCESS).show();
    }
}
