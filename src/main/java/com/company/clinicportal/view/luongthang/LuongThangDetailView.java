package com.company.clinicportal.view.luongthang;

import com.company.clinicportal.entity.LuongThang;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "luong-thangs/:id", layout = MainView.class)
@ViewController(id = "LuongThang.detail")
@ViewDescriptor(path = "luong-thang-detail-view.xml")
@EditedEntityContainer("luongThangDc")
public class LuongThangDetailView extends StandardDetailView<LuongThang> {
}