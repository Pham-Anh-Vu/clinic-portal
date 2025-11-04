package com.company.clinicportal.view.benhnhan;

import com.company.clinicportal.entity.*;
import com.company.clinicportal.view.buoidieutri.DanhSachDichVuTrong1BListView;
import com.company.clinicportal.view.buoidieutri.ThuThuatListView;
import com.company.clinicportal.view.chitietdichvu.ChiTietDichVuBDTListView;
import com.company.clinicportal.view.chitietdieutri.ChiTietDieuTriListView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.chartsflowui.component.Chart;
import com.company.clinicportal.view.benhnhan.dto.StatusCount;
import io.jmix.flowui.model.CollectionContainer;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    @ViewComponent
    private DataGrid<BuoiDieuTri> buoiDieuTrisDataGrid;
    @ViewComponent
    private Chart statusChart;
    @ViewComponent
    private CollectionContainer<StatusCount> statusCountsDc;
    @Autowired
    private DialogWindows dialogWindows;
    @ViewComponent
    private CollectionLoader<BuoiDieuTri> buoiDieuTrisDl;
    @Autowired
    private Notifications notifications;
    @ViewComponent
    private H3 benhNhanField;
    @Autowired
    private MetadataTools metadataTools;

    @Subscribe
    public void onInit(InitEvent event) {
        buoiDieuTrisDataGrid.addComponentColumn(lh -> {
                    Span span = uiComponents.create(Span.class);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

                    if (lh == null
                            || lh.getIdChiTietDichVu() == null
                            || lh.getIdChiTietDichVu().getIdChiTietPhieuDieuTri() == null
                            || lh.getIdChiTietDichVu().getIdChiTietPhieuDieuTri().getGioHenMacDinh() == null) {
                        span.setText("");
                    } else {
                        Date gioHen = lh.getIdChiTietDichVu().getIdChiTietPhieuDieuTri().getGioHenMacDinh();
                        span.setText(timeFormat.format(gioHen));
                    }

                    return span;
                })
                .setHeader("Giờ hẹn")
                .setAutoWidth(true);

        buoiDieuTrisDataGrid.addColumn(lh -> {
                    if (lh == null
                            || lh.getIdChiTietDichVu() == null
                            || lh.getIdChiTietDichVu().getIdChiTietPhieuDieuTri() == null) {
                        return "";
                    }
                    Long soDichVu = dataManager.loadValue(
                                    "select count(e) from BuoiDieuTri e where e.ngayThucHien = :ngayThucHien and e.idBenhNhan = :idBenhNhan",
                                    Long.class)
                            .parameter("ngayThucHien", lh.getNgayThucHien())
                            .parameter("idBenhNhan", lh.getIdBenhNhan())
                            .one();

                    return soDichVu.toString();
                })
                .setHeader("Số dịch vụ")
                .setAutoWidth(true);

        buoiDieuTrisDataGrid.addComponentColumn(buoiDieuTri -> {
            // Tạo layout chứa hai nút
            HorizontalLayout actionsLayout = uiComponents.create(HorizontalLayout.class);

            // Nút Sửa
            JmixButton editButton = uiComponents.create(JmixButton.class);
            editButton.setText("Sửa giờ hẹn");
            editButton.addClickListener(e -> {
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("Sửa giờ hẹn");
                dialog.setHeight("100%");
                dialog.setWidth("80%");

                // Tạo layout
                VerticalLayout layout = new VerticalLayout();
                layout.setSpacing(true);
                layout.setPadding(true);

                // Tạo TextField
                TimePicker timePicker = uiComponents.create(TimePicker.class);
                timePicker.setLabel("Giờ hẹn");
                timePicker.setWidth("100%");

                // Tạo Button
                Button button = uiComponents.create(Button.class);
                button.setText("Xác nhận");
                button.addClickListener(eXN -> {
                    if (buoiDieuTri == null
                            || buoiDieuTri.getIdChiTietDichVu() == null
                            || buoiDieuTri.getIdChiTietDichVu().getIdChiTietPhieuDieuTri() == null) {
                        dialog.close();
                    }
                    ChiTietDieuTri chiTietDieuTri = dataManager.load(ChiTietDieuTri.class).id(buoiDieuTri.getIdChiTietDichVu().getIdChiTietPhieuDieuTri().getId()).optional().orElse(null);

                    if(chiTietDieuTri != null && timePicker.getValue() != null){
                        LocalTime localTime = timePicker.getValue();
                        LocalDate localDate = LocalDate.of(1970, 1, 1); // ngày mặc định
                        Date date = Date.from(LocalDateTime.of(localDate, localTime)
                                .atZone(ZoneId.systemDefault())
                                .toInstant());

                        chiTietDieuTri.setGioHenMacDinh(date);
                        dataManager.save(chiTietDieuTri);
                        dialog.close();

                    }else{
                        throw new ValidationException("Hãy nhập giờ hẹn");
                    }
                });

                Button btnDong = uiComponents.create(Button.class);
                btnDong.setText("Đóng");
                btnDong.addClickListener(eDong -> dialog.close());

                // Tạo layout ngang cho 2 nút
                HorizontalLayout buttonsLayout = uiComponents.create(HorizontalLayout.class);
                buttonsLayout.add(button, btnDong);
                buttonsLayout.setWidthFull();
                buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END); // canh phải (hoặc CENTER nếu muốn giữa)
                buttonsLayout.setSpacing(true);

                layout.add(timePicker, buttonsLayout);
                dialog.add(layout);

                dialog.open();
            });

            // Nút Chi tiết
            JmixButton deleteButton = uiComponents.create(JmixButton.class);
            deleteButton.setText("Chi Tiết");
            deleteButton.addClickListener(e -> {
                DialogWindow<DanhSachDichVuTrong1BListView> windows = dialogWindows.view(this, DanhSachDichVuTrong1BListView.class).build();
                windows.getView().setIdBenhNhan(buoiDieuTri.getIdBenhNhan());
                windows.getView().setNgayThucHien(buoiDieuTri.getNgayThucHien());
                windows.setHeight("100%");
                windows.setWidth("80%");
                windows.open();
            });

            // Thêm 3 nút vào layout
            actionsLayout.add(deleteButton);
            actionsLayout.add(editButton);

            return actionsLayout;
        }).setHeader("Thao tác").setAutoWidth(true);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if (this.isReadOnly()) {
            buoiDieuTrisDl.setParameter("idBenhNhan", getEditedEntity());
            buoiDieuTrisDl.load();
            // render chart after data is available
            renderStatusChart();

            benhNhanField.setText("Bệnh nhân: " + getEditedEntity().getInstanceName(metadataTools));

            Optional<PhieuDieuTri> phieuDieuTri = dataManager.load(PhieuDieuTri.class)
                    .query("select p from PhieuDieuTri p where p.idBenhNhan = :bn order by p.ngayKham desc")
                    .parameter("bn", getEditedEntity())
                    .maxResults(1)
                    .optional();
            Span span = uiComponents.create(Span.class);
            Span spanTG = uiComponents.create(Span.class);

            if (phieuDieuTri.isPresent()) {
                if (phieuDieuTri.get().getTrangThai() != null) {
                    var trangThai = phieuDieuTri.get().getTrangThai();
                    span.setText(messages.getMessage(trangThai));
                    span.addClassName(trangThai.toString()); // gán class CSS (VD: DANG_DT, DA_DT, KHONG_DT)
                }

                if (phieuDieuTri.get().getThoiGianTaiKham() != null) {
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

    @Subscribe(id = "buoiDieuTrisDl", target = Target.DATA_LOADER)
    public void onBuoiDieuTrisLoaded(CollectionLoader.PostLoadEvent<BuoiDieuTri> event) {
        renderStatusChart();
    }

    private void renderStatusChart() {
        if (statusCountsDc == null) {
            return;
        }

        List<BuoiDieuTri> items = buoiDieuTrisDataGrid != null && buoiDieuTrisDataGrid.getItems() != null
                ? buoiDieuTrisDataGrid.getItems().getItems().stream().toList()
                : List.of();

        Map<String, Integer> statusToCount = new LinkedHashMap<>();
        for (BuoiDieuTri bdt : items) {
            String key = bdt.getTrangThai() == null ? "Chưa xác định" : messages.getMessage(bdt.getTrangThai());
            statusToCount.merge(key, 1, Integer::sum);
        }

        List<StatusCount> grouped = new java.util.ArrayList<>();
        for (Map.Entry<String, Integer> e : statusToCount.entrySet()) {
            grouped.add(new StatusCount(e.getKey(), e.getValue()));
        }

        statusCountsDc.setItems(grouped);
    }
}