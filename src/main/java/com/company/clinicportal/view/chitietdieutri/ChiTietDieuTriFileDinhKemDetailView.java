package com.company.clinicportal.view.chitietdieutri;

import com.company.clinicportal.entity.ChiTietDieuTriFileDinhKem;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "chi-tiet-dieu-tri-file-dinh-kems/:id", layout = MainView.class)
@ViewController(id = "ChiTietDieuTriFileDinhKem.detail")
@ViewDescriptor(path = "chi-tiet-dieu-tri-file-dinh-kem-detail-view.xml")
@EditedEntityContainer("chiTietDieuTriFileDinhKemDc")
@DialogMode(width = "40em", height = "AUTO")
public class ChiTietDieuTriFileDinhKemDetailView extends StandardDetailView<ChiTietDieuTriFileDinhKem> {
}
