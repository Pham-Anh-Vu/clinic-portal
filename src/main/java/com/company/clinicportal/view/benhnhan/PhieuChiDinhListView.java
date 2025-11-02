package com.company.clinicportal.view.benhnhan;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.LichHen;
import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.view.chitietdieutri.ChiTietDieuTriListView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;


@Route(value = "phieu-chi-dinh", layout = MainView.class)
@ViewController(id = "PhieuChiDinh.list")
@ViewDescriptor(path = "phieu-chi-dinh-list-view.xml")
@LookupComponent("benhNhansDataGrid")
@DialogMode(width = "64em")
public class PhieuChiDinhListView extends StandardListView<BenhNhan> {
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private ViewNavigators viewNavigators;
    @ViewComponent
    private DataGrid<BenhNhan> benhNhansDataGrid;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Messages messages;

    @Subscribe
    public void onInit(InitEvent event) {
        benhNhansDataGrid.addColumn(lh -> {
                    if (lh == null) return uiComponents.create(Span.class);

                    Date ngayKham = dataManager.loadValue(
                                    "select p.ngayHen from LichHen p where p.idBenhNhan = :bn order by p.createdAt desc",
                                    Date.class
                            ).parameter("bn", lh)
                            .maxResults(1)
                            .optional()
                            .orElse(null);

                    if (ngayKham == null) {
                        return "";
                    }

                    // Format về dd/MM/yyyy
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    return sdf.format(ngayKham);
                })
                .setHeader("Ngày hẹn khám")
                .setAutoWidth(true);

        benhNhansDataGrid.addComponentColumn(lh -> {
                    if (lh == null) return uiComponents.create(Span.class);

                    Optional<PhieuDieuTri> phieuDieuTri = dataManager.load(PhieuDieuTri.class)
                            .query("select p from PhieuDieuTri p where p.idBenhNhan = :bn order by p.ngayKham desc")
                            .parameter("bn", lh)
                            .maxResults(1)
                            .optional();

                    Span span = uiComponents.create(Span.class);

                    if (phieuDieuTri.isPresent() && phieuDieuTri.get().getTrangThai() != null) {
                        var trangThai = phieuDieuTri.get().getTrangThai(); // Enum hoặc String
                        span.setText(messages.getMessage(trangThai));
                        span.addClassName(trangThai.toString()); // Gán class CSS nếu muốn
                    }

                    return span;
                })
                .setHeader("Trạng thái")
                .setAutoWidth(true);

        benhNhansDataGrid.addComponentColumn(benhNhan -> {
            JmixButton button = uiComponents.create(JmixButton.class);
            button.setText("Chi tiết");
            button.addClickListener(e -> {
                if (benhNhan != null && benhNhan.getId() != null) {
                    DialogWindow<ChiTietDieuTriListView> windows = dialogWindows.view(this, ChiTietDieuTriListView.class).build();
                    windows.getView().setIdBenhNhan(benhNhan.getId());
                    windows.setHeight("100%");
                    windows.setWidth("80%");
                    windows.open();
                }
            });
            return button;
        }).setHeader("Thao tác").setAutoWidth(true);
    }
}