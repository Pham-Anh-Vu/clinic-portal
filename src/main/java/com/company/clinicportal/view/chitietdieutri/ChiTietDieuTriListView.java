package com.company.clinicportal.view.chitietdieutri;

import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.view.buoidieutri.BuoiDieuTriListView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.flowui.model.CollectionLoader;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "chi-tiet-dieu-tris", layout = MainView.class)
@ViewController(id = "ChiTietDieuTri.list")
@ViewDescriptor(path = "chi-tiet-dieu-tri-list-view.xml")
@LookupComponent("chiTietDieuTrisDataGrid")
@DialogMode(width = "64em")
public class ChiTietDieuTriListView extends StandardListView<ChiTietDieuTri> {
    @ViewComponent
    private CollectionLoader<ChiTietDieuTri> chiTietDieuTrisDl;

    public Long idBenhNhan;
    @Autowired
    private DialogWindows dialogWindows;

    public void setIdBenhNhan(Long idBenhNhan) {
        this.idBenhNhan = idBenhNhan;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if(idBenhNhan != null){
            chiTietDieuTrisDl.setParameter("idBenhNhan", idBenhNhan);
            chiTietDieuTrisDl.load();
        }
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
            }
        });
        
        dialogWindow.open();
    }
}