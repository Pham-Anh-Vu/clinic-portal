package com.company.clinicportal.view.tblkhachhang;

import com.company.clinicportal.entity.TblKhachhang;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "tbl-khachhangs/:id", layout = MainView.class)
@ViewController(id = "TblKhachhang.detail")
@ViewDescriptor(path = "tbl-khachhang-detail-view.xml")
@EditedEntityContainer("tblKhachhangDc")
public class TblKhachhangDetailView extends StandardDetailView<TblKhachhang> {
}