package com.company.clinicportal.view.luongthang;

import com.company.clinicportal.entity.LuongThang;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "luong-thangs", layout = MainView.class)
@ViewController(id = "LuongThang.list")
@ViewDescriptor(path = "luong-thang-list-view.xml")
@LookupComponent("luongThangsDataGrid")
@DialogMode(width = "64em")
public class LuongThangListView extends StandardListView<LuongThang> {
}