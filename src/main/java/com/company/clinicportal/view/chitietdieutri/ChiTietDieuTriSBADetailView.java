package com.company.clinicportal.view.chitietdieutri;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.LichSuThanhToan;
import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.enumentity.TrangThaiBuoiDieuTri;
import com.company.clinicportal.view.buoidieutri.BuoiDieuTriListView;
import com.company.clinicportal.view.chitietdichvu.ChiTietDichVuDetailView;
import com.company.clinicportal.view.lichsuthanhtoan.LichSuThanhToanDetailView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.data.value.ValueChangeMode;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

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
    @Autowired
    private DataManager dataManager;
    @ViewComponent
    private CollectionPropertyContainer<ChiTietDichVu> chiTietDichVuDc;

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

        recalculatePaymentFields();
    }

    @Subscribe("chiTietDichVusDataGrid.create")
    public void onChiTietDichVusDataGridCreate(final ActionPerformedEvent event) {
        DialogWindow<ChiTietDichVuDetailView> window =
                dialogWindows.detail(this, ChiTietDichVu.class)
                        .withViewClass(ChiTietDichVuDetailView.class)
                        .newEntity()
                        .build();

        window.addAfterCloseListener(event1 -> {
            if (event1.closedWith(StandardOutcome.SAVE)) {
                ChiTietDichVu saved = event1.getView().getEditedEntity();
                saved.setIdChiTietPhieuDieuTri(getEditedEntity());
                saved.setCreatedAt(LocalDateTime.now());
                ChiTietDichVu persisted = dataManager.save(saved);

                Long soLuong = persisted.getSoLuong();
                Date ngayBatDau = persisted.getNgayBatDau();
                Long khoangCachBuoiDieuTri = persisted.getKhoangCachBuoiDieuTri();

                if (soLuong != null && soLuong > 0
                        && ngayBatDau != null
                        && khoangCachBuoiDieuTri != null && khoangCachBuoiDieuTri > 0
                        && persisted.getIdChiTietPhieuDieuTri() != null
                        && persisted.getIdChiTietPhieuDieuTri().getIdBenhNhan() != null) {

                    SaveContext saveContext = new SaveContext();

                    for (int i = 0; i < soLuong; i++) {
                        BuoiDieuTri buoiDieuTri = dataManager.create(BuoiDieuTri.class);
                        buoiDieuTri.setIdChiTietDichVu(persisted);
                        buoiDieuTri.setIdBenhNhan(persisted.getIdChiTietPhieuDieuTri().getIdBenhNhan());

                        Calendar ngayThucHienCal = Calendar.getInstance();
                        ngayThucHienCal.setTime(ngayBatDau);
                        ngayThucHienCal.add(Calendar.DAY_OF_MONTH, (int) (khoangCachBuoiDieuTri * i));
                        buoiDieuTri.setNgayThucHien(ngayThucHienCal.getTime());
                        buoiDieuTri.setTrangThai(TrangThaiBuoiDieuTri.CHUA_THUC_HIEN);

                        saveContext.saving(buoiDieuTri);
                    }

                    if (!saveContext.getEntitiesToSave().isEmpty()) {
                        dataManager.save(saveContext);
                    }
                }

                chiTietDieuTriDl.load();
                recalculatePaymentFields();
            }
        });
        window.open();
    }

    @Subscribe("khuyenMaiField")
    public void onKhuyenMaiFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<TypedTextField<Double>, Double> event) {
        recalculatePaymentFields();
    }

    @Subscribe("khuyenMaiField")
    public void onKhuyenMaiFieldTypedValueChange(final SupportsTypedValue.TypedValueChangeEvent<TypedTextField<Double>, Double> event) {
        recalculatePaymentFields();
    }

    private void recalculatePaymentFields() {
        long tongTien = 0L;
        for (ChiTietDichVu item : chiTietDichVuDc.getItems()) {
            long gia = item.getIdDichVu() != null && item.getIdDichVu().getGia() != null
                    ? item.getIdDichVu().getGia()
                    : 0L;
            long soBuoi = item.getSoLuong() != null ? item.getSoLuong() : 0L;
            tongTien += gia * soBuoi;
        }

        BigDecimal tongTienBd = BigDecimal.valueOf(tongTien);
        BigDecimal khuyenMaiPercent = BigDecimal.valueOf(
                khuyenMaiField.getTypedValue() != null ? khuyenMaiField.getTypedValue() : 0D
        );
        BigDecimal daThanhToan = BigDecimal.valueOf(
                daThanhToanField.getTypedValue() != null ? daThanhToanField.getTypedValue() : 0L
        );

        BigDecimal tongSauKhuyenMaiBd = tongTienBd.subtract(
                tongTienBd.multiply(khuyenMaiPercent)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        );
        long tongSauKhuyenMai = tongSauKhuyenMaiBd.setScale(0, RoundingMode.HALF_UP).longValue();
        int phaiDong = BigDecimal.valueOf(tongSauKhuyenMai)
                .subtract(daThanhToan)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        getEditedEntity().setTongTien(tongTien);
        getEditedEntity().setTongTienSauKhuyenMai(tongSauKhuyenMai);
        getEditedEntity().setPhaiDong(phaiDong);

        tongTienField.setTypedValue(tongTien);
        tongTienSauKhuyenMaiField.setTypedValue(tongSauKhuyenMai);
        phaiDongField.setTypedValue(phaiDong);
    }

    @Install(to = "lichSuThanhToansDataGrid.create", subject = "newEntitySupplier")
    private LichSuThanhToan lichSuThanhToansDataGridCreateNewEntitySupplier() {
        LichSuThanhToan lichSuThanhToan = metadata.create(LichSuThanhToan.class);
        lichSuThanhToan.setIdPhieuDieuTri(getEditedEntity().getIdPhieuDieuTri());
        return lichSuThanhToan;
    }
}