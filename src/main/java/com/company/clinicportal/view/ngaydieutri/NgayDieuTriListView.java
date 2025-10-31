package com.company.clinicportal.view.ngaydieutri;

import com.company.clinicportal.entity.NgayDieuTri;
import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Date;


@Route(value = "ngay-dieu-tris", layout = MainView.class)
@ViewController(id = "NgayDieuTri.list")
@ViewDescriptor(path = "ngay-dieu-tri-list-view.xml")
@LookupComponent("ngayDieuTrisDataGrid")
@DialogMode(width = "64em")
public class NgayDieuTriListView extends StandardListView<NgayDieuTri> {
    @ViewComponent
    private CollectionLoader<NgayDieuTri> ngayDieuTrisDl;
    @ViewComponent
    private DataGrid<NgayDieuTri> ngayDieuTrisDataGrid;
    @Autowired
    private DataManager dataManager;

    @Subscribe
    public void onInit(InitEvent event) {
        ngayDieuTrisDataGrid.addColumn(lh -> {
            var bn = lh.getIdBenhNhan();
            if (bn == null) return "";
            var p = dataManager.load(PhieuDieuTri.class)
                    .query("select p from PhieuDieuTri p where p.idBenhNhan = :bn order by p.ngayKham desc")
                    .parameter("bn", bn)
                    .maxResults(1)
                    .optional()
                    .orElse(null);
            return p != null && p.getChiTietDieuTri() != null ? p.getChiTietDieuTri().getChuanDoan() : "";
        }).setHeader("Chuẩn đoán");

        ngayDieuTrisDataGrid.addColumn(lh -> {
            var bn = lh.getIdBenhNhan();
            if (bn == null) return null;
            return dataManager.loadValue(
                    "select p.ngayKham from PhieuDieuTri p where p.idBenhNhan = :bn order by p.ngayKham desc",
                    Date.class
            ).parameter("bn", bn).maxResults(1).optional().orElse(null);
        }).setHeader("Ngày khám");

        ngayDieuTrisDataGrid.addColumn(lh -> {
            var bn = lh.getIdBenhNhan();
            if (bn == null) return "";
            return dataManager.loadValue(
                    "select p.trangThai from PhieuDieuTri p where p.idBenhNhan = :bn order by p.ngayKham desc",
                    String.class
            ).parameter("bn", bn).maxResults(1).optional().orElse("");
        }).setHeader("Trạng thái");
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ngayDieuTrisDl.setParameter("currentDate", new Date());
        ngayDieuTrisDl.load();
    }

}