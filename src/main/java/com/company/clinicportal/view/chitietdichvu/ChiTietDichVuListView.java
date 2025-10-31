package com.company.clinicportal.view.chitietdichvu;

import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.view.buoidieutri.BuoiDieuTriListView;
import com.company.clinicportal.view.chitietdieutri.ChiTietDieuTriListView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "chi-tiet-dich-vus", layout = MainView.class)
@ViewController(id = "ChiTietDichVu.list")
@ViewDescriptor(path = "chi-tiet-dich-vu-list-view.xml")
@LookupComponent("chiTietDichVusDataGrid")
@DialogMode(width = "64em")
public class ChiTietDichVuListView extends StandardListView<ChiTietDichVu> {
}