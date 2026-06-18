package com.company.clinicportal.service;

import com.company.clinicportal.entity.ChiTietDieuTri;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Service
public class ChiTietDieuTriPaymentSummaryService {

    private static final String TONG_TIEN_QUERY = """
            select coalesce(sum(
                case when e.tinhTheoGia = 'Lẻ'
                    then (coalesce(e.idDichVu.giaBuoiLe, 0) * coalesce(e.soLuong, 0))
                    else (coalesce(e.idDichVu.gia, 0) * coalesce(e.soLuong, 0))
                end
            ), 0)
            from ChiTietDichVu e
            where e.idChiTietPhieuDieuTri.id = :ctdtId
            """;

    private final DataManager dataManager;

    public ChiTietDieuTriPaymentSummaryService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void refreshPaymentSummary(Long chiTietDieuTriId) {
        if (chiTietDieuTriId == null) {
            return;
        }
        ChiTietDieuTri entity = dataManager.load(ChiTietDieuTri.class).id(chiTietDieuTriId).one();
        applyTotals(entity);
        dataManager.save(entity);
    }

    public void refreshPaymentSummariesForBenhNhan(Long benhNhanId) {
        if (benhNhanId == null) {
            return;
        }
        List<ChiTietDieuTri> phieuList = dataManager.load(ChiTietDieuTri.class)
                .query("select e from ChiTietDieuTri e where e.idBenhNhan.id = :benhNhanId")
                .parameter("benhNhanId", benhNhanId)
                .list();
        if (phieuList.isEmpty()) {
            return;
        }
        SaveContext saveContext = new SaveContext();
        for (ChiTietDieuTri phieu : phieuList) {
            applyTotals(phieu);
            saveContext.saving(phieu);
        }
        dataManager.save(saveContext);
    }

    public void applyTotals(ChiTietDieuTri entity) {
        if (entity == null || entity.getId() == null) {
            return;
        }
        Long chiTietDieuTriId = entity.getId();

        long tongTien = dataManager.loadValue(TONG_TIEN_QUERY, Long.class)
                .parameter("ctdtId", chiTietDieuTriId)
                .one();

        double khuyenMaiPercent = entity.getKhuyenMai() != null ? entity.getKhuyenMai() : 0D;
        long daThanhToan = dataManager.loadValue(
                        "select coalesce(sum(e.daThanhToan), 0) from LichSuThanhToan e where e.idChiTietDieuTri.id = :ctdtId",
                        Long.class
                )
                .parameter("ctdtId", chiTietDieuTriId)
                .one();
        Date ngayThanhToanCuoi = dataManager.loadValue(
                        "select max(e.thanhToanLuc) from LichSuThanhToan e where e.idChiTietDieuTri.id = :ctdtId",
                        Date.class
                )
                .parameter("ctdtId", chiTietDieuTriId)
                .optional()
                .orElse(null);

        BigDecimal tongTienBd = BigDecimal.valueOf(tongTien);
        long tongSauKhuyenMai = tongTienBd.subtract(
                        tongTienBd.multiply(BigDecimal.valueOf(khuyenMaiPercent))
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                )
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
        int phaiDong = BigDecimal.valueOf(tongSauKhuyenMai)
                .subtract(BigDecimal.valueOf(daThanhToan))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        entity.setTongTien(tongTien);
        entity.setTongTienSauKhuyenMai(tongSauKhuyenMai);
        entity.setDaThanhToan(daThanhToan);
        entity.setPhaiDong(phaiDong);
        entity.setNgayThanhToan(ngayThanhToanCuoi);
    }
}
