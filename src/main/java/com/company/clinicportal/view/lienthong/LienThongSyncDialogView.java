package com.company.clinicportal.view.lienthong;

import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.enumentity.TrangThaiDonThuoc;
import com.company.clinicportal.lienthong.LienThongOutboxService;
import com.company.clinicportal.service.DonThuocService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import io.jmix.core.DataManager;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

/**
 * Dialog đồng bộ nhiều đơn thuốc 1 lần: tìm các đơn theo tiêu chí rồi enqueue
 * outbox. Hỗ trợ 3 mode:
 * <ul>
 *     <li>CHUA_GUI: đơn trạng thái NHAP (chưa bao giờ đưa vào outbox).</li>
 *     <li>BI_FAILED: đơn có outbox row ở FAILED/RETRY_SCHEDULED.</li>
 *     <li>DA_GUI_RETRY: đơn đã phát hành nhưng có lastError → retry.</li>
 * </ul>
 */
@ViewController(id = "ltcs_LienThongSync.dialog")
@ViewDescriptor(path = "lien-thong-sync-dialog-view.xml")
@DialogMode(width = "70em", height = "50em")
public class LienThongSyncDialogView extends StandardView {

    public enum Mode {CHUA_GUI, BI_FAILED, DA_GUI_RETRY, ALL}

    @Autowired
    private DataManager dataManager;
    @Autowired
    private DonThuocService donThuocService;
    @Autowired
    private LienThongOutboxService outboxService;
    @Autowired
    private Notifications notifications;

    @ViewComponent
    private ComboBox<Mode> modeCombo;
    @ViewComponent
    private CollectionLoader<DonThuoc> donThuocsDl;
    @ViewComponent
    private DataGrid<DonThuoc> donThuocsDataGrid;
    @ViewComponent
    private JmixButton scanBtn;
    @ViewComponent
    private JmixButton syncBtn;
    @ViewComponent
    private JmixButton syncAllBtn;

    @Subscribe
    public void onInit(InitEvent event) {
        if (modeCombo != null) {
            modeCombo.setItems(Mode.values());
            modeCombo.setValue(Mode.BI_FAILED);
        }
        if (scanBtn != null) scanBtn.addClickListener(e -> scan());
        if (syncBtn != null) syncBtn.addClickListener(e -> sync(false));
        if (syncAllBtn != null) syncAllBtn.addClickListener(e -> sync(true));
    }

    /**
     * Renderer hiển thị tiếng Việt cho cột "Trạng thái": chuyển {@code trangThai} (String ID như
     * {@code PHAT_HANH}, {@code CHO_GUI}) sang tên hiển thị của enum {@link TrangThaiDonThuoc}
     * (ví dụ: "Đã phát hành", "Chờ gửi"). Cột XML dùng {@code key="trangThaiLabel"} không bind
     * property — giá trị do renderer cung cấp.
     */
    @Supply(to = "donThuocsDataGrid.trangThaiLabel", subject = "renderer")
    private Renderer<DonThuoc> donThuocsDataGridTrangThaiLabelRenderer() {
        return new TextRenderer<>(dt -> {
            TrangThaiDonThuoc e = TrangThaiDonThuoc.fromId(dt.getTrangThai());
            return e == null ? dt.getTrangThai() : e.getTenHienThi();
        });
    }

    private void scan() {
        Mode m = modeCombo.getValue();
        if (m == null) m = Mode.BI_FAILED;
        switch (m) {
            case CHUA_GUI -> donThuocsDl.setQuery(
                    "select e from DonThuoc e where e.trangThai = 'NHAP' order by e.ngayKe desc");
            case BI_FAILED -> donThuocsDl.setQuery(
                    "select e from DonThuoc e where e.trangThai in ('NHAP','DA_GUI') and e.lastError is not null "
                            + "order by e.ngayKe desc");
            case DA_GUI_RETRY -> donThuocsDl.setQuery(
                    "select e from DonThuoc e where e.trangThai = 'DA_GUI' and e.lastError is not null order by e.ngayKe desc");
            case ALL -> donThuocsDl.setQuery("select e from DonThuoc e order by e.ngayKe desc");
        }
        donThuocsDl.load();
    }

    private void sync(boolean all) {
        Set<DonThuoc> rows = new HashSet<>();
        if (all) {
            rows.addAll(donThuocsDl.getContainer().getItems());
        } else {
            rows.addAll(donThuocsDataGrid.getSelectedItems());
        }
        if (rows.isEmpty()) {
            notifications.create("Không có đơn nào để đồng bộ.")
                    .withType(Notifications.Type.WARNING).show();
            return;
        }
        int ok = 0;
        int fail = 0;
        for (DonThuoc dt : rows) {
            try {
                outboxService.enqueueDonThuoc(dt, idempotencyKeyOf(dt));
                ok++;
            } catch (Exception ex) {
                fail++;
            }
        }
        notifications.create("Đã đưa " + ok + " đơn vào hàng chờ" + (fail > 0 ? "; lỗi " + fail : ""))
                .withType(fail == 0 ? Notifications.Type.SUCCESS : Notifications.Type.WARNING)
                .show();
        scan();
    }

    private String idempotencyKeyOf(DonThuoc dt) {
        if (dt.getIdempotencyKey() != null && !dt.getIdempotencyKey().isBlank()) {
            return dt.getIdempotencyKey();
        }
        // Tạo key mới - nếu đã có outbox row thì enqueueDonThuoc trả về row cũ.
        String k = "eco-" + System.currentTimeMillis() + "-" + Math.abs(dt.getMaDonThuoc().hashCode());
        dt.setIdempotencyKey(k);
        dataManager.save(dt);
        return k;
    }
}