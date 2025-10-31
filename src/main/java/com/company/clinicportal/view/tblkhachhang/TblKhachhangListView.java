package com.company.clinicportal.view.tblkhachhang;

import com.company.clinicportal.entity.TblKhachhang;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "tbl-khachhangs", layout = MainView.class)
@ViewController(id = "TblKhachhang.list")
@ViewDescriptor(path = "tbl-khachhang-list-view.xml")
@LookupComponent("tblKhachhangsDataGrid")
@DialogMode(width = "64em")
public class TblKhachhangListView extends StandardListView<TblKhachhang> {
}