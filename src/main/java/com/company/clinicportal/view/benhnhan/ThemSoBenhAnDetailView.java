package com.company.clinicportal.view.benhnhan;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "them-so-benh-ans/:id", layout = MainView.class)
@ViewController(id = "ThemSoBenhAn.detail")
@ViewDescriptor(path = "them-so-benh-an-detail-view.xml")
@EditedEntityContainer("benhNhanDc")
@DialogMode(height = "100%", width = "80%")
public class ThemSoBenhAnDetailView extends StandardDetailView<BenhNhan> {
    @Autowired
    private Metadata metadata;
    @ViewComponent
    private EntityComboBox<BenhNhan> benhNhanField;
    @Autowired
    private DataManager dataManager;

    @Subscribe("saveAction")
    public void onSaveAction(final ActionPerformedEvent event) {
        BenhNhan copy = metadata.create(BenhNhan.class);
        BeanUtils.copyProperties(benhNhanField.getValue(), copy, "id", "version", "createdBy", "lastModifiedBy", "createTs", "updateTs");
        // Xoá ID để đảm bảo đây là entity mới
        EntityValues.setId(copy, null);
        dataManager.save(copy);
        this.closeWithDiscard();
    }
}