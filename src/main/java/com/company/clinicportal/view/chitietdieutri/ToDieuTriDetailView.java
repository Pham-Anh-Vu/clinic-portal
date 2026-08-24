package com.company.clinicportal.view.chitietdieutri;

import com.company.clinicportal.entity.NhanSu;
import com.company.clinicportal.entity.ToDieuTri;
import com.company.clinicportal.entity.ToDieuTriKyThuat;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "to-dieu-tris/:id", layout = MainView.class)
@ViewController(id = "ToDieuTri.detail")
@ViewDescriptor(path = "to-dieu-tri-detail-view.xml")
@EditedEntityContainer("toDieuTriDc")
@DialogMode(width = "50em", height = "AUTO")
public class ToDieuTriDetailView extends StandardDetailView<ToDieuTri> {

    @Autowired
    private Metadata metadata;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private DialogWindows dialogWindows;

    @ViewComponent
    private DataGrid<ToDieuTriKyThuat> kyThuatListDataGrid;
    @ViewComponent
    private CollectionPropertyContainer<ToDieuTriKyThuat> kyThuatListDc;

    private boolean kyThuatActionColumnsConfigured;
    @ViewComponent
    private EntityComboBox<NhanSu> idNguoiThucHienField;
    @Autowired
    private DataManager dataManager;
    @ViewComponent
    private EntityComboBox<NhanSu> idBacSiChiDinhField;

    @Subscribe
    public void onInit(final InitEvent event) {
        configureKyThuatActionColumns();
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        //KTV được phân công
        NhanSu ktvPhanCong = dataManager.load(NhanSu.class).id(15).optional().orElse(null);
        idNguoiThucHienField.setValue(ktvPhanCong);

        //BS Đặng Thị Hà
        NhanSu bsChiDinh = dataManager.load(NhanSu.class).id(1).optional().orElse(null);
        idBacSiChiDinhField.setValue(bsChiDinh);
    }



    @Install(to = "kyThuatListDataGrid.create", subject = "newEntitySupplier")
    private ToDieuTriKyThuat kyThuatListDataGridCreateNewEntitySupplier() {
        ToDieuTriKyThuat kyThuat = metadata.create(ToDieuTriKyThuat.class);
        kyThuat.setToDieuTri(getEditedEntity());
        return kyThuat;
    }

    private void configureKyThuatActionColumns() {
        if (kyThuatActionColumnsConfigured) {
            return;
        }
        Grid.Column<ToDieuTriKyThuat> thaoTacColumn = kyThuatListDataGrid.getColumnByKey("thaoTacColumn");
        if (thaoTacColumn != null) {
            thaoTacColumn.setRenderer(new ComponentRenderer<>(kyThuat -> {
                HorizontalLayout layout = uiComponents.create(HorizontalLayout.class);
                layout.setSpacing(true);

                JmixButton editButton = uiComponents.create(JmixButton.class);
                editButton.setIcon(VaadinIcon.EDIT.create());
                editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
                editButton.addClickListener(e -> openKyThuatDetail(kyThuat));

                JmixButton deleteButton = uiComponents.create(JmixButton.class);
                Icon trashIcon = VaadinIcon.TRASH.create();
                trashIcon.setColor("var(--lumo-error-text-color)");
                deleteButton.setIcon(trashIcon);
                deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
                deleteButton.addClickListener(e -> kyThuatListDc.getMutableItems().remove(kyThuat));

                layout.add(editButton, deleteButton);
                return layout;
            }));
            thaoTacColumn.setAutoWidth(true);
            thaoTacColumn.setFlexGrow(0);
        }
        kyThuatActionColumnsConfigured = true;
    }

    private void openKyThuatDetail(ToDieuTriKyThuat kyThuat) {
        DialogWindow<ToDieuTriKyThuatDetailView> window = dialogWindows.detail(this, ToDieuTriKyThuat.class)
                .withViewClass(ToDieuTriKyThuatDetailView.class)
                .editEntity(kyThuat)
                .withContainer(kyThuatListDc)
                .build();
        window.open();
    }
}
