package com.company.clinicportal.view.buoidieutri;

import com.company.clinicportal.entity.*;
import com.company.clinicportal.service.BuoiDieuTriService;
import com.company.clinicportal.enumentity.CaLamViec;
import com.company.clinicportal.enumentity.NhomDichVu;
import com.company.clinicportal.enumentity.TrangThaiBuoiDieuTri;
import com.company.clinicportal.view.lichhen.LichHenDetailView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.list.RemoveAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Route(value = "buoi-dieu-tris", layout = MainView.class)
@ViewController(id = "BuoiDieuTri.list")
@ViewDescriptor(path = "buoi-dieu-tri-list-view.xml")
@LookupComponent("buoiDieuTrisDataGrid")
@DialogMode(width = "80%", height = "100%")
public class BuoiDieuTriListView extends StandardListView<BuoiDieuTri> {
    public Long idChiTietDichVu = null;

    @ViewComponent
    private CollectionLoader<BuoiDieuTri> buoiDieuTrisDl;
    @Autowired
    private DataManager dataManager;
    @ViewComponent
    private H3 dichVuField;
    @Autowired
    private Messages messages;
    @Autowired
    private UiComponents uiComponents;
    @ViewComponent
    private DataGrid<BuoiDieuTri> buoiDieuTrisDataGrid;
    @Autowired
    private DialogWindows dialogWindows;
    @ViewComponent("buoiDieuTrisDataGrid.removeAction")
    private RemoveAction<BuoiDieuTri> buoiDieuTrisDataGridRemoveAction;
    @Autowired
    private Metadata metadata;
    @Autowired
    private BuoiDieuTriService buoiDieuTriService;

    @Subscribe("buoiDieuTrisDataGrid.removeAction")
    public void onBuoiDieuTrisDataGridRemoveBefore(RemoveOperation.BeforeActionPerformedEvent<BuoiDieuTri> event) {
        List<BuoiDieuTri> items = event.getItems();
        if (items.isEmpty()) {
            return;
        }
        event.preventAction();
        buoiDieuTriService.deleteAll(items);
        buoiDieuTrisDl.setParameter("idChiTietDichVu", idChiTietDichVu);
        buoiDieuTrisDl.load();
    }

    public void setIdChiTietDichVu(Long idChiTietDichVu) {
        this.idChiTietDichVu = idChiTietDichVu;
    }

    private ChiTietDichVu chiTietDichVu = null;

    @Subscribe
    public void onInit(InitEvent event) {
        buoiDieuTrisDataGrid.addComponentColumn(buoiDieuTri -> {
                    // Tạo layout chứa hai nút
                    HorizontalLayout actionsLayout = uiComponents.create(HorizontalLayout.class);

                    // Nút Sửa
                    JmixButton editButton = uiComponents.create(JmixButton.class);
                    if(buoiDieuTri.getTrangThai().equals(TrangThaiBuoiDieuTri.DA_THUC_HIEN))editButton.setEnabled(false);
                    editButton.setText("Sửa");
                    editButton.addClickListener(e -> {
                        DialogWindow<BuoiDieuTriDetailView> window = dialogWindows.detail(this, BuoiDieuTri.class)
                                .editEntity(buoiDieuTri) // chỉnh sửa entity hiện tại
                                .withViewClass(BuoiDieuTriDetailView.class)
                                .build();
                        window.addAfterCloseListener(e1 -> {
                            buoiDieuTrisDl.setParameter("idChiTietDichVu", idChiTietDichVu);
                            buoiDieuTrisDl.load();
                        });
                        window.open();
                    });

                    // Nút Xóa
                    JmixButton deleteButton = uiComponents.create(JmixButton.class);
                    deleteButton.setText("Xóa");
                    deleteButton.addClickListener(e -> {
                        buoiDieuTrisDataGrid.select(buoiDieuTri);
                        buoiDieuTrisDataGridRemoveAction.execute();
                    });

                    // Thêm 2 nút vào layout
                    actionsLayout.add(editButton);
                    actionsLayout.add(deleteButton);

                    return actionsLayout;
                })
                .setHeader("Thao tác")
                .setAutoWidth(true);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        buoiDieuTrisDl.setParameter("idChiTietDichVu", idChiTietDichVu);
        buoiDieuTrisDl.load();

        chiTietDichVu = dataManager.load(ChiTietDichVu.class).id(idChiTietDichVu).optional().orElse(null);
        if(chiTietDichVu != null) {
            dichVuField.setText("Dịch vụ: " + chiTietDichVu.getIdDichVu().getTenDichVu());
        }
    }

