package com.company.clinicportal.view.lichsuthanhtoan;

import com.company.clinicportal.entity.LichSuThanhToan;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "lich-su-thanh-toans", layout = MainView.class)
@ViewController(id = "LichSuThanhToan.list")
@ViewDescriptor(path = "lich-su-thanh-toan-list-view.xml")
@LookupComponent("lichSuThanhToansDataGrid")
@DialogMode(width = "64em")
public class LichSuThanhToanListView extends StandardListView<LichSuThanhToan> {
}