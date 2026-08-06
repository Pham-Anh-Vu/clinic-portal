package com.company.clinicportal.view.donthuoc;

import com.company.clinicportal.entity.DonThuocChanDoan;
import com.company.clinicportal.entity.Icd10;
import com.company.clinicportal.service.DonThuocService;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.valuepicker.CustomValueSetEvent;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Modal nhập/sửa 1 dòng chẩn đoán ICD-10 trong đơn thuốc.
 *
 * <p>Mở từ {@link DonThuocQuickAddDialog} qua {@code dialogWindows.detail(...)}
 * với {@code .editEntity(cd)} — entity được Jmix attach vào dataContainer đúng cách.</p>
 */
@Route(value = "don-thuoc-chan-doan-edit", layout = MainView.class)
@ViewController(id = "DonThuocChanDoanEditDialog")
@ViewDescriptor(path = "don-thuoc-chan-doan-edit-dialog.xml")
@EditedEntityContainer("chanDoanDc")
@DialogMode(width = "44em", height = "auto")
public class DonThuocChanDoanEditDialog extends StandardDetailView<DonThuocChanDoan> {

    @Autowired
    private DonThuocService donThuocService;
    @Autowired
    private Notifications notifications;

    @ViewComponent
    private EntityPicker<DonThuocChanDoan> icd10Field;
    @ViewComponent
    private JmixButton saveBtn;
    @ViewComponent
    private TypedTextField<String> maIcdSnapshotField;
    @ViewComponent
    private TypedTextField<String> tenIcdSnapshotField;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        refreshSaveButton();
    }

    @Subscribe("icd10Field")
    public void onIcd10FieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityPicker<Icd10>, Icd10> event) {
        Icd10 icd10 = event.getValue();
        if (icd10 == null) {
            return;
        }
        try {
            // pickDiagnosis: check duplicate + set snapshot fields (maIcd/tenIcd)
            donThuocService.pickDiagnosis(getEditedEntity(), icd10);
            // EntityPicker trong dialog không bind 2 chiều với snapshot fields khi
            // entity set từ service → ép set UI để hiển thị ngay.
            maIcdSnapshotField.setValue(icd10.getMaIcd());
            tenIcdSnapshotField.setValue(icd10.getTenBenh());
        } catch (IllegalStateException ex) {
            notifications.create(ex.getMessage())
                    .withType(Notifications.Type.WARNING).show();
        }
    }

    @Subscribe("icd10Field")
    public void onIcd10FieldCustomValueSet(final CustomValueSetEvent<EntityPicker<Icd10>, Icd10> event) {
        Icd10 icd10 = event.getSource().getValue();
        if (icd10 == null) {
            return;
        }
        try {
            donThuocService.pickDiagnosis(getEditedEntity(), icd10);
            maIcdSnapshotField.setValue(icd10.getMaIcd());
            tenIcdSnapshotField.setValue(icd10.getTenBenh());
        } catch (IllegalStateException ex) {
            notifications.create(ex.getMessage())
                    .withType(Notifications.Type.WARNING).show();
        }
    }

    private void refreshSaveButton() {
//        DonThuocChanDoan cd = getEditedEntity();
//        boolean ok = cd != null && cd.getIcd10() != null;
//        saveBtn.setEnabled(ok);
    }

    @Subscribe
    public void onValidation(ValidationEvent event) {
        DonThuocChanDoan cd = getEditedEntity();
        if (cd == null) return;
        if (cd.getIcd10() == null) {
            event.getErrors().add("Vui lòng chọn mã ICD-10");
        }
    }

    @Subscribe
    public void onAfterSave(AfterSaveEvent event) {
        notifications.create("Đã lưu chẩn đoán")
                .withType(Notifications.Type.SUCCESS).show();
    }
}