    @Install(to = "buoiDieuTrisDataGrid.createAction", subject = "newEntitySupplier")
    private BuoiDieuTri buoiDieuTrisDataGridCreateActionNewEntitySupplier() {
        BuoiDieuTri buoiDieuTri = dataManager.create(BuoiDieuTri.class);
        buoiDieuTri.setIdChiTietDichVu(chiTietDichVu);
        buoiDieuTri.setIdBenhNhan(chiTietDichVu.getIdChiTietPhieuDieuTri().getIdBenhNhan());
        buoiDieuTri.setIdChiTietDieuTri(chiTietDichVu.getIdChiTietPhieuDieuTri());
        return buoiDieuTri;
    }

    @Install(to = "buoiDieuTrisDataGrid.createAction", subject = "afterSaveHandler")
    private void buoiDieuTrisDataGridCreateActionAfterSaveHandler(final BuoiDieuTri buoiDieuTri) {
        SaveContext saveContext = new SaveContext();
        buoiDieuTri.setCa(CaLamViec.fromGioBatDau(buoiDieuTri.getGioBatDau()));
        saveContext.saving(buoiDieuTri);

        // Chỉ tính KPI khi buổi điều trị đã thực hiện.
        if (TrangThaiBuoiDieuTri.DA_THUC_HIEN.equals(buoiDieuTri.getTrangThai())) {
            if (buoiDieuTri.getIdNhanSuStaging() != null) {
                TinhKpi tinhKpi = dataManager.create(TinhKpi.class);
                tinhKpi.setIdNhanSu(buoiDieuTri.getIdNhanSuStaging());
                tinhKpi.setThang(new Date());

                Double sumKpi = tinhTrongSoKpi(buoiDieuTri).toBigInteger().doubleValue();
                if (CaLamViec.SANG.equals(buoiDieuTri.getCa())) {
                    tinhKpi.setKpiSang(sumKpi);
                    tinhKpi.setKpiToi((double) 0);
                    tinhKpi.setKpiTong(sumKpi);
                } else {
                    tinhKpi.setKpiToi(sumKpi);
                    tinhKpi.setKpiSang((double) 0);
                    tinhKpi.setKpiTong(sumKpi);
                }

                GiaKpi giaKpi = dataManager.load(GiaKpi.class).condition(PropertyCondition.equal("loai", buoiDieuTri.getCa().getId())).one();
                tinhKpi.setThanhTien(Math.round(giaKpi.getGia().doubleValue() * tinhKpi.getKpiTong()));
                saveContext.saving(tinhKpi);
            }

            if (buoiDieuTri.getIdNhanSu2Staging() != null) {
                TinhKpi tinhKpi = dataManager.create(TinhKpi.class);
                tinhKpi.setIdNhanSu(buoiDieuTri.getIdNhanSu2Staging());
                tinhKpi.setThang(new Date());

                Double sumKpi = tinhTrongSoKpi(buoiDieuTri).toBigInteger().doubleValue();
                if (CaLamViec.SANG.equals(buoiDieuTri.getCa())) {
                    tinhKpi.setKpiSang(sumKpi);
                    tinhKpi.setKpiToi((double) 0);
                    tinhKpi.setKpiTong(sumKpi);
                } else {
                    tinhKpi.setKpiToi(sumKpi);
                    tinhKpi.setKpiSang((double) 0);
                    tinhKpi.setKpiTong(sumKpi);
                }

                GiaKpi giaKpi = dataManager.load(GiaKpi.class).condition(PropertyCondition.equal("loai", buoiDieuTri.getCa().getId())).one();
                tinhKpi.setThanhTien(Math.round(giaKpi.getGia().doubleValue() * tinhKpi.getKpiTong()));
                saveContext.saving(tinhKpi);
            }
        }

        dataManager.save(saveContext);
    }
        

