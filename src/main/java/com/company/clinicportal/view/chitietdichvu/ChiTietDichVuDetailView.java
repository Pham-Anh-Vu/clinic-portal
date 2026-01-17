package com.company.clinicportal.view.chitietdichvu;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.EntitySet;
import io.jmix.core.SaveContext;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;

@Route(value = "chi-tiet-dich-vus/:id", layout = MainView.class)
@ViewController(id = "ChiTietDichVu.detail")
@ViewDescriptor(path = "chi-tiet-dich-vu-detail-view.xml")
@EditedEntityContainer("chiTietDichVuDc")
public class ChiTietDichVuDetailView extends StandardDetailView<ChiTietDichVu> {
}