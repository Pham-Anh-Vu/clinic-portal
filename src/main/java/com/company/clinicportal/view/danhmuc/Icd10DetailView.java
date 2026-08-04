package com.company.clinicportal.view.danhmuc;

import com.company.clinicportal.entity.Icd10;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "danhmuc/icd10/:id", layout = MainView.class)
@ViewController(id = "ltcs_Icd10.detail")
@ViewDescriptor(path = "icd10-detail-view.xml")
@EditedEntityContainer("icd10Dc")
public class Icd10DetailView extends StandardDetailView<Icd10> {
}
