package com.company.clinicportal.view.benhnhan;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.enumentity.TrangThaiPhieuDT;
import com.company.clinicportal.view.chitietdieutri.ChiTietDieuTriListView;
import com.company.clinicportal.view.chitietdieutri.ChiTietDieuTriSBADetailView;
import com.company.clinicportal.view.chitietdieutri.PhieuChiDinhDetailView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.DialogOutcome;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Optional;

@Route(value = "so-benh-ans/:id", layout = MainView.class)
@ViewController(id = "SoBenhAn.detail")
@ViewDescriptor(path = "so-benh-an-detail-view.xml")
@EditedEntityContainer("benhNhanDc")
@DialogMode(height = "100%", width = "80%")
public class SoBenhAnDetailView extends StandardDetailView<BenhNhan> {
    @Autowired
    private DataManager dataManager;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Messages messages;
    @ViewComponent
    private HorizontalLayout trangThaiBox;
    @ViewComponent
    private HorizontalLayout thoiGianTaiKhamBox;
    @ViewComponent
    private HorizontalLayout ngayKhamBenhBox;

    private BenhNhan idBenhNhan = null;
    @ViewComponent
    private DataGrid<ChiTietDieuTri> chiTietDieuTrisDataGrid;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private Dialogs dialogs;
    @ViewComponent
    private InstanceLoader<BenhNhan> benhNhanDl;
    @ViewComponent
    private JmixButton editBN;

    private PhieuDieuTri currentPhieuDieuTri = null;

    public void setIdBenhNhan(BenhNhan idBenhNhan) {
        this.idBenhNhan = idBenhNhan;
    }

    @ViewComponent
    private CollectionLoader<ChiTietDieuTri> chiTietDieuTrisDl;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if (idBenhNhan != null) {
            benhNhanDl.setEntityId(idBenhNhan.getId());
            benhNhanDl.load();
        }

        chiTietDieuTrisDl.setParameter("idBenhNhan", idBenhNhan);
        chiTietDieuTrisDl.load();

        Optional<PhieuDieuTri> phieuDieuTriOpt = dataManager.load(PhieuDieuTri.class)
                .query("select p from PhieuDieuTri p where p.idBenhNhan = :bn order by p.ngayKham desc")
                .parameter("bn", idBenhNhan)
                .maxResults(1)
                .optional();

        currentPhieuDieuTri = phieuDieuTriOpt.orElse(null);

        Span span = uiComponents.create(Span.class);
        Span spanTG = uiComponents.create(Span.class);
        Span spanNK = uiComponents.create(Span.class);

        if (phieuDieuTriOpt.isPresent()) {
            PhieuDieuTri phieuDieuTri = phieuDieuTriOpt.get();
            if(phieuDieuTri.getTrangThai() != null){
                var trangThai = phieuDieuTri.getTrangThai();
                span.setText(messages.getMessage(trangThai));
                span.addClassName(trangThai.toString()); // gán class CSS (VD: DANG_DT, DA_DT, KHONG_DT)
            }

            if(phieuDieuTri.getThoiGianTaiKham() != null){
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                spanTG.setText(sdf.format(phieuDieuTri.getThoiGianTaiKham()));
            }

            if(phieuDieuTri.getNgayKham() != null){
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                spanNK.setText(sdf.format(phieuDieuTri.getNgayKham()));
            }
        }

        Span label = uiComponents.create(Span.class);
        label.setText("Trạng thái: ");
        label.addClassName("trang-thai-label");

        Span labelTG = uiComponents.create(Span.class);
        labelTG.setText("Thời gian tái khám: ");
        labelTG.addClassName("trang-thai-label");

        Span labelNK = uiComponents.create(Span.class);
        labelNK.setText("Ngày khám bệnh: ");
        labelNK.addClassName("trang-thai-label");

        trangThaiBox.removeAll();
        trangThaiBox.add(label, span);

        thoiGianTaiKhamBox.removeAll();
        thoiGianTaiKhamBox.add(labelTG, spanTG);

        ngayKhamBenhBox.removeAll();
        ngayKhamBenhBox.add(labelNK, spanNK);

        chiTietDieuTrisDataGrid.addComponentColumn(chiTietDieuTri -> {
            JmixButton button = uiComponents.create(JmixButton.class);
            button.setText("Chi tiết");
            button.addClickListener(e -> {
                DialogWindow<ChiTietDieuTriSBADetailView> windows = dialogWindows.view(this, ChiTietDieuTriSBADetailView.class).build();
                windows.getView().setIdBenhNhan(idBenhNhan);
                windows.open();
            });
            return button;
        }).setHeader("Thao tác").setAutoWidth(true);
    }

    @Subscribe("chiTietDieuTrisDataGrid.create")
    public void onChiTietDieuTrisDataGridCreate(final ActionPerformedEvent event) {
        DialogWindow<PhieuChiDinhDetailView> dialogWindow =  dialogWindows.detail(this, ChiTietDieuTri.class)
                .withViewClass(PhieuChiDinhDetailView.class)
                .newEntity()
                .build();
        dialogWindow.getView().setIdBenhNhan(idBenhNhan.getId());
        dialogWindow.setWidth("80%");
        dialogWindow.setHeight("100%");

        // Reload datagrid after dialog closes
        dialogWindow.addAfterCloseListener(event1 -> {
            if(idBenhNhan != null){
                chiTietDieuTrisDl.setParameter("idBenhNhan", idBenhNhan);
                chiTietDieuTrisDl.load();
            }
        });

        dialogWindow.open();
    }

    @Subscribe("editBN")
    public void onEditBNClick(final com.vaadin.flow.component.ClickEvent<JmixButton> event) {
        if (currentPhieuDieuTri == null) {
            dialogs.createMessageDialog()
                    .withHeader("Thông báo")
                    .withText("Không tìm thấy phiếu điều trị để cập nhật trạng thái.")
                    .open();
            return;
        }

        dialogs.createInputDialog(this)
                .withHeader("Cập nhật trạng thái")
                .withParameters(
                        InputParameter.enumParameter("trangThai", TrangThaiPhieuDT.class)
                                .withLabel("Trạng thái")
                                .withDefaultValue(currentPhieuDieuTri.getTrangThai())
                                .withRequired(true)
                )
                .withActions(DialogActions.OK_CANCEL)
                .withCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(DialogOutcome.OK)) {
                        TrangThaiPhieuDT newTrangThai = closeEvent.getValue("trangThai");
                        if (newTrangThai != null) {
                            currentPhieuDieuTri.setTrangThai(newTrangThai);
                            dataManager.save(currentPhieuDieuTri);
                            updateTrangThaiBox(newTrangThai);
                        }
                    }
                })
                .open();
    }

    private void updateTrangThaiBox(TrangThaiPhieuDT trangThai) {
        trangThaiBox.removeAll();

        Span label = uiComponents.create(Span.class);
        label.setText("Trạng thái: ");
        label.addClassName("trang-thai-label");

        Span span = uiComponents.create(Span.class);
        span.setText(messages.getMessage(trangThai));
        span.addClassName(trangThai.toString());

        trangThaiBox.add(label, span);
    }
}