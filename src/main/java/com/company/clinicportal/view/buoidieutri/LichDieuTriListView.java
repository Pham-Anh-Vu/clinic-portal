package com.company.clinicportal.view.buoidieutri;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.Icd10;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Route(value = "lich-dieu-tris", layout = MainView.class)
@ViewController(id = "LichDieuTri.list")
@ViewDescriptor(path = "lich-dieu-tri-list-view.xml")
@LookupComponent("buoiDieuTrisDataGrid")
@DialogMode(width = "64em")
public class LichDieuTriListView extends StandardListView<BuoiDieuTri> {
    private static final String QUERY_TODAY = """
        select distinct e
        from BuoiDieuTri e
        join e.idChiTietDieuTri cdt
        join cdt.toDieuTri tdt
        where e.ngayThucHien = :currentDate
          and tdt.tuNgay <= :currentDate
          and tdt.denNgay >= :currentDate
        order by e.createdAt desc
        """;

    private static final String QUERY_ALL = """
        select e
        from BuoiDieuTri e
        order by e.createdAt desc
        """;
    @ViewComponent
    private DataGrid<BuoiDieuTri> buoiDieuTrisDataGrid;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Messages messages;
    @ViewComponent
    private CollectionLoader<BuoiDieuTri> buoiDieuTrisDl;

    private boolean filterMode = false;


    @Subscribe
    public void onInit(final InitEvent event) {
        buoiDieuTrisDataGrid.addComponentColumn(lh -> {
                    var bn = lh.getIdBenhNhan();
                    if (bn == null) return uiComponents.create(Span.class);

                    Span span = uiComponents.create(Span.class);
                    var trangThai = bn.getTrangThaiKhamBenh();
                    if (trangThai != null) {
                        span.setText(messages.getMessage(trangThai));
                        span.addClassName(trangThai.toString());
                    }

                    return span;
                })
                .setHeader("Trạng thái")
                .setAutoWidth(true);
    }

    @Supply(to = "buoiDieuTrisDataGrid.chuanDoan", subject = "renderer")
    private Renderer<BuoiDieuTri> buoiDieuTrisDataGridChuanDoanRenderer() {
        return new TextRenderer<>(item -> {

            Set<Icd10> dsIcd = item.getIdChiTietDieuTri().getDsChanDoanIcd();

            // Nếu có ICD thì hiển thị danh sách ICD
            if (dsIcd != null && !dsIcd.isEmpty()) {
                return dsIcd.stream()
                        .map(Icd10::getInstanceName)
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.joining(", "));
            }

            // Nếu không có ICD thì dùng chẩn đoán cũ
            return  item.getIdChiTietDieuTri().getChuanDoan() != null
                    ?  item.getIdChiTietDieuTri().getChuanDoan()
                    : "";
        });
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        buoiDieuTrisDl.setQuery(QUERY_TODAY);

        buoiDieuTrisDl.setParameter(
                "currentDate",
                new Date()
        );
        buoiDieuTrisDl.load();
    }
}