package com.company.clinicportal.view.danhmuc;

import com.company.clinicportal.entity.DmThuoc;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "danhmuc/thuoc/:id", layout = MainView.class)
@ViewController(id = "DmThuoc.detail")
@ViewDescriptor(path = "dm-thuoc-detail-view.xml")
@EditedEntityContainer("dmThuocDc")
@DialogMode(width = "80%", height = "100%")
public class DmThuocDetailView extends StandardDetailView<DmThuoc> {
}
