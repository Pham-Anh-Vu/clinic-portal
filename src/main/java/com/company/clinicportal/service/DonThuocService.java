package com.company.clinicportal.service;

import com.company.clinicportal.entity.*;
import com.company.clinicportal.enumentity.LoaiDon;
import com.company.clinicportal.enumentity.TrangThaiDonThuoc;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.security.CurrentAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Service cốt lõi cho kê đơn thuốc: tạo nháp, snapshot bệnh nhân, validate trước khi phát hành.
 *
 * <p>Trạng thái vòng đời (xem {@link TrangThaiDonThuoc}):</p>
 * <ul>
 *     <li>NHAP: bác sĩ đang nhập, có thể sửa.</li>
 *     <li>CHO_GUI: đã dùng để gửi liên thông (Giai đoạn 4).</li>
 *     <li>DA_GUI / GUI_LOI / PHAT_HANH / TU_CHOI: trạng thái từ API.</li>
 *     <li>HUY: bác sĩ huỷ đơn trước khi gửi.</li>
 * </ul>
 */
@Service
public class DonThuocService {

    private static final Logger log = LoggerFactory.getLogger(DonThuocService.class);

    @Autowired
    private DataManager dataManager;
    @Autowired
    private DonThuocCodeGenerator codeGenerator;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired(required = false)
    private ObjectProvider<com.company.clinicportal.lienthong.LienThongOutboxService> outboxServiceProvider;

    /**
     * Cờ TEST MODE — khi bật, "Lưu & phát hành" sẽ KHÔNG enqueue outbox (không gọi API liên thông).
     * Mặc định false. Có thể bật bằng:
     *   - JVM: -Dclinicportal.donthuoc.test-mode=true
     *   - application.properties: clinicportal.donthuoc.test-mode=true
     * Dùng để test business logic + validate ở local mà không cần token cơ sở/mạng tới donthuocquocgia.vn.
     */
    @org.springframework.beans.factory.annotation.Value("${clinicportal.donthuoc.test-mode:false}")
    private boolean testMode;

    /**
     * Tạo nháp đơn thuốc mới cho bệnh nhân theo phiên khám (PhieuDieuTri).
     * Snapshot toàn bộ thông tin bệnh nhân + bác sĩ + cơ sở; không tham chiếu sống.
     */
    @Transactional
    public DonThuoc createDraft(PhieuDieuTri phieuDieuTri, NhanSu bacSi, String maCoSoKcb) {
        if (phieuDieuTri == null) {
            throw new IllegalArgumentException("phieuDieuTri bắt buộc");
        }
        BenhNhan bn = phieuDieuTri.getIdBenhNhan();
        if (bn == null) {
            throw new IllegalArgumentException("PhieuDieuTri chưa có bệnh nhân");
        }
        DonThuoc dt = dataManager.create(DonThuoc.class);
        dt.setMaDonThuoc(codeGenerator.nextMaDonThuoc());
        dt.setIdempotencyKey(codeGenerator.newIdempotencyKey());
        dt.setBenhNhan(bn);
        dt.setPhieuDieuTri(phieuDieuTri);
        snapshotBenhNhan(dt, bn);
        snapshotBacSi(dt, bacSi);
        dt.setMaCoSoKcb(maCoSoKcb);
        dt.setNgayKe(codeGenerator.now());
        dt.setTrangThaiEnum(TrangThaiDonThuoc.NHAP);
        dt.setLoaiDonEnum(LoaiDon.THUONG);
        dt.setHinhThucDieuTriEnum(
                com.company.clinicportal.enumentity.HinhThucDieuTri.NGOAI_TRU);
        dt.setApiResponse(null);
        dt.setRetryCount(0);

        SaveContext ctx = new SaveContext().saving(dt);
        dataManager.save(ctx);
        return dt;
    }

    /**
     * Tạo nháp đơn thuốc nhanh từ {@link ChiTietDieuTri} — dùng cho nút "Thêm mới đơn thuốc"
     * ngay trong màn chi tiết phiếu điều trị (trong sổ bệnh án).
     *
     * <p>Các trường auto-fill:</p>
     * <ul>
     *     <li>{@code chanDoanText} &larr; {@code ChiTietDieuTri.chuanDoan} (Chuẩn đoán)</li>
     *     <li>{@code hinhThucDieuTri} = NGOAI_TRU (Ngoại trú)</li>
     *     <li>{@code ngayKe} = hôm nay</li>
     *     <li>{@code tenBacSi} = {@code tenBacSi} truyền vào (mặc định "BS. Đặng Thị Hà")</li>
     * </ul>
     */
    @Transactional
    public DonThuoc createDraftForChiTietDieuTri(ChiTietDieuTri chiTietDieuTri, String tenBacSi) {
        if (chiTietDieuTri == null) {
            throw new IllegalArgumentException("chiTietDieuTri bắt buộc");
        }
        PhieuDieuTri phieuDieuTri = chiTietDieuTri.getIdPhieuDieuTri();
        BenhNhan bn = chiTietDieuTri.getIdBenhNhan();
        if (bn == null && phieuDieuTri != null) {
            bn = phieuDieuTri.getIdBenhNhan();
        }
        if (bn == null) {
            throw new IllegalArgumentException("ChiTietDieuTri chưa có bệnh nhân");
        }

        DonThuoc dt = dataManager.create(DonThuoc.class);
        dt.setMaDonThuoc(codeGenerator.nextMaDonThuoc());
        dt.setIdempotencyKey(codeGenerator.newIdempotencyKey());
        dt.setBenhNhan(bn);
        dt.setPhieuDieuTri(phieuDieuTri);
        dt.setChiTietDieuTri(chiTietDieuTri);
        snapshotBenhNhan(dt, bn);
        dt.setTenBacSi(tenBacSi);
        // maLienThongBacSi, soCCHN sẽ được mapper sau (Giai đoạn 4) khi có mã liên thông chính thức.
        dt.setMaCoSoKcb(null);
        dt.setNgayKe(codeGenerator.now());
        dt.setTrangThaiEnum(TrangThaiDonThuoc.NHAP);
        dt.setLoaiDonEnum(LoaiDon.THUONG);
        dt.setHinhThucDieuTriEnum(
                com.company.clinicportal.enumentity.HinhThucDieuTri.NGOAI_TRU);
        dt.setApiResponse(null);
        dt.setRetryCount(0);

        // Auto-fill Chuẩn đoán từ Chi tiết phiếu điều trị.
        String chuanDoan = chiTietDieuTri.getChuanDoan();
        if (chuanDoan != null && !chuanDoan.isBlank()) {
            dt.setChanDoanText(chuanDoan.trim());
        }

        SaveContext ctx = new SaveContext().saving(dt);
        dataManager.save(ctx);
        return dt;
    }

    /**
     * Lưu nháp (chưa phát hành): cập nhật các trường bệnh nhân tự thay đổi, dòng thuốc, chẩn đoán, đợt dùng.
     * KHÔNG đụng vào thuốc snapshot khi bệnh nhân/ICD/thuốc đã rời; ta chỉ lưu các thay đổi trên chính đơn.
     * Tự động cập nhật số lượng thuốc/số đợt dùng.
     */
    @Transactional
    public DonThuoc saveDraft(DonThuoc dt) {
        if (dt == null) throw new IllegalArgumentException("dt null");
        if (dt.getTrangThaiEnum() == TrangThaiDonThuoc.DA_GUI
                || dt.getTrangThaiEnum() == TrangThaiDonThuoc.PHAT_HANH) {
            throw new IllegalStateException("Đơn đã gửi, không thể sửa. Hãy kê đơn mới.");
        }
        dt.setRetryCount(dt.getRetryCount() == null ? 0 : dt.getRetryCount());
        if (dt.getChiTiets() != null) {
            int count = 0;
            for (DonThuocChiTiet ct : dt.getChiTiets()) {
                if (ct != null && ct.getMaThuocSnapshot() != null && !ct.getMaThuocSnapshot().isBlank()) {
                    count++;
                }
            }
            dt.setSoLuongThuocTrongDon(count);
        }
        if (dt.getDotDungs() != null) {
            int dots = 0;
            for (DonThuocDotDung dd : dt.getDotDungs()) {
                if (dd != null && dd.getSoDot() != null) dots++;
            }
            dt.setSoDotDung(dots);
        }
        SaveContext ctx = new SaveContext().saving(dt);
        dataManager.save(ctx);
        return dt;
    }

    /**
     * Thêm dòng thuốc vào đơn (chỉ pick từ danh mục, tự snapshot).
     */
    @Transactional
    public DonThuocChiTiet addDrugLine(DonThuoc dt, DmThuoc dmThuoc, Integer stt) {
        if (dt == null) throw new IllegalArgumentException("dt null");
        if (dmThuoc == null) {
            // Không có tham chiếu danh mục → cho phép dòng rỗng
            DonThuocChiTiet empty = dataManager.create(DonThuocChiTiet.class);
            empty.setDonThuoc(dt);
            empty.setStt(stt);
            dataManager.save(new SaveContext().saving(empty));
            return empty;
        }
        // Không cho phép trùng thuốc trong cùng đơn (theo mã thuốc)
        Long dup = dataManager.loadValue(
                        "select count(e) from DonThuocChiTiet e where e.donThuoc = :dt and e.maThuocSnapshot = :ma and e.maThuocSnapshot is not null",
                        Long.class)
                .parameter("dt", dt)
                .parameter("ma", dmThuoc.getMaThuoc())
                .one();
        if (dup != null && dup > 0L) {
            throw new IllegalStateException("Thuốc " + dmThuoc.getMaThuoc() + " đã có trong đơn. Hãy tăng số lượng hoặc chọn thuốc khác.");
        }
        DonThuocChiTiet ct = dataManager.create(DonThuocChiTiet.class);
        ct.setDonThuoc(dt);
        ct.setStt(stt);
        ct.snapshotFrom(dmThuoc);
        dataManager.save(new SaveContext().saving(ct));
        return ct;
    }

    /**
     * Thêm dòng thuốc rỗng (chưa chọn từ danh mục). Dùng cho UI editor kiểu "Thêm thuốc".
     */
    @Transactional
    public DonThuocChiTiet addEmptyDrugLine(DonThuoc dt, Integer stt) {
        return addDrugLine(dt, null, stt);
    }

    /**
     * Khi user chọn một DmThuoc qua entityPicker trong dòng thuốc đã có:
     * - Gọi {@link DonThuocChiTiet#snapshotFrom(DmThuoc)} để điền các trường snapshot.
     * - Bỏ qua nếu đã trùng mã với dòng khác.
     */
    @Transactional
    /**
     * Map thông tin từ DmThuoc vào {@code ct} (snapshot). UI refresh do controller
     * sau khi gọi hàm này bằng cách container.setItem(ct) để re-fire events.
     */
    public DonThuocChiTiet pickDrug(DonThuocChiTiet ct, DmThuoc dm) {
        if (ct == null) throw new IllegalArgumentException("ct null");
        if (dm == null) return ct;
        Long dup = dataManager.loadValue(
                        "select count(e) from DonThuocChiTiet e where e.donThuoc = :dt and e.maThuocSnapshot = :ma and e.id <> :self and e.maThuocSnapshot is not null",
                        Long.class)
                .parameter("dt", ct.getDonThuoc())
                .parameter("ma", dm.getMaThuoc())
                .parameter("self", ct.getId())
                .one();
        if (dup != null && dup > 0L) {
            throw new IllegalStateException("Thuốc " + dm.getMaThuoc() + " đã có trong đơn.");
        }
        ct.snapshotFrom(dm);
        return ct;
    }

    @Transactional
    public DonThuoc removeDrugLine(DonThuocChiTiet ct) {
        if (ct == null) return null;
        dataManager.remove(ct);
        return ct.getDonThuoc();
    }

    @Transactional
    public DonThuocChanDoan addDiagnosis(DonThuoc dt, Icd10 icd, Integer stt, String ketLuan) {
        DonThuocChanDoan cd = dataManager.create(DonThuocChanDoan.class);
        cd.setDonThuoc(dt);
        cd.setStt(stt);
        if (icd != null) {
            cd.snapshotFrom(icd);
        }
        cd.setKetLuan(ketLuan);
        dataManager.save(new SaveContext().saving(cd));
        return cd;
    }

    /**
     * Khi user chọn một Icd10 qua entityPicker trong dòng chẩn đoán đã có.
     */
    @Transactional
    public DonThuocChanDoan pickDiagnosis(DonThuocChanDoan cd, Icd10 icd) {
        if (cd == null) throw new IllegalArgumentException("cd null");
        if (icd == null) return cd;
        cd.snapshotFrom(icd);
        dataManager.save(new SaveContext().saving(cd));
        return cd;
    }

    @Transactional
    public DonThuocDotDung addDotDung(DonThuoc dt, Integer soDot, Date tuNgay, Date denNgay, Integer soThangThuoc) {
        DonThuocDotDung dd = dataManager.create(DonThuocDotDung.class);
        dd.setDonThuoc(dt);
        dd.setSoDot(soDot);
        dd.setTuNgay(tuNgay);
        dd.setDenNgay(denNgay);
        dd.setSoThangThuoc(soThangThuoc);
        dataManager.save(new SaveContext().saving(dd));
        return dd;
    }

    /**
     * Validate trước khi phát hành. Phải có:
     * - Ít nhất 1 dòng thuốc, số lượng > 0
     * - Ít nhất 1 chẩn đoán
     * - Loại đơn hợp lệ
     * - Bệnh nhân snapshot đầy đủ (họ tên, ngày sinh, giới tính)
     */
    public List<String> validateForIssue(DonThuoc dt) {
        List<String> errors = new ArrayList<>();
        if (dt == null) {
            errors.add("don-thuoc-empty");
            return errors;
        }
        if (dt.getMaDonThuoc() == null || dt.getMaDonThuoc().isBlank()) {
            errors.add("maDonThuoc-required");
        }
        if (dt.getNgayKe() == null) {
            errors.add("ngayKe-required");
        }
        if (dt.getHoVaTenBenhNhan() == null || dt.getHoVaTenBenhNhan().isBlank()) {
            errors.add("hoVaTen-required");
        }
        if (dt.getNgaySinh() == null) {
            errors.add("ngaySinh-required");
        }
        if (dt.getGioiTinh() == null || dt.getGioiTinh().isBlank()) {
            errors.add("gioiTinh-required");
        }
        if (dt.getLoaiDon() == null) {
            errors.add("loaiDon-required");
        }
        if (dt.getHinhThucDieuTri() == null) {
            errors.add("hinhThucDieuTri-required");
        }
        List<DonThuocChiTiet> lines = dt.getChiTiets();
        if (lines == null || lines.isEmpty()) {
            errors.add("chiTiet-empty");
        } else {
            java.util.Set<String> seenMaThuoc = new java.util.HashSet<>();
            for (DonThuocChiTiet ct : lines) {
                if (ct.getMaThuocSnapshot() == null || ct.getMaThuocSnapshot().isBlank()) {
                    errors.add("chiTiet-ma-required");
                } else if (!seenMaThuoc.add(ct.getMaThuocSnapshot())) {
                    errors.add("chiTiet-trung-ma");
                    break;
                }
                if (ct.getSoLuong() == null
                        || ct.getSoLuong().signum() <= 0) {
                    errors.add("chiTiet-soLuong-invalid");
                }
            }
        }
        List<DonThuocChanDoan> cds = dt.getChanDoans();
        if (cds == null || cds.isEmpty()) {
            errors.add("chanDoan-empty");
        }
        return errors;
    }

    /**
     * Đánh dấu đơn đã gửi (khi outbox thực sự gọi API). Sets trạng thái + log tối thiểu.
     */
    @Transactional
    public DonThuoc markSent(DonThuoc dt, String apiRequestId, String apiResponse) {
        dt.setTrangThaiEnum(TrangThaiDonThuoc.DA_GUI);
        dt.setApiRequestId(apiRequestId);
        dt.setApiResponse(apiResponse);
        dt.setSentAt(codeGenerator.now());
        dt.setLastError(null);
        dataManager.save(new SaveContext().saving(dt));
        return dt;
    }

    @Transactional
    public DonThuoc markError(DonThuoc dt, String lastError) {
        dt.setTrangThaiEnum(TrangThaiDonThuoc.GUI_LOI);
        dt.setLastError(lastError);
        dt.setRetryCount((dt.getRetryCount() == null ? 0 : dt.getRetryCount()) + 1);
        dataManager.save(new SaveContext().saving(dt));
        return dt;
    }

    @Transactional
    public DonThuoc cancel(DonThuoc dt, String lyDo) {
        if (dt.getTrangThaiEnum() == TrangThaiDonThuoc.DA_GUI
                || dt.getTrangThaiEnum() == TrangThaiDonThuoc.PHAT_HANH) {
            throw new IllegalStateException("Đơn đã gửi, không thể huỷ tại đây. Cần dùng nghiệp vụ đính chính.");
        }
        dt.setTrangThaiEnum(TrangThaiDonThuoc.HUY);
        dt.setLastError(lyDo);
        dataManager.save(new SaveContext().saving(dt));
        return dt;
    }

    /**
     * Đẩy đơn vào outbox (Giai đoạn 3 cung cấp skeleton; Giai đoạn 4 sẽ gọi API liên thông).
     * Idempotent nhờ {@code idempotencyKey} của đơn: trùng key → không enqueue.
     *
     * <p>Nếu {@code clinicportal.donthuoc.test-mode=true}, sẽ KHÔNG gọi outbox — đơn vẫn được
     * validate + lưu + đánh dấu "Đã phát hành" nhưng không enqueue liên thông. Dùng để test.</p>
     */
    @Transactional
    public com.company.clinicportal.lienthong.entity.LienThongDonThuocOutbox submitForSending(DonThuoc dt) {
        if (dt == null) return null;
        List<String> errors = validateForIssue(dt);
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Đơn chưa đủ điều kiện gửi: " + String.join(", ", errors));
        }
        saveDraft(dt);
        if (testMode) {
            log.warn("[TEST-MODE] submitForSending bỏ qua outbox cho maDonThuoc={}", dt.getMaDonThuoc());
            return null;
        }
        com.company.clinicportal.lienthong.LienThongOutboxService outboxService = outboxServiceProvider.getIfAvailable();
        if (outboxService == null) return null;
        return outboxService.enqueueDonThuoc(dt, dt.getIdempotencyKey());
    }

    /**
     * Snapshot toàn bộ thông tin bệnh nhân vào đơn - để lịch sử không đổi.
     */
    void snapshotBenhNhan(DonThuoc dt, BenhNhan bn) {
        if (bn == null) return;
        dt.setHoVaTenBenhNhan(bn.getHoVaTen());
        dt.setMaDinhDanhYTe(bn.getMaDinhDanhYTe());
        dt.setMaDinhDanhCongDan(bn.getMaDinhDanhCongDan());
        dt.setSoDienThoai(bn.getDienThoai());
        dt.setNgaySinh(bn.getNgaySinh());
        dt.setGioiTinh(bn.getGioiTinh() == null ? null : bn.getGioiTinh().getId());
        dt.setDiaChi(bn.getDiaChi());
        dt.setCanNang(bn.getCanNang());
        dt.setSoThangTuoi(bn.getSoThangTuoi());
        dt.setNguoiGiamHoHoTen(bn.getNguoiGiamHoHoTen());
        dt.setNguoiGiamHoQuanHe(bn.getNguoiGiamHoQuanHe());
        dt.setNguoiGiamHoSoDienThoai(bn.getNguoiGiamHoSoDienThoai());
    }

    void snapshotBacSi(DonThuoc dt, NhanSu bacSi) {
        if (bacSi == null) return;
        dt.setTenBacSi(bacSi.getHoTen());
        // maLienThongBacSi, soCCHN sẽ được mapper sau (Giai đoạn 4) khi có mã liên thông chính thức.
    }

    /** Trích userId từ CurrentAuthentication, dùng cho audit. */
    public Long currentUserId() {
        try {
            Object principal = currentAuthentication.getUser();
            if (principal == null) return 0L;
            if (principal instanceof Number n) return n.longValue();
            try {
                return Long.parseLong(principal.toString());
            } catch (NumberFormatException ex) {
                return 0L;
            }
        } catch (Exception e) {
            return 0L;
        }
    }
}
