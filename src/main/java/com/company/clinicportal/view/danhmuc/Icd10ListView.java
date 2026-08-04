package com.company.clinicportal.view.danhmuc;

import com.company.clinicportal.entity.Icd10;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "danhmuc/icd10", layout = MainView.class)
@ViewController(id = "ltcs_Icd10.list")
@ViewDescriptor(path = "icd10-list-view.xml")
@LookupComponent("icd10sDataGrid")
@DialogMode(width = "64em")
public class Icd10ListView extends StandardListView<Icd10> {

    @ViewComponent
    private DataGrid<Icd10> icd10sDataGrid;
    @ViewComponent
    private CollectionLoader<Icd10> icd10sDl;
    @ViewComponent
    private JmixButton importButton;

    @Autowired
    private DialogWindows dialogWindows;

    @Subscribe("importButton")
    public void onImportButtonClick(com.vaadin.flow.component.ClickEvent<JmixButton> event) {
        dialogWindows.view(this, DanhMucImportDialogView.class)
                .withViewConfigurer(view -> ((DanhMucImportDialogView) view).setKind(DanhMucImportDialogView.KIND_ICD))
                .open()
                .addAfterCloseListener(closeEvent -> icd10sDl.load());
    }
}
