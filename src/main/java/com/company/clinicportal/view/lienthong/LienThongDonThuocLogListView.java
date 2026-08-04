package com.company.clinicportal.view.lienthong;

import com.company.clinicportal.lienthong.entity.LienThongDonThuocLog;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "lienthong/don-thuoc-log", layout = MainView.class)
@ViewController(id = "ltcs_LienThongDonThuocLog.list")
@ViewDescriptor(path = "lien-thong-don-thuoc-log-list-view.xml")
@LookupComponent("lienThongDonThuocLogsDataGrid")
@DialogMode(width = "80em")
public class LienThongDonThuocLogListView extends StandardListView<LienThongDonThuocLog> {
}