    @Supply(to = "buoiDieuTrisDataGrid.trangThai", subject = "renderer")
    private Renderer<BuoiDieuTri> buoiDieuTrisDataGridTrangThaiRenderer() {
        return new ComponentRenderer<>(buoidieutri -> {
            // TODO: create suitable component
            Span span = uiComponents.create(Span.class);
            if (buoidieutri.getTrangThai()!=null) {
                span.setText(messages.getMessage(buoidieutri.getTrangThai()));
                span.addClassName(buoidieutri.getTrangThai().getId());
            }
            return span;
        });
    }

    private BigDecimal tinhTrongSoKpi (BuoiDieuTri buoiDieuTri){
        BigDecimal totalTrongSoKpi = BigDecimal.ONE;
        totalTrongSoKpi = totalTrongSoKpi.multiply(BigDecimal.valueOf(buoiDieuTri.getIdChiTietDichVu().getIdChiTietPhieuDieuTri().getTrongSoKpi()));

        if (buoiDieuTri != null
                && buoiDieuTri.getIdChiTietDichVu() != null
                && buoiDieuTri.getIdChiTietDichVu().getIdDichVu() != null
                && buoiDieuTri.getIdChiTietDichVu().getIdDichVu().getNhomDichVu() != null
                && buoiDieuTri.getIdChiTietDichVu().getIdChiTietPhieuDieuTri() != null) {

            NhomDichVu nhom = buoiDieuTri.getIdChiTietDichVu().getIdDichVu().getNhomDichVu();
            var chiTietPhieu = buoiDieuTri.getIdChiTietDichVu().getIdChiTietPhieuDieuTri();
            BigDecimal trongSo = null;

            switch (nhom) {
                case VAT_LY_TRI_LIEU:
                    if(chiTietPhieu.getTrongSoVatLyTriLieu() != null)trongSo = BigDecimal.valueOf(chiTietPhieu.getTrongSoVatLyTriLieu());
                    break;
                case VAN_DONG_TRI_LIEU:
                    if(chiTietPhieu.getTrongSoVanDongTriLieu() != null)trongSo = BigDecimal.valueOf(chiTietPhieu.getTrongSoVanDongTriLieu());
                    break;
                case KEO_NAN_TRI_LIEU:
                    if(chiTietPhieu.getTrongSoKeoNanTriLieu() != null)trongSo = BigDecimal.valueOf(chiTietPhieu.getTrongSoKeoNanTriLieu());
                    break;
                case XOA_BOP_TRI_LIEU:
                    if(chiTietPhieu.getTrongSoXoaBopTriLieu() != null)trongSo = BigDecimal.valueOf(chiTietPhieu.getTrongSoXoaBopTriLieu());
                    break;
                case KHAM_LUONG_GIA:
                    if(chiTietPhieu.getTrongSoKhamLuongGia() != null)trongSo = BigDecimal.valueOf(chiTietPhieu.getTrongSoKhamLuongGia());
                    break;
                default:
                    break;
            }

            // Kiểm tra trongSo null trước khi nhân
            if (trongSo != null) {
                totalTrongSoKpi = totalTrongSoKpi.multiply(trongSo);
            }
        }

        if(buoiDieuTri.getIdNhanSu2Staging() != null && buoiDieuTri.getIdNhanSuStaging() != null)   totalTrongSoKpi = totalTrongSoKpi.multiply(BigDecimal.valueOf(0.5));

        return totalTrongSoKpi;
    }


}