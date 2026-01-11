package com.company.clinicportal.view.chitietdieutri;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.LichSuThanhToan;
import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.enumentity.TrangThaiBuoiDieuTri;
import com.company.clinicportal.view.buoidieutri.BuoiDieuTriListView;
import com.company.clinicportal.view.chitietdichvu.ChiTietDichVuDetailView;
import com.company.clinicportal.view.lichsuthanhtoan.LichSuThanhToanDetailView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
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
    @Autowired
    private Metadata metadata;
    @ViewComponent
    private CollectionPropertyContainer<ChiTietDichVu> chiTietDichVuDc;
    @ViewComponent
    private InstanceLoader<ChiTietDieuTri> chiTietDieuTriDl;

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



    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if(getEditedEntity().getIdPhieuDieuTri() != null){
            lichSuThanhToansDl.setParameter("idPhieuDieuTri", getEditedEntity().getIdPhieuDieuTri());
            lichSuThanhToansDl.load();
        }

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

    @Install(to = "lichSuThanhToansDataGrid.create", subject = "newEntitySupplier")
    private LichSuThanhToan lichSuThanhToansDataGridCreateNewEntitySupplier() {
        LichSuThanhToan lichSuThanhToan = metadata.create(LichSuThanhToan.class);
        lichSuThanhToan.setIdPhieuDieuTri(getEditedEntity().getIdPhieuDieuTri());
        return lichSuThanhToan;
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    public void onPostSave(final DataContext.PostSaveEvent event) {
        System.out.println(event);
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
                // Thiết lập quan hệ với ChiTietDieuTri
                saved.setIdChiTietPhieuDieuTri(getEditedEntity());
                saved.setCreatedAt(LocalDateTime.now());
                // Lưu entity vào database ngay lập tức
                ChiTietDichVu persisted = dataManager.save(saved);
                
                // Kiểm tra các biến cần thiết để tạo BuoiDieuTri
                Long soLuong = persisted.getSoLuong();
                Date ngayBatDau = persisted.getNgayBatDau();
                Long khoangCachBuoiDieuTri = persisted.getKhoangCachBuoiDieuTri();
                
                // Nếu có đủ thông tin, tạo các BuoiDieuTri
                if (soLuong != null && soLuong > 0 && 
                    ngayBatDau != null && 
                    khoangCachBuoiDieuTri != null && khoangCachBuoiDieuTri > 0 &&
                    persisted.getIdChiTietPhieuDieuTri() != null &&
                    persisted.getIdChiTietPhieuDieuTri().getIdBenhNhan() != null) {
                    
                    // Tạo SaveContext để lưu nhiều entity cùng lúc
                    SaveContext saveContext = new SaveContext();
                    
                    // Tạo số lượng BuoiDieuTri tương ứng
                    for (int i = 0; i < soLuong; i++) {
                        BuoiDieuTri buoiDieuTri = dataManager.create(BuoiDieuTri.class);
                        
                        // Thiết lập thông tin cơ bản
                        buoiDieuTri.setIdChiTietDichVu(persisted);
                        buoiDieuTri.setIdBenhNhan(persisted.getIdChiTietPhieuDieuTri().getIdBenhNhan());
                        
                        // Tính ngày thực hiện: ngayBatDau + (khoangCachBuoiDieuTri * i) ngày
                        Calendar ngayThucHienCal = Calendar.getInstance();
                        ngayThucHienCal.setTime(ngayBatDau);
                        ngayThucHienCal.add(Calendar.DAY_OF_MONTH, (int) (khoangCachBuoiDieuTri * i));
                        Date ngayThucHien = ngayThucHienCal.getTime();
                        buoiDieuTri.setNgayThucHien(ngayThucHien);
                        buoiDieuTri.setTrangThai(TrangThaiBuoiDieuTri.CHUA_THUC_HIEN);
                        
                        // Thêm vào SaveContext
                        saveContext.saving(buoiDieuTri);
                    }
                    
                    // Lưu tất cả các BuoiDieuTri
                    if (!saveContext.getEntitiesToSave().isEmpty()) {
                        dataManager.save(saveContext);
                    }
                }
                
                // Reload collection để hiển thị entity mới
                chiTietDieuTriDl.load();
            }
        });
        window.open();
    }

}