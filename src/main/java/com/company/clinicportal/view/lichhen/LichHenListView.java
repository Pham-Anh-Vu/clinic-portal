package com.company.clinicportal.view.lichhen;

import com.company.clinicportal.entity.LichHen;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;

import java.time.LocalDate;
import java.util.Date;


@Route(value = "lich-hens", layout = MainView.class)
@ViewController(id = "LichHen.list")
@ViewDescriptor(path = "lich-hen-list-view.xml")
@LookupComponent("lichHensDataGrid")
@DialogMode(width = "64em")
public class LichHenListView extends StandardListView<LichHen> {
    @ViewComponent
    private CollectionLoader<LichHen> lichHensDl;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        lichHensDl.setParameter("currentDate", new Date());
        lichHensDl.load();
    }
}