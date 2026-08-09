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
    @ViewComponent
    protected TypedTextField<Long> soLuongField;
    @ViewComponent
    protected TypedTextField<Long> khoangCachBuoiDieuTriField;

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

    /**
     * Validate giá trị số buổi / khoảng cách buổi điều trị tại thời điểm save:
     *   - Không cho phép nhập số buổi (soLuong) < 1.
     *   - Không cho phép nhập khoảng cách buổi điều trị (khoangCachBuoiDieuTri) <= 0.
     * Lỗi hiển thị inline dưới field tương ứng, form không bị lưu.
     */
    @Subscribe
    public void onValidation(final ValidationEvent event) {
        ChiTietDichVu entity = getEditedEntity();

        Long soLuong = entity.getSoLuong();
        if (soLuong != null && soLuong < 1) {
            event.getErrors().add(soLuongField,
                    "Số buổi phải lớn hơn 0");
        }

        Long khoangCach = entity.getKhoangCachBuoiDieuTri();
        if (khoangCach != null && khoangCach < 1) {
            event.getErrors().add(khoangCachBuoiDieuTriField,
                    "Khoảng cách buổi điều trị phải lớn hơn 0");
        }
    }
}