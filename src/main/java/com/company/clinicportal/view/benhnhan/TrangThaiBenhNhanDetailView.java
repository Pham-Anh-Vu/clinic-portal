package com.company.clinicportal.view.benhnhan;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "trang-thai-benh-nhans/:id", layout = MainView.class)
@ViewController(id = "TrangThaiBenhNhan.detail")
@ViewDescriptor(path = "trang-thai-benh-nhan-detail-view.xml")
@EditedEntityContainer("benhNhanDc")
@DialogMode(width = "32em")
public class TrangThaiBenhNhanDetailView extends StandardDetailView<BenhNhan> {
}