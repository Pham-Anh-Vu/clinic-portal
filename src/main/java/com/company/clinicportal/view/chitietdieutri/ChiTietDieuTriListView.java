package com.company.clinicportal.view.chitietdieutri;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.LichHen;
import com.company.clinicportal.view.buoidieutri.BuoiDieuTriListView;
import com.company.clinicportal.view.lichhen.LichHenDetailView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.list.RemoveAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;


@Route(value = "chi-tiet-dieu-tris", layout = MainView.class)
@ViewController(id = "ChiTietDieuTri.list")
@ViewDescriptor(path = "chi-tiet-dieu-tri-list-view.xml")
@LookupComponent("chiTietDieuTrisDataGrid")
@DialogMode(width = "80%", height = "100%")
public class ChiTietDieuTriListView extends StandardListView<ChiTietDieuTri> {
    @ViewComponent
    private CollectionLoader<ChiTietDieuTri> chiTietDieuTrisDl;
    @ViewComponent
    private CollectionContainer<ChiTietDieuTri> chiTietDieuTrisDc;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private DataManager dataManager;
    @ViewComponent
    private H3 benhNhanField;
    @Autowired
    private Metadata metadata;
    @Autowired
    private MetadataTools metadataTools;
    @ViewComponent
    private DataGrid<ChiTietDieuTri> chiTietDieuTrisDataGrid;
    @Autowired
    private UiComponents uiComponents;
    @ViewComponent("chiTietDieuTrisDataGrid.removeAction")
    private RemoveAction<ChiTietDieuTri> chiTietDieuTrisDataGridRemoveAction;

    public Long idBenhNhan;

    public void setIdBenhNhan(Long idBenhNhan) {
        this.idBenhNhan = idBenhNhan;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if(idBenhNhan != null){
            chiTietDieuTrisDl.setParameter("idBenhNhan", idBenhNhan);
            chiTietDieuTrisDl.load();
            applyPaymentSummaryForList();
        }

        BenhNhan bn = dataManager.load(BenhNhan.class).id(idBenhNhan).optional().orElse(null);
        if(bn != null) benhNhanField.setText("Bệnh nhân: " + bn.getInstanceName(metadataTools));
    }

    @Subscribe
    public void onInit(InitEvent event) {
        chiTietDieuTrisDataGrid.addComponentColumn(chiTietDieuTri -> {
                    // Tạo layout chứa hai nút
                    HorizontalLayout actionsLayout = uiComponents.create(HorizontalLayout.class);

                    // Nút Sửa
                    JmixButton editButton = uiComponents.create(JmixButton.class);
                    editButton.setText("Chi tiết");
                    editButton.addClickListener(e -> {
                        DialogWindow<ChiTietDieuTriDetailView> window = dialogWindows.detail(this, ChiTietDieuTri.class)
                                .editEntity(chiTietDieuTri) // chỉnh sửa entity hiện tại
                                .withViewClass(ChiTietDieuTriDetailView.class)
                                .build();
                        window.addAfterCloseListener(e1 -> {
                            chiTietDieuTrisDl.setParameter("idBenhNhan", idBenhNhan);
                            chiTietDieuTrisDl.load();
                            applyPaymentSummaryForList();
                        });

                        window.open();
                    });

                    // Nút Xóa
                    JmixButton deleteButton = uiComponents.create(JmixButton.class);
                    deleteButton.setText("Xóa");
                    deleteButton.addClickListener(e -> {
                        chiTietDieuTrisDataGrid.select(chiTietDieuTri);
                        chiTietDieuTrisDataGridRemoveAction.execute();
                    });

                    // Thêm 2 nút vào layout
                    actionsLayout.add(editButton);
                    actionsLayout.add(deleteButton);

                    return actionsLayout;
                })
                .setHeader("Thao tác")
                .setAutoWidth(true);
    }

    @Subscribe("chiTietDieuTrisDataGrid.createAction")
    public void onChiTietDieuTrisDataGridCreateAction(final ActionPerformedEvent event) {
        DialogWindow<PhieuChiDinhDetailView> dialogWindow =  dialogWindows.detail(this, ChiTietDieuTri.class)
                .withViewClass(PhieuChiDinhDetailView.class)
                .newEntity()
                .build();
        dialogWindow.getView().setIdBenhNhan(idBenhNhan);
        dialogWindow.setWidth("80%");
        dialogWindow.setHeight("100%");
        
        // Reload datagrid after dialog closes
        dialogWindow.addAfterCloseListener(event1 -> {
            if(idBenhNhan != null){
                chiTietDieuTrisDl.setParameter("idBenhNhan", idBenhNhan);
                chiTietDieuTrisDl.load();
                applyPaymentSummaryForList();
            }
        });
        
        dialogWindow.open();
    }

    private void applyPaymentSummaryForList() {
        for (ChiTietDieuTri chiTietDieuTri : chiTietDieuTrisDc.getItems()) {
            Number tongTienNumber = dataManager.loadValue(
                            "select coalesce(sum(dv.idDichVu.gia * dv.soLuong), 0) from ChiTietDichVu dv where dv.idChiTietPhieuDieuTri = :ctdt",
                            Number.class
                    )
                    .parameter("ctdt", chiTietDieuTri)
                    .one();
            long tongTien = tongTienNumber != null ? tongTienNumber.longValue() : 0L;
            double khuyenMaiPercent = chiTietDieuTri.getKhuyenMai() != null ? chiTietDieuTri.getKhuyenMai() : 0D;
            long daThanhToan = dataManager.loadValue(
                            "select coalesce(sum(e.daThanhToan), 0) from LichSuThanhToan e where e.idChiTietDieuTri = :ctdt",
                            Long.class
                    )
                    .parameter("ctdt", chiTietDieuTri)
                    .one();
            Date ngayThanhToanCuoi = dataManager.loadValue(
                            "select max(e.thanhToanLuc) from LichSuThanhToan e where e.idChiTietDieuTri = :ctdt",
                            Date.class
                    )
                    .parameter("ctdt", chiTietDieuTri)
                    .optional()
                    .orElse(null);

            BigDecimal tongTienBd = BigDecimal.valueOf(tongTien);
            long tongSauKhuyenMai = tongTienBd.subtract(
                            tongTienBd.multiply(BigDecimal.valueOf(khuyenMaiPercent))
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    )
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
            int phaiDong = BigDecimal.valueOf(tongSauKhuyenMai)
                    .subtract(BigDecimal.valueOf(daThanhToan))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();

            chiTietDieuTri.setTongTien(tongTien);
            chiTietDieuTri.setTongTienSauKhuyenMai(tongSauKhuyenMai);
            chiTietDieuTri.setDaThanhToan(daThanhToan);
            chiTietDieuTri.setPhaiDong(phaiDong);
            chiTietDieuTri.setNgayThanhToan(ngayThanhToanCuoi);
        }
    }
}