package com.company.clinicportal.view.lichhen;

import com.company.clinicportal.entity.LichHen;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.valuepicker.EntityPicker;
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
    @Autowired
    private Messages messages;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if(!entityStates.isNew(getEditedEntity())) benhNhanField.setReadOnly(true);
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


}