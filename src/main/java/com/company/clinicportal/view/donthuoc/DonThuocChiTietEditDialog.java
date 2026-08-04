package com.company.clinicportal.view.donthuoc;

import com.company.clinicportal.entity.DonThuocChiTiet;
import com.company.clinicportal.entity.DmThuoc;
import com.company.clinicportal.service.DonThuocService;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.AbstractField;
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
 * với {@code .editEntity(ct)} — entity được Jmix attach vào dataContainer đúng cách.</p>
 *
 * <p>Khi user chọn DmThuoc, tự điền các trường snapshot (mã, tên, biệt dược, đơn vị tính).</p>
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
    @ViewComponent
    private TypedTextField<String> maThuocSnapshotField;
    @ViewComponent
    private TypedTextField<String> tenThuocSnapshotField;
    @ViewComponent
    private TypedTextField<String> bietDuocSnapshotField;
    @ViewComponent
    private TypedTextField<String> donViTinhSnapshotField;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        refreshSaveButton();
    }

    /**
     * Khi user chọn thuốc từ danh mục → điền các trường snapshot.
     * Dùng AbstractField.ComponentValueChangeEvent (fired khi user chọn entity).
     */
    @Subscribe("dmThuocField")
    public void onDmThuocFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<EntityPicker<DmThuoc>, DmThuoc> event) {
        DmThuoc dm = event.getValue();
        if (dm == null) {
            maThuocSnapshotField.setValue(null);
            tenThuocSnapshotField.setValue(null);
            bietDuocSnapshotField.setValue(null);
            donViTinhSnapshotField.setValue(null);
            return;
        }
        maThuocSnapshotField.setValue(dm.getMaThuoc());
        tenThuocSnapshotField.setValue(dm.getTenThuoc());
        bietDuocSnapshotField.setValue(dm.getBietDuoc());
        donViTinhSnapshotField.setValue(dm.getDonViTinh());
    }

    @Subscribe("soLuongField")
    public void onSoLuongValueChange(HasValue.ValueChangeEvent<?> e) {
        if (e.isFromClient()) refreshSaveButton();
    }

    private void refreshSaveButton() {
        // Button state managed by action + required fields in XML
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
