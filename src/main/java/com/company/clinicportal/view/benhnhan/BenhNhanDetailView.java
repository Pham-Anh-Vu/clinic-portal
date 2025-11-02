package com.company.clinicportal.view.benhnhan;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Optional;

@Route(value = "benh-nhans/:id", layout = MainView.class)
@ViewController(id = "BenhNhan.detail")
@ViewDescriptor(path = "benh-nhan-detail-view.xml")
@EditedEntityContainer("benhNhanDc")
@DialogMode(width = "80%", height = "100%")
public class BenhNhanDetailView extends StandardDetailView<BenhNhan> {
    @Autowired
    private DataManager dataManager;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Messages messages;
    @ViewComponent
    private HorizontalLayout trangThaiBox;

    @ViewComponent
    private VerticalLayout formCreate;
    @ViewComponent
    private VerticalLayout formRead;
    @ViewComponent
    private HorizontalLayout thoiGianTaiKhamField;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if(this.isReadOnly()){
            Optional<PhieuDieuTri> phieuDieuTri = dataManager.load(PhieuDieuTri.class)
                    .query("select p from PhieuDieuTri p where p.idBenhNhan = :bn order by p.ngayKham desc")
                    .parameter("bn", getEditedEntity())
                    .maxResults(1)
                    .optional();
            Span span = uiComponents.create(Span.class);
            Span spanTG = uiComponents.create(Span.class);

            if (phieuDieuTri.isPresent()) {
                if(phieuDieuTri.get().getTrangThai() != null){
                    var trangThai = phieuDieuTri.get().getTrangThai();
                    span.setText(messages.getMessage(trangThai));
                    span.addClassName(trangThai.toString()); // gán class CSS (VD: DANG_DT, DA_DT, KHONG_DT)
                }

                if(phieuDieuTri.get().getThoiGianTaiKham() != null){
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    spanTG.setText(sdf.format(phieuDieuTri.get().getThoiGianTaiKham()));
                }
            }

            Span label = uiComponents.create(Span.class);
            label.setText("Trạng thái: ");
            label.addClassName("trang-thai-label");

            Span labelTG = uiComponents.create(Span.class);
            labelTG.setText("Thời gian tái khám: ");
            label.addClassName("trang-thai-label");

            trangThaiBox.removeAll();
            trangThaiBox.add(label, span);

            thoiGianTaiKhamField.removeAll();
            thoiGianTaiKhamField.add(labelTG, spanTG);

            //Mở màn xem
            formCreate.setVisible(false);
            formRead.setVisible(true);
        }

    }
}