package com.company.clinicportal.view.quatrinhhoatdong;

import com.company.clinicportal.entity.QuaTrinhHoatDong;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "quaTrinhHoatDongs/:id", layout = MainView.class)
@ViewController(id = "QuaTrinhHoatDong.detail")
@ViewDescriptor(path = "qua-trinh-hoat-dong-detail-view.xml")
@EditedEntityContainer("quaTrinhHoatDongDc")
public class QuaTrinhHoatDongDetailView extends StandardDetailView<QuaTrinhHoatDong> {
}