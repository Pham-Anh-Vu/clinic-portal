package com.company.clinicportal.view.buoidieutri;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Optional;


@Route(value = "lich-dieu-tris", layout = MainView.class)
@ViewController(id = "LichDieuTri.list")
@ViewDescriptor(path = "lich-dieu-tri-list-view.xml")
@LookupComponent("buoiDieuTrisDataGrid")
@DialogMode(width = "64em")
public class LichDieuTriListView extends StandardListView<BuoiDieuTri> {
    @ViewComponent
    private DataGrid<BuoiDieuTri> buoiDieuTrisDataGrid;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Messages messages;
    @ViewComponent
    private CollectionLoader<BuoiDieuTri> buoiDieuTrisDl;

    @Subscribe
    public void onInit(final InitEvent event) {
        buoiDieuTrisDataGrid.addComponentColumn(lh -> {
                    var bn = lh.getIdBenhNhan();
                    if (bn == null) return uiComponents.create(Span.class);

                    Optional<PhieuDieuTri> phieuDieuTri = dataManager.load(PhieuDieuTri.class)
                            .query("select p from PhieuDieuTri p where p.idBenhNhan = :bn order by p.ngayKham desc")
                            .parameter("bn", bn)
                            .maxResults(1)
                            .optional();

                    Span span = uiComponents.create(Span.class);

                    if (phieuDieuTri.isPresent()) {
                        var trangThai = phieuDieuTri.get().getTrangThai(); // Enum hoặc String
                        if(trangThai != null){
                            span.setText(messages.getMessage(trangThai));
                            span.addClassName(trangThai.toString()); // Gán class CSS nếu muốn
                        }
                    }

                    return span;
                })
                .setHeader("Trạng thái")
                .setAutoWidth(true);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        buoiDieuTrisDl.setParameter("currentDate", new Date());
        buoiDieuTrisDl.load();
    }
}