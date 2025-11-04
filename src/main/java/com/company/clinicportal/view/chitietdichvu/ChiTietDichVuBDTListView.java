package com.company.clinicportal.view.chitietdichvu;

import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

import java.util.Date;


@Route(value = "chi-tiet-dich-vu-bdts", layout = MainView.class)
@ViewController(id = "ChiTietDichVuBDT.list")
@ViewDescriptor(path = "chi-tiet-dich-vu-bdt-list-view.xml")
@LookupComponent("chiTietDichVusDataGrid")
@DialogMode(width = "64em")
public class ChiTietDichVuBDTListView extends StandardListView<ChiTietDichVu> {
    private ChiTietDieuTri idChiTietPhieuDieuTri;

    private Date ngayBatDau;

    public void setIdChiTietPhieuDieuTri(ChiTietDieuTri idChiTietPhieuDieuTri) {
        this.idChiTietPhieuDieuTri = idChiTietPhieuDieuTri;
    }

    public void setNgayBatDau(Date ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }


}