package com.company.clinicportal.view.ngaydieutri;

import com.company.clinicportal.entity.NgayDieuTri;
import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
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
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Messages messages;

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
        }).setHeader("Chẩn đoán");

        ngayDieuTrisDataGrid.addColumn(lh -> {
                    var bn = lh.getIdBenhNhan();
                    if (bn == null) return null;
                    if (bn.getNgayKhamBenh() == null) {
                        return "";
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    return sdf.format(bn.getNgayKhamBenh());
                })
                .setHeader("Ngày khám bệnh")
                .setAutoWidth(true);

        ngayDieuTrisDataGrid.addComponentColumn(lh -> {
                    var bn = lh.getIdBenhNhan();
                    if (bn == null) return uiComponents.create(Span.class);

                    Span span = uiComponents.create(Span.class);
                    if (bn.getTrangThaiKhamBenh() != null) {
                        var trangThai = bn.getTrangThaiKhamBenh();
                        span.setText(messages.getMessage(trangThai));
                        span.addClassName(trangThai.toString());
                    }

                    return span;
                })
                .setHeader("Trạng thái")
                .setAutoWidth(true);

        ngayDieuTrisDataGrid.addColumn(lh -> {
                    Date today = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    return sdf.format(today);
                })
                .setHeader("Ngày")
                .setAutoWidth(true);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ngayDieuTrisDl.setParameter("currentDate", new Date());
        ngayDieuTrisDl.load();
    }

}