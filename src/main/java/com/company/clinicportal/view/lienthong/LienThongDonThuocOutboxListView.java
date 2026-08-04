package com.company.clinicportal.view.lienthong;

import com.company.clinicportal.lienthong.LienThongOutboxService;
import com.company.clinicportal.lienthong.entity.LienThongDonThuocOutbox;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Outbox list — mỗi row là 1 job gửi liên thông. Cho phép:
 * <ul>
 *     <li>Lọc theo status (PENDING / IN_PROGRESS / DONE / FAILED / RETRY_SCHEDULED).</li>
 *     <li>Lọc theo maDonThuoc.</li>
 *     <li>Retry 1 job bất kỳ (đặt lại attempt, nextAttemptAt=now).</li>
 *     <li>Bulk retry cho nhiều đơn đang FAILED.</li>
 * </ul>
 */
@Route(value = "lienthong/don-thuoc-outbox", layout = MainView.class)
@ViewController(id = "ltcs_LienThongDonThuocOutbox.list")
@ViewDescriptor(path = "lien-thong-don-thuoc-outbox-list-view.xml")
@LookupComponent("lienThongDonThuocOutboxesDataGrid")
@DialogMode(width = "100em")
public class LienThongDonThuocOutboxListView extends StandardListView<LienThongDonThuocOutbox> {

    @Autowired
    private DataManager dataManager;
    @Autowired
    private Notifications notifications;

    @ViewComponent
    private CollectionLoader<LienThongDonThuocOutbox> lienThongDonThuocOutboxesDl;
    @ViewComponent
    private ComboBox<String> statusFilter;
    @ViewComponent
    private JmixButton retryBtn;
    @ViewComponent
    private JmixButton bulkRetryBtn;
    @ViewComponent
    private io.jmix.flowui.component.grid.DataGrid<LienThongDonThuocOutbox> lienThongDonThuocOutboxesDataGrid;

    @Subscribe
    public void onInit(InitEvent event) {
        if (statusFilter != null) {
            statusFilter.setItems("ALL", "PENDING", "IN_PROGRESS", "DONE",
                    "FAILED", "RETRY_SCHEDULED");
            statusFilter.setValue("ALL");
            statusFilter.addValueChangeListener(e -> applyFilter());
        }
        if (retryBtn != null) {
            retryBtn.addClickListener(e -> onRetry());
        }
        if (bulkRetryBtn != null) {
            bulkRetryBtn.addClickListener(e -> onBulkRetry());
        }
    }

    private void applyFilter() {
        String s = statusFilter.getValue();
        if (s == null || "ALL".equals(s)) {
            lienThongDonThuocOutboxesDl.setQuery(
                    "select e from ltcs_LienThongDonThuocOutbox e order by e.nextAttemptAt desc");
        } else {
            lienThongDonThuocOutboxesDl.setQuery(
                    "select e from ltcs_LienThongDonThuocOutbox e where e.status = :s "
                            + "order by e.nextAttemptAt desc");
            lienThongDonThuocOutboxesDl.setParameter("s", s);
        }
    }

    private void onRetry() {
        LienThongDonThuocOutbox row = lienThongDonThuocOutboxesDataGrid.getSingleSelectedItem();
        if (row == null) {
            notifications.create("Chọn 1 job outbox để retry.")
                    .withType(Notifications.Type.WARNING).show();
            return;
        }
        resetForRetry(row);
        notifications.create("Đã đặt lại job. Worker sẽ xử lý trong vòng 30s.")
                .withType(Notifications.Type.SUCCESS).show();
    }

    private void onBulkRetry() {
        List<LienThongDonThuocOutbox> selected = lienThongDonThuocOutboxesDataGrid.getSelectedItems().stream().toList();
        if (selected.isEmpty()) {
            notifications.create("Chọn nhiều job outbox để retry.")
                    .withType(Notifications.Type.WARNING).show();
            return;
        }
        int count = 0;
        for (LienThongDonThuocOutbox r : selected) {
            if ("FAILED".equals(r.getStatus()) || "RETRY_SCHEDULED".equals(r.getStatus())) {
                resetForRetry(r);
                count++;
            }
        }
        notifications.create("Đã đặt lại " + count + " job.")
                .withType(count > 0 ? Notifications.Type.SUCCESS : Notifications.Type.WARNING)
                .show();
    }

    private void resetForRetry(LienThongDonThuocOutbox row) {
        row.setStatus(LienThongOutboxService.STATUS_PENDING);
        row.setAttempt(0);
        row.setNextAttemptAt(Date.from(Instant.now()));
        row.setLastError(null);
        row.setUpdatedAt(Date.from(Instant.now()));
        row.setLockedAt(null);
        row.setLockedBy(null);
        dataManager.save(row);
    }
}