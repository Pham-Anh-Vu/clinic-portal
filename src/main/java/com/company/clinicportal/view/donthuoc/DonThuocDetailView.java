package com.company.clinicportal.view.donthuoc;

import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.entity.DonThuocChanDoan;
import com.company.clinicportal.entity.DonThuocChiTiet;
import com.company.clinicportal.entity.DonThuocDotDung;
import com.company.clinicportal.entity.PhieuDieuTri;
import com.company.clinicportal.enumentity.TrangThaiDonThuoc;
import com.company.clinicportal.security.DuocSiRole;
import com.company.clinicportal.service.DonThuocService;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Editor chính cho kê đơn thuốc liên thông 808/QĐ-BYT.
 *
 * Hỗ trợ 2 cách mở:
 * 1. Có :id → load đơn đã có (sửa/xem).
 * 2. Có param {@code phieuDieuTriId} → tạo nháp mới từ phiếu khám.
 *
 * <p>Quy tắc khoá: khi đơn đã gửi / phát hành / huỷ, view chuyển sang read-only.</p>
 */
@Route(value = "don-thuocs/:id", layout = MainView.class)
@ViewController(id = "ltcs_DonThuoc.detail")
@ViewDescriptor(path = "don-thuoc-detail-view.xml")
@EditedEntityContainer("donThuocDc")
public class DonThuocDetailView extends StandardDetailView<DonThuoc> {

    @Autowired
    private DataManager dataManager;
    @Autowired
    private DonThuocService donThuocService;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private CurrentAuthentication currentAuthentication;

    @ViewComponent
    private DataGrid<DonThuocChiTiet> chiTietDataGrid;
    @ViewComponent
    private DataGrid<DonThuocChanDoan> chanDoanDataGrid;
    @ViewComponent
    private DataGrid<DonThuocDotDung> dotDungDataGrid;
    @ViewComponent
    private CollectionLoader<DonThuocChiTiet> chiTietsDl;
    @ViewComponent
    private CollectionLoader<DonThuocChanDoan> chanDoansDl;
    @ViewComponent
    private CollectionLoader<DonThuocDotDung> dotDungsDl;
    @ViewComponent
    private JmixButton addThuocButton;
    @ViewComponent
    private JmixButton removeThuocButton;
    @ViewComponent
    private JmixButton addChanDoanButton;
    @ViewComponent
    private JmixButton removeChanDoanButton;
    @ViewComponent
    private JmixButton addDotDungButton;
    @ViewComponent
    private JmixButton removeDotDungButton;
    @ViewComponent
    private JmixButton issueButton;
    @ViewComponent
    private JmixButton cancelButton;
    @ViewComponent
    private JmixButton previewButton;
    @ViewComponent
    private InstanceContainer<DonThuoc> donThuocDc;
    @ViewComponent
    private io.jmix.flowui.model.CollectionContainer<DonThuocChiTiet> chiTietsDc;
    @ViewComponent
    private io.jmix.flowui.model.CollectionContainer<DonThuocChanDoan> chanDoansDc;
    @ViewComponent
    private io.jmix.flowui.model.CollectionContainer<DonThuocDotDung> dotDungsDc;

    private Long phieuDieuTriIdParam;

    /** Lấy số thứ tự tiếp theo cho dòng thuốc/chẩn đoán/đợt dùng. */
    private static <T> int nextStt(io.jmix.flowui.model.CollectionContainer<T> dc) {
        return dc == null ? 1 : dc.getItems().size() + 1;
    }

    public void setPhieuDieuTriIdParam(Long id) {
        this.phieuDieuTriIdParam = id;
    }

