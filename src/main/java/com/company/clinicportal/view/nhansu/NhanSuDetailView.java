package com.company.clinicportal.view.nhansu;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.NhanSu;
import com.company.clinicportal.entity.TinhKpi;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "nhan-sus/:id", layout = MainView.class)
@ViewController(id = "NhanSu.detail")
@ViewDescriptor(path = "nhan-su-detail-view.xml")
@EditedEntityContainer("nhanSuDc")
@DialogMode(height = "100%", width = "80%")
public class NhanSuDetailView extends StandardDetailView<NhanSu> {
    @ViewComponent
    private CollectionLoader<TinhKpi> tinhKpisDl;
    @ViewComponent
    private CollectionLoader<BuoiDieuTri> buoiDieuTrisDl;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private UiComponents uiComponents;
    @ViewComponent
    private VerticalLayout kpiMonth;
    @ViewComponent
    private VerticalLayout kpiDetail;
    @Autowired
    private Messages messages;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if(this.isReadOnly()){
            kpiMonth.setVisible(true);
            kpiDetail.setVisible(true);
        }

        tinhKpisDl.setParameter("idNhanSu", getEditedEntity());
        tinhKpisDl.load();

        buoiDieuTrisDl.setParameter("idNhanSuStaging", getEditedEntity());
        buoiDieuTrisDl.load();
    }

    @Supply(to = "buoiDieuTrisDataGrid.trangThai", subject = "renderer")
    private Renderer<BuoiDieuTri> buoiDieuTrisDataGridTrangThaiRenderer() {
        return new ComponentRenderer<>(buoidieutri -> {
            // TODO: create suitable component
            Span span = uiComponents.create(Span.class);
            if (buoidieutri.getTrangThai()!=null) {
                span.setText(messages.getMessage(buoidieutri.getTrangThai()));
                span.addClassName(buoidieutri.getTrangThai().getId());
            }
            return span;
        });
    }
}