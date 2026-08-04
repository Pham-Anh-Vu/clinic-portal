package com.company.clinicportal.view.lienthong;

import com.company.clinicportal.lienthong.entity.CoSoKhamChuaBenhLienThong;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "lienthong/cs-kcb", layout = MainView.class)
@ViewController(id = "ltcs_CoSoKhamChuaBenhLienThong.list")
@ViewDescriptor(path = "co-so-kcb-lien-thong-list-view.xml")
@LookupComponent("coSoKhamChuaBenhLienThongsDataGrid")
@DialogMode(width = "64em")
public class CoSoKhamChuaBenhLienThongListView extends StandardListView<CoSoKhamChuaBenhLienThong> {
}
