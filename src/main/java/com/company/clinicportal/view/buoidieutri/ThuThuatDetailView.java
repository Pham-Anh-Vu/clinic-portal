package com.company.clinicportal.view.buoidieutri;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.NhanSu;
import com.company.clinicportal.enumentity.ChucVu;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.*;

@Route(value = "thu-thuats/:id", layout = MainView.class)
@ViewController(id = "ThuThuatdetail")
@ViewDescriptor(path = "thu-thuat-detail-view.xml")
@EditedEntityContainer("buoiDieuTriDc")
@DialogMode(width = "80%", height = "100%")
public class ThuThuatDetailView extends StandardDetailView<BuoiDieuTri> {
    @ViewComponent
    private CollectionLoader<NhanSu> nhanSusDl;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        nhanSusDl.setParameter("chucVu", ChucVu.KTV.getId());
        nhanSusDl.load();
    }
}