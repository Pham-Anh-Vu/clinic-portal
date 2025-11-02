package com.company.clinicportal.view.benhnhan;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.view.chitietdieutri.ChiTietDieuTriListView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;


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

    @Subscribe
    public void onInit(InitEvent event) {
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