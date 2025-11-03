package com.company.clinicportal.view.chitietdieutri;

import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.LichSuThanhToan;
import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.view.buoidieutri.BuoiDieuTriListView;
import com.company.clinicportal.view.lichsuthanhtoan.LichSuThanhToanDetailView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Route(value = "chi-tiet-dieu-tris/:id", layout = MainView.class)
@ViewController(id = "ChiTietDieuTri.detail")
@ViewDescriptor(path = "chi-tiet-dieu-tri-detail-view.xml")
@EditedEntityContainer("chiTietDieuTriDc")
@DialogMode(width = "80%", height = "100%")
public class ChiTietDieuTriDetailView extends StandardDetailView<ChiTietDieuTri> {
    @ViewComponent
    private DataGrid<ChiTietDichVu> chiTietDichVusDataGrid;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private DialogWindows dialogWindows;
    @ViewComponent
    private CollectionLoader<LichSuThanhToan> lichSuThanhToansDl;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Messages messages;
    @ViewComponent
    private HorizontalLayout trangThaiBox;
    @ViewComponent
    private DataGrid<LichSuThanhToan> lichSuThanhToansDataGrid;
    @ViewComponent
    private TypedTextField<Long> tongTienField;
    @ViewComponent
    private TypedTextField<Double> khuyenMaiField;
    @ViewComponent
    private TypedTextField<Long> daThanhToanField;
    @ViewComponent
    private TypedTextField<Long> tongTienSauKhuyenMaiField;
    @ViewComponent
    private TypedTextField<Integer> phaiDongField;

    @Subscribe
    public void onInit(InitEvent event) {
        chiTietDichVusDataGrid.addComponentColumn(chiTietDichVu -> {
            JmixButton button = uiComponents.create(JmixButton.class);
            button.setText("Chi tiết");
            button.addClickListener(e -> {
                if (chiTietDichVu != null && chiTietDichVu.getId() != null) {
                    DialogWindow<View<?>> window =
                            dialogWindows.view(this, "BuoiDieuTri.list")
                                    .build();

                    BuoiDieuTriListView view = (BuoiDieuTriListView) window.getView();
                    view.setIdChiTietDichVu(chiTietDichVu.getId());
                    window.open();
                }
            });
            return button;
        }).setHeader("Thao tác").setAutoWidth(true);

        lichSuThanhToansDataGrid.addComponentColumn(lichSuThanhToan -> {
            JmixButton button = uiComponents.create(JmixButton.class);
            button.setText("Chi tiết");
            button.addClickListener(e -> {
                DialogWindow<LichSuThanhToanDetailView> window =
                        dialogWindows.view(this, LichSuThanhToanDetailView.class)
                                .build();
                window.open();
            });
            return button;
        }).setHeader("Thao tác").setAutoWidth(true);
    }



    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        lichSuThanhToansDl.setParameter("idPhieuDieuTri", getEditedEntity().getIdPhieuDieuTri());
        lichSuThanhToansDl.load();

        Optional<PhieuDieuTri> phieuDieuTri = dataManager.load(PhieuDieuTri.class)
                .query("select p from PhieuDieuTri p where p.idBenhNhan = :bn order by p.ngayKham desc")
                .parameter("bn", getEditedEntity().getIdBenhNhan())
                .maxResults(1)
                .optional();
        Span span = uiComponents.create(Span.class);

        if (phieuDieuTri.isPresent() && phieuDieuTri.get().getTrangThai() != null) {
            var trangThai = phieuDieuTri.get().getTrangThai();
            span.setText(messages.getMessage(trangThai));
            span.addClassName(trangThai.toString()); // gán class CSS (VD: DANG_DT, DA_DT, KHONG_DT)
        }

        Span label = uiComponents.create(Span.class);
        label.setText("Trạng thái: ");
        label.addClassName("trang-thai-label");

        trangThaiBox.removeAll();
        trangThaiBox.add(label, span);
    }

    @Subscribe("khuyenMaiField")
    public void onKhuyenMaiFieldValueChange(final HasValue.ValueChangeEvent<BigDecimal> event) {
        tinhToanSauKhuyenMai();
    }

    // 👉 Hàm tính toán sau khuyến mãi
    private void tinhToanSauKhuyenMai() {
        BigDecimal tongTien = BigDecimal.valueOf(
                tongTienField.getTypedValue() != null ? tongTienField.getTypedValue() : 0L
        );
        BigDecimal khuyenMai = BigDecimal.valueOf(
                khuyenMaiField.getTypedValue() != null ? khuyenMaiField.getTypedValue() : 0L
        );
        BigDecimal daThanhToan = BigDecimal.valueOf(
                daThanhToanField.getTypedValue() != null ? daThanhToanField.getTypedValue() : 0L);

        // Tính tổng sau khuyến mãi = tổng tiền - (tổng tiền * khuyến mãi / 100)
        BigDecimal tongSauKM = tongTien.subtract(
                tongTien.multiply(khuyenMai).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        );

        // Số tiền phải đóng = tổng sau KM - đã thanh toán
        BigDecimal phaiDong = tongSauKM.subtract(daThanhToan);

        // Cập nhật lại các field hiển thị
        tongTienSauKhuyenMaiField.setValue(tongSauKM.toString());
        phaiDongField.setValue(phaiDong.toString());
    }
}