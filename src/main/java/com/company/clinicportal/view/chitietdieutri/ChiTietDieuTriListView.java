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
import com.company.clinicportal.service.ChiTietDieuTriPaymentSummaryService;
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
    @Autowired
    private ChiTietDieuTriPaymentSummaryService paymentSummaryService;
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
        if (idBenhNhan != null) {
            chiTietDieuTrisDl.setParameter("idBenhNhan", idBenhNhan);
            chiTietDieuTrisDl.load();
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
                            if (!e1.closedWith(StandardOutcome.SAVE)) {
                                return;
                            }
                            Long phieuId = chiTietDieuTri.getId();
                            if (phieuId != null) {
                                paymentSummaryService.refreshPaymentSummary(phieuId);
                            }
                            chiTietDieuTrisDl.setParameter("idBenhNhan", idBenhNhan);
                            chiTietDieuTrisDl.load();
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
        dialogWindow.setWidth("60%");
        dialogWindow.setHeight("90%");

        // Reload datagrid after dialog closes; nếu save thành công -> auto mở màn chi tiết phiếu điều trị
        dialogWindow.addAfterCloseListener(event1 -> {
            if (idBenhNhan != null) {
                chiTietDieuTrisDl.setParameter("idBenhNhan", idBenhNhan);
                chiTietDieuTrisDl.load();
            }
            if (event1.closedWith(StandardOutcome.SAVE)) {
                ChiTietDieuTri saved = dialogWindow.getView().getEditedEntity();
                if (saved != null && saved.getId() != null) {
                    DialogWindow<ChiTietDieuTriDetailView> detailWindow = dialogWindows.detail(this, ChiTietDieuTri.class)
                            .withViewClass(ChiTietDieuTriDetailView.class)
                            .editEntity(saved)
                            .build();
                    detailWindow.addAfterCloseListener(detailClose -> {
                        if (idBenhNhan != null) {
                            chiTietDieuTrisDl.setParameter("idBenhNhan", idBenhNhan);
                            chiTietDieuTrisDl.load();
                        }
                    });
                    detailWindow.open();
                }
            }
        });

        dialogWindow.open();
    }

}