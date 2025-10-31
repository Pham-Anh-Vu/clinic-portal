package com.company.clinicportal.view.nhansu;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.NhanSu;
import com.company.clinicportal.entity.TinhKpi;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;

@Route(value = "nhan-sus/:id", layout = MainView.class)
@ViewController(id = "NhanSu.detail")
@ViewDescriptor(path = "nhan-su-detail-view.xml")
@EditedEntityContainer("nhanSuDc")
public class NhanSuDetailView extends StandardDetailView<NhanSu> {
    @ViewComponent
    private CollectionLoader<TinhKpi> tinhKpisDl;
    @ViewComponent
    private CollectionLoader<BuoiDieuTri> buoiDieuTrisDl;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        tinhKpisDl.setParameter("idNhanSu", getEditedEntity());
        tinhKpisDl.load();

        buoiDieuTrisDl.setParameter("idNhanSuStaging", getEditedEntity());
        buoiDieuTrisDl.load();
    }

}