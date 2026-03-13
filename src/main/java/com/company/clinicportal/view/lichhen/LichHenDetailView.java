package com.company.clinicportal.view.lichhen;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.LichHen;
import com.company.clinicportal.entity.NhanSu;
import com.company.clinicportal.enumentity.ChucVu;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "lich-hens/:id", layout = MainView.class)
@ViewController(id = "LichHen.detail")
@ViewDescriptor(path = "lich-hen-detail-view.xml")
@EditedEntityContainer("lichHenDc")
@DialogMode(height = "100%", width = "80%")
public class LichHenDetailView extends StandardDetailView<LichHen> {
    @Autowired
    private EntityStates entityStates;
    @ViewComponent
    private EntityPicker<Object> benhNhanField;
    @ViewComponent
    private TypedTextField<String> tuoiField;
    @ViewComponent
    private CollectionLoader<NhanSu> nhanSusDl;
    @Autowired
    private Messages messages;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        nhanSusDl.setParameter("chucVu", ChucVu.BS.getId());
        nhanSusDl.load();

        if (entityStates.isNew(getEditedEntity())
                && getEditedEntity().getNhuCauKhachHang() == null) {
            getEditedEntity().setNhuCauKhachHang("Thăm khám và tư vấn");
        }

        if(!entityStates.isNew(getEditedEntity())) benhNhanField.setReadOnly(true);
        updateTuoiField();
    }

    @Autowired
    private UiComponents uiComponents;

    @Supply(to = "gioiTinhField", subject = "renderer")
    private ComponentRenderer gioiTinhFieldRenderer() {
        return new ComponentRenderer<>(obj -> {
            // TODO: create suitable component
            Span span = uiComponents.create(Span.class);
            span.setText(obj.toString());
            span.addClassName("gioi_tinh");
            return span;
        });
    }

    @Subscribe("benhNhanField")
    public void onBenhNhanFieldValueChange(final HasValue.ValueChangeEvent<Object> event) {
        updateTuoiField();
    }

    private void updateTuoiField() {
        BenhNhan benhNhan = getEditedEntity().getIdBenhNhan();
        if(benhNhan != null) tuoiField.setValue(BenhNhan.calculateTuoi(benhNhan != null ? benhNhan.getNgaySinh() : null));
    }


}