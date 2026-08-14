package com.company.clinicportal.view.danhmuc;

import com.company.clinicportal.entity.DmThuoc;
import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.entity.DonThuocChiTiet;
import com.company.clinicportal.view.danhmuc.DanhMucImportDialogView;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.list.RemoveAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "dm-thuocs", layout = MainView.class)
@ViewController(id = "ltcs_DmThuoc.list")
@ViewDescriptor(path = "dm-thuoc-list-view.xml")
@LookupComponent("dmThuocsDataGrid")
@DialogMode(width = "80em")
public class DmThuocListView extends StandardListView<DmThuoc> {

    @ViewComponent
    private DataGrid<DmThuoc> dmThuocsDataGrid;
    @ViewComponent
    private CollectionLoader<DmThuoc> dmThuocsDl;
    @ViewComponent
    private JmixButton importButton;

    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private UiComponents uiComponents;

    @ViewComponent("dmThuocsDataGrid.removeAction")
    private RemoveAction<DmThuoc> dmThuocsDataGridRemoveAction;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Notifications notifications;
    @ViewComponent
    private MessageBundle messageBundle;

    @Subscribe
    public void onInit(InitEvent event) {
        dmThuocsDataGrid.addComponentColumn(dmThuoc -> {
            HorizontalLayout actionsLayout = uiComponents.create(HorizontalLayout.class);

            JmixButton editButton = uiComponents.create(JmixButton.class);
            editButton.setText("Cập nhật");
            editButton.addClickListener(e -> {
                DialogWindow<DmThuocDetailView> window = dialogWindows.detail(this, DmThuoc.class)
                        .editEntity(dmThuoc)
                        .withViewClass(DmThuocDetailView.class)
                        .build();
                window.addAfterCloseListener(e1 -> dmThuocsDl.load());
                window.open();
            });

            JmixButton deleteButton = uiComponents.create(JmixButton.class);
            deleteButton.setText("Xóa");
            deleteButton.addClickListener(e -> {
                DonThuocChiTiet donThuocChiTiet = dataManager.load(DonThuocChiTiet.class).query("select e from DonThuocChiTiet e where e.dmThuoc = :dmThuoc").parameter("dmThuoc", dmThuoc).optional().orElse(null);

                if(donThuocChiTiet != null){
                    notifications.create(messageBundle.getMessage("DonThuocChiTiet.deleted"))
                            .withThemeVariant(NotificationVariant.LUMO_WARNING)
                            .withPosition(Notification.Position.TOP_START)
                            .show();
                }

                dmThuocsDataGrid.select(dmThuoc);
                dmThuocsDataGridRemoveAction.execute();
            });

            actionsLayout.add(editButton);
            actionsLayout.add(deleteButton);
            return actionsLayout;
        })
        .setHeader("Thao tác")
        .setAutoWidth(true);
    }

    @Subscribe("importButton")
    public void onImportButtonClick(ClickEvent<JmixButton> event) {
        dialogWindows.view(this, DanhMucImportDialogView.class)
                .withViewConfigurer(view -> ((DanhMucImportDialogView) view).setKind(DanhMucImportDialogView.KIND_THUOC))
                .open()
                .addAfterCloseListener(closeEvent -> dmThuocsDl.load());
    }
}