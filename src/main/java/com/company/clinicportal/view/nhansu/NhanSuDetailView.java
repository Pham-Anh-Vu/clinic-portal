package com.company.clinicportal.view.nhansu;

import com.company.clinicportal.entity.NhanSu;
import com.company.clinicportal.entity.TinhKpi;
import com.company.clinicportal.entity.TinhKpiChiTiet;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
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
    private CollectionLoader<TinhKpiChiTiet> tinhKpiChiTietsDl;
    @Autowired
    private EntityStates entityStates;
    @ViewComponent
    private VerticalLayout kpiMonth;
    @ViewComponent
    private VerticalLayout kpiDetail;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if(this.isReadOnly()){
            kpiMonth.setVisible(true);
            kpiDetail.setVisible(true);
        }

        tinhKpisDl.setParameter("idNhanSu", getEditedEntity());
        tinhKpisDl.load();

        tinhKpiChiTietsDl.setParameter("idNhanSu", getEditedEntity());
        tinhKpiChiTietsDl.load();
    }
}