    @Subscribe
    public void onInit(InitEvent event) {
        addThuocButton.addClickListener(e -> donThuocService.addEmptyDrugLine(getEditedEntity(),
                nextStt(chiTietsDc)));
        removeThuocButton.addClickListener(e -> removeSelected(chiTietDataGrid, chiTietsDl, "Chọn dòng thuốc để xoá"));
        addChanDoanButton.addClickListener(e -> donThuocService.addDiagnosis(getEditedEntity(), null,
                nextStt(chanDoansDc), null));
        removeChanDoanButton.addClickListener(e -> removeSelected(chanDoanDataGrid, chanDoansDl, "Chọn chẩn đoán để xoá"));
        addDotDungButton.addClickListener(e -> {
            com.company.clinicportal.entity.DonThuocDotDung dd = dataManager.create(com.company.clinicportal.entity.DonThuocDotDung.class);
            dd.setDonThuoc(getEditedEntity());
            dd.setSoDot(nextStt(dotDungsDc));
            dataManager.save(new SaveContext().saving(dd));
            dotDungsDl.load();
        });
        removeDotDungButton.addClickListener(e -> removeSelected(dotDungDataGrid, dotDungsDl, "Chọn đợt dùng để xoá"));
        issueButton.addClickListener(e -> onIssue());
        cancelButton.addClickListener(e -> onCancelDon());
        previewButton.addClickListener(e -> onPreview());
    }

    private void onPreview() {
        // Lưu trước khi preview để đảm bảo dữ liệu cập nhật
        try {
            donThuocService.saveDraft(getEditedEntity());
        } catch (Exception ex) {
            notifications.create("Lỗi khi lưu: " + ex.getMessage()).withType(Notifications.Type.WARNING).show();
            return;
        }
        // Load lại full fetchPlan cho preview
        DonThuoc fresh = dataManager.load(DonThuoc.class)
                .id(getEditedEntity().getId())
                .fetchPlan(builder -> builder.add("chiTiets", b -> b.add("stt"))
                        .add("chanDoans").add("dotDungs"))
                .one();
        dialogWindows.view(this, DonThuocPreviewDialogView.class)
                .withViewConfigurer(v -> ((DonThuocPreviewDialogView) v).setDonThuoc(fresh))
                .open();
    }

    /**
     * Khi user chọn DmThuoc trong cột entityPicker, tự điền các trường snapshot.
     */
    @Subscribe("chiTietDataGrid.dmThuoc")
    public void onChiTietDmThuocValueChange(com.vaadin.flow.component.HasValue.ValueChangeEvent<com.company.clinicportal.entity.DmThuoc> e) {
        if (e.isFromClient()) {
            try {
                com.company.clinicportal.entity.DonThuocChiTiet editing = chiTietDataGrid.getSingleSelectedItem();
                if (editing != null) {
                    donThuocService.pickDrug(editing, e.getValue());
                    chiTietsDl.load();
                }
            } catch (IllegalStateException ex) {
                notifications.create(ex.getMessage()).withType(Notifications.Type.WARNING).show();
            }
        }
    }

    /**
     * Khi user chọn Icd10 trong cột entityPicker, tự điền các trường snapshot.
     */
    @Subscribe("chanDoanDataGrid.icd10")
    public void onChanDoanIcd10ValueChange(com.vaadin.flow.component.HasValue.ValueChangeEvent<com.company.clinicportal.entity.Icd10> e) {
        if (e.isFromClient()) {
            try {
                com.company.clinicportal.entity.DonThuocChanDoan editing = chanDoanDataGrid.getSingleSelectedItem();
                if (editing != null) {
                    donThuocService.pickDiagnosis(editing, e.getValue());
                    chanDoansDl.load();
                }
            } catch (IllegalStateException ex) {
                notifications.create(ex.getMessage()).withType(Notifications.Type.WARNING).show();
            }
        }
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (phieuDieuTriIdParam != null && getEditedEntity() != null && getEditedEntity().getId() == null) {
            PhieuDieuTri pdt = dataManager.load(PhieuDieuTri.class).id(phieuDieuTriIdParam).one();
            DonThuoc draft = donThuocService.createDraft(pdt, null, null);
            donThuocDc.setItem(draft);
        }
    }

    @Subscribe
    public void onReady(io.jmix.flowui.view.View.ReadyEvent event) {
        if (getEditedEntity() != null && getEditedEntity().getId() != null) {
            chiTietsDl.setParameter("donThuoc", getEditedEntity());
            chanDoansDl.setParameter("donThuoc", getEditedEntity());
            dotDungsDl.setParameter("donThuoc", getEditedEntity());
            chiTietsDl.load();
            chanDoansDl.load();
            dotDungsDl.load();
        }
        applyReadOnlyUI();
    }

    private void applyReadOnlyUI() {
        DonThuoc e = getEditedEntity();
        if (e == null) return;
        TrangThaiDonThuoc st = e.getTrangThaiEnum();
        boolean lockedByState = st == TrangThaiDonThuoc.DA_GUI
                || st == TrangThaiDonThuoc.PHAT_HANH
                || st == TrangThaiDonThuoc.HUY;
        boolean readonlyByRole = isDuocSiOnly();
        boolean locked = lockedByState || readonlyByRole;
        setReadOnly(locked);
        if (issueButton != null) issueButton.setEnabled(!locked);
        if (cancelButton != null) cancelButton.setEnabled(!locked && !readonlyByRole && st != TrangThaiDonThuoc.HUY);
        if (addThuocButton != null) addThuocButton.setEnabled(!locked);
        if (removeThuocButton != null) removeThuocButton.setEnabled(!locked);
        if (addChanDoanButton != null) addChanDoanButton.setEnabled(!locked);
        if (removeChanDoanButton != null) removeChanDoanButton.setEnabled(!locked);
        if (addDotDungButton != null) addDotDungButton.setEnabled(!locked);
        if (removeDotDungButton != null) removeDotDungButton.setEnabled(!locked);
        if (previewButton != null) previewButton.setEnabled(true); // Dược sĩ vẫn được preview
    }

    /**
     * User hiện tại có vai trò dược sĩ (chỉ xem) hay không.
     */
    private boolean isDuocSiOnly() {
        try {
            String code = currentAuthentication.getAuthentication().getAuthorities().stream()
                    .filter(a -> a.getAuthority() != null && a.getAuthority().startsWith("role:"))
                    .map(a -> a.getAuthority().substring("role:".length()))
                    .findAny().orElse(null);
            // Chỉ dược sĩ (không phải admin/bác sĩ) thì mới khóa.
            if (code == null) return false;
            return code.equals(DuocSiRole.CODE);
        } catch (Exception e) {
            return false;
        }
    }

    private void addEmptyDotDung() {
        DonThuocDotDung dd = dataManager.create(DonThuocDotDung.class);
        dd.setDonThuoc(getEditedEntity());
        dd.setSoDot(nextStt(dotDungsDc));
        dataManager.save(new SaveContext().saving(dd));
        dotDungsDl.load();
    }

    private <T> void removeSelected(DataGrid<T> grid, CollectionLoader<T> loader, String warnMessage) {
        T sel = grid.getSingleSelectedItem();
        if (sel == null) {
            notifications.create(warnMessage).withType(Notifications.Type.WARNING).show();
            return;
        }
        dataManager.remove(sel);
        loader.load();
    }

    private void onIssue() {
        try {
            donThuocService.saveDraft(getEditedEntity());
        } catch (Exception ex) {
            throw new ValidationException("Lỗi khi lưu đơn: " + ex.getMessage());
        }
        List<String> errors = donThuocService.validateForIssue(getEditedEntity());
        if (!errors.isEmpty()) {
            throw new ValidationException("Đơn chưa đủ điều kiện phát hành:\n- " + String.join("\n- ", errors));
        }
        // Lưu lần cuối trước khi enqueue outbox
        donThuocService.saveDraft(getEditedEntity());
        try {
            com.company.clinicportal.lienthong.entity.LienThongDonThuocOutbox job =
                    donThuocService.submitForSending(getEditedEntity());
            if (job != null) {
                notifications.create("Đã đưa vào hàng chờ gửi. Mã: " + job.getMaDonThuoc())
                        .withType(Notifications.Type.SUCCESS).show();
            } else {
                notifications.create("Đơn đã lưu (test mode / outbox chưa sẵn sàng). Đã bỏ qua bước gửi liên thông.")
                        .withType(Notifications.Type.WARNING).show();
            }
        } catch (Exception ex) {
            throw new ValidationException("Lỗi enqueue: " + ex.getMessage());
        }
        applyReadOnlyUI();
    }

    private void onCancelDon() {
        donThuocService.cancel(getEditedEntity(), "Bác sĩ huỷ đơn");
        notifications.create("Đã huỷ đơn").withType(Notifications.Type.SUCCESS).show();
        applyReadOnlyUI();
    }
}
