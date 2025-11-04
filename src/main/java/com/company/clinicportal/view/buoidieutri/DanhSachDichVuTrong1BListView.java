package com.company.clinicportal.view.buoidieutri;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;


@Route(value = "danh-sach-dich-vu-trong-1bs", layout = MainView.class)
@ViewController(id = "DanhSachDichVuTrong1B.list")
@ViewDescriptor(path = "danh-sach-dich-vu-trong-1b-list-view.xml")
@LookupComponent("buoiDieuTrisDataGrid")
@DialogMode(width = "64em")
public class DanhSachDichVuTrong1BListView extends StandardListView<BuoiDieuTri> {
    private Date ngayThucHien;

    private BenhNhan idBenhNhan;
    @ViewComponent
    private CollectionLoader<BuoiDieuTri> buoiDieuTrisDl;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Messages messages;

    public void setNgayThucHien(Date ngayThucHien) {
        this.ngayThucHien = ngayThucHien;
    }

    public void setIdBenhNhan(BenhNhan idBenhNhan) {
        this.idBenhNhan = idBenhNhan;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        buoiDieuTrisDl.setParameter("ngayThucHien", ngayThucHien);
        buoiDieuTrisDl.setParameter("idBenhNhan", idBenhNhan);
        buoiDieuTrisDl.load();
    }

    @Supply(to = "buoiDieuTrisDataGrid.trangThai", subject = "renderer")
    private Renderer<BuoiDieuTri> buoiDieuTrisDataGridTrangThaiRenderer() {
        return new ComponentRenderer<>(buoidieutri -> {
            // TODO: create suitable component
            Span span = uiComponents.create(Span.class);
            if (buoidieutri.getTrangThai()!=null) {
                span.setText(messages.getMessage(buoidieutri.getTrangThai()));
                span.addClassName(buoidieutri.getTrangThai().getId());
            }
            return span;
        });
    }
}