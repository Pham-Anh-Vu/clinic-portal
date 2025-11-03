package com.company.clinicportal.view.chitietdieutri;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.LichSuThanhToan;
import com.company.clinicportal.view.buoidieutri.BuoiDieuTriListView;
import com.company.clinicportal.view.lichsuthanhtoan.LichSuThanhToanDetailView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.router.Route;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Route(value = "chi-tiet-dieu-tri-sbas/:id", layout = MainView.class)
@ViewController(id = "ChiTietDieuTriSBA.detail")
@ViewDescriptor(path = "chi-tiet-dieu-tri-sba-detail-view.xml")
@EditedEntityContainer("chiTietDieuTriDc")
@DialogMode(height = "100%", width = "80%")
public class ChiTietDieuTriSBADetailView extends StandardDetailView<ChiTietDieuTri> {
    private BenhNhan idBenhNhan = null;
    @ViewComponent
    private InstanceLoader<ChiTietDieuTri> chiTietDieuTriDl;
    @ViewComponent
    private DataGrid<ChiTietDichVu> chiTietDichVusDataGrid;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private DialogWindows dialogWindows;
    @ViewComponent
    private DataGrid<LichSuThanhToan> lichSuThanhToansDataGrid;
    @ViewComponent
    private CollectionLoader<LichSuThanhToan> lichSuThanhToansDl;
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
    @Autowired
    private Metadata metadata;

    public void setIdBenhNhan(BenhNhan idBenhNhan) {
        this.idBenhNhan = idBenhNhan;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        chiTietDieuTriDl.setParameter("idBenhNhan", idBenhNhan);
        chiTietDieuTriDl.load();

        if(getEditedEntity().getIdPhieuDieuTri() != null){
            lichSuThanhToansDl.setParameter("idPhieuDieuTri", getEditedEntity().getIdPhieuDieuTri());
            lichSuThanhToansDl.load();
        }

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
                        dialogWindows.detail(this, LichSuThanhToan.class)
                                .withViewClass(LichSuThanhToanDetailView.class)
                                .editEntity(lichSuThanhToan)
                                .build();
                window.addAfterCloseListener(eTT -> {
                    lichSuThanhToansDl.setParameter("idPhieuDieuTri", getEditedEntity().getIdPhieuDieuTri());
                    lichSuThanhToansDl.load();
                });
                window.getView().setPhieuDieuTri(getEditedEntity().getIdPhieuDieuTri());
                window.open();
            });
            return button;
        }).setHeader("Thao tác").setAutoWidth(true);
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

    @Install(to = "lichSuThanhToansDataGrid.create", subject = "newEntitySupplier")
    private LichSuThanhToan lichSuThanhToansDataGridCreateNewEntitySupplier() {
        LichSuThanhToan lichSuThanhToan = metadata.create(LichSuThanhToan.class);
        lichSuThanhToan.setIdPhieuDieuTri(getEditedEntity().getIdPhieuDieuTri());
        return lichSuThanhToan;
    }
}