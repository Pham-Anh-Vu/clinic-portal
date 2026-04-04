package com.company.clinicportal.view.chitietdichvu;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.enumentity.TinhTheoGia;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.EntitySet;
import io.jmix.core.SaveContext;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.TypedTextField;
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
    @ViewComponent
    private TypedTextField<Long> giaLeField;
    @ViewComponent
    private TypedTextField<Long> giaField;

    @Subscribe("tinhTheoGia")
    public void onTinhTheoGiaComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixSelect<TinhTheoGia>, TinhTheoGia> event) {
        if(event.getValue().equals(TinhTheoGia.GOI)) {
            giaLeField.setVisible(false);
            giaField.setVisible(true);
        }
        else {
            giaField.setVisible(false);
            giaLeField.setVisible(true);
        }
    }
}