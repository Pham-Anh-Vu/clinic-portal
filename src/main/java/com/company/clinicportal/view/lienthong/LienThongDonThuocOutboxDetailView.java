package com.company.clinicportal.view.lienthong;

import com.company.clinicportal.lienthong.LienThongOutboxService;
import com.company.clinicportal.lienthong.entity.LienThongDonThuocOutbox;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Date;

/**
 * Read-only detail cho 1 outbox row + nút Retry ngay tại đây.
 */
@Route(value = "lienthong/don-thuoc-outbox/:id", layout = MainView.class)
@ViewController(id = "ltcs_LienThongDonThuocOutbox.detail")
@ViewDescriptor(path = "lien-thong-don-thuoc-outbox-detail-view.xml")
@EditedEntityContainer("lienThongDonThuocOutboxDc")
public class LienThongDonThuocOutboxDetailView extends StandardDetailView<LienThongDonThuocOutbox> {

    @Autowired
    private DataManager dataManager;
    @Autowired
    private Notifications notifications;

    @ViewComponent
    private JmixButton retryBtn;

    @Subscribe
    public void onInit(InitEvent event) {
        if (retryBtn != null) {
            retryBtn.addClickListener(e -> onRetry());
        }
    }

    private void onRetry() {
        LienThongDonThuocOutbox row = getEditedEntity();
        if (row == null) return;
        row.setStatus(LienThongOutboxService.STATUS_PENDING);
        row.setAttempt(0);
        row.setNextAttemptAt(Date.from(Instant.now()));
        row.setLastError(null);
        row.setUpdatedAt(Date.from(Instant.now()));
        row.setLockedAt(null);
        row.setLockedBy(null);
        dataManager.save(row);
        notifications.create("Đã đặt lại job.")
                .withType(Notifications.Type.SUCCESS).show();
    }
}