package com.company.clinicportal.view.dmdichvu;

import com.company.clinicportal.entity.DmDichVu;
import com.company.clinicportal.entity.GiaKpi;
import com.company.clinicportal.enumentity.LoaiGiaKPI;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Route(value = "dm-dich-vus/:id", layout = MainView.class)
@ViewController(id = "DmDichVu.detail")
@ViewDescriptor(path = "dm-dich-vu-detail-view.xml")
@EditedEntityContainer("dmDichVuDc")
@DialogMode(width = "80%", height = "100%")
public class DmDichVuDetailView extends StandardDetailView<DmDichVu> {
    @Autowired
    private DataManager dataManager;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        Map<LoaiGiaKPI, GiaKpi> giaKpiMap = dataManager.load(GiaKpi.class)
                .query("select g from GiaKpi g where g.loai in :loai")
                .parameter("loai", List.of("sang", "toi"))
                .list()
                .stream()
                .collect(Collectors.toMap(GiaKpi::getLoai, Function.identity()));

        GiaKpi giaKpiSang = giaKpiMap.get(LoaiGiaKPI.SANG);
        GiaKpi giaKpiToi = giaKpiMap.get(LoaiGiaKPI.TOI);

        if (giaKpiSang != null) {
            getEditedEntity().setGiaKpiSang(giaKpiSang.getGia());
        }
        if (giaKpiToi != null) {
            getEditedEntity().setGiaKpiToi(giaKpiToi.getGia());
        }
    }
}