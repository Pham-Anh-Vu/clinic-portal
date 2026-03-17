package com.company.clinicportal.service;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.ChiTietDichVu;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.GiaKpi;
import com.company.clinicportal.entity.NhanSu;
import com.company.clinicportal.entity.TinhKpi;
import com.company.clinicportal.entity.TinhKpiChiTiet;
import com.company.clinicportal.enumentity.CaLamViec;
import com.company.clinicportal.enumentity.LoaiGiaKPI;
import com.company.clinicportal.enumentity.NhomDichVu;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class TinhKpiChiTietService {
    private static final BigDecimal DEFAULT_RATE_SANG = BigDecimal.valueOf(30000);
    private static final BigDecimal DEFAULT_RATE_TOI = BigDecimal.valueOf(35000);

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Metadata metadata;

    public void regenerateForChiTietDieuTri(Long chiTietDieuTriId) {
        if (chiTietDieuTriId == null) {
            return;
        }

        ChiTietDieuTri chiTietDieuTri = dataManager.load(ChiTietDieuTri.class)
                .id(chiTietDieuTriId)
                .optional()
                .orElse(null);
        if (chiTietDieuTri == null) {
            return;
        }

        List<TinhKpiChiTiet> oldRows = dataManager.load(TinhKpiChiTiet.class)
                .query("select e from TinhKpiChiTiet e where e.idChiTietDieuTri = :chiTiet")
                .parameter("chiTiet", chiTietDieuTri)
                .list();

        List<ChiTietDichVu> chiTietDichVus = dataManager.load(ChiTietDichVu.class)
                .query("select c from ChiTietDichVu c where c.idChiTietPhieuDieuTri = :chiTiet")
                .parameter("chiTiet", chiTietDieuTri)
                .list();

        List<BuoiDieuTri> buoiDieuTris = dataManager.load(BuoiDieuTri.class)
                .query("select distinct b from BuoiDieuTri b " +
                        "where b.idChiTietDieuTri = :chiTiet ")
                .parameter("chiTiet", chiTietDieuTri)
                .list();

        SaveContext saveContext = new SaveContext();
        if (!oldRows.isEmpty()) {
            saveContext.removing(oldRows);
        }

        if (buoiDieuTris.isEmpty()) {
            dataManager.save(saveContext);
            return;
        }

        Map<Long, NhomDichVu> nhomTheoDichVu = new HashMap<>(chiTietDichVus.size());
        Map<NhomDichVu, Set<Long>> dichVuTheoNhom = new EnumMap<>(NhomDichVu.class);
        // Count services by group from ChiTietDichVu rows (same source as "Chi dinh dich vu" grid).
        for (ChiTietDichVu chiTietDichVu : chiTietDichVus) {
            if (chiTietDichVu.getId() == null
                    || chiTietDichVu.getIdDichVu() == null
                    || chiTietDichVu.getIdDichVu().getNhomDichVu() == null) {
                continue;
            }
            Long idChiTietDichVu = chiTietDichVu.getId();
            NhomDichVu nhom = chiTietDichVu.getIdDichVu().getNhomDichVu();
            nhomTheoDichVu.put(idChiTietDichVu, nhom);
            dichVuTheoNhom.computeIfAbsent(nhom, k -> new HashSet<>()).add(idChiTietDichVu);
        }

        BigDecimal trongSoKpiTong = BigDecimal.valueOf(
                chiTietDieuTri.getTrongSoKpi() != null ? chiTietDieuTri.getTrongSoKpi() : 1D
        );
        Map<LoaiGiaKPI, BigDecimal> donGiaKpiTheoCa = loadDonGiaKpiTheoCa();
        Date now = new Date();
        List<TinhKpiChiTiet> newRows = new ArrayList<>();

        for (BuoiDieuTri buoi : buoiDieuTris) {
            if (buoi.getIdChiTietDichVu() == null || buoi.getIdChiTietDichVu().getId() == null) {
                continue;
            }
            NhomDichVu nhom = nhomTheoDichVu.get(buoi.getIdChiTietDichVu().getId());
            if (nhom == null) {
                continue;
            }

            int soDichVuTrongNhom = Math.max(
                    1,
                    dichVuTheoNhom.getOrDefault(nhom, Collections.emptySet()).size()
            );

            List<NhanSu> nhanSuThucHien = new ArrayList<>(2);
            if (buoi.getIdNhanSuStaging() != null) {
                nhanSuThucHien.add(buoi.getIdNhanSuStaging());
            }
            if (buoi.getIdNhanSu2Staging() != null) {
                nhanSuThucHien.add(buoi.getIdNhanSu2Staging());
            }
            int soNguoiThucHien = nhanSuThucHien.size();
            if (soNguoiThucHien == 0) {
                continue;
            }

            CaLamViec caThucHien = resolveCaThucHien(buoi);
            BigDecimal donGiaCa = caThucHien == CaLamViec.TOI
                    ? donGiaKpiTheoCa.getOrDefault(LoaiGiaKPI.TOI, DEFAULT_RATE_TOI)
                    : donGiaKpiTheoCa.getOrDefault(LoaiGiaKPI.SANG, DEFAULT_RATE_SANG);
            BigDecimal tiLeNhom = getTiLeNhom(chiTietDieuTri, nhom)
                    .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
            // KPI weight per person: (trongSoKpiTong * tiLeNhom) / soDichVuTrongNhom / soNguoiThucHien
            BigDecimal trongSoKpiMoiNguoi = trongSoKpiTong
                    .multiply(tiLeNhom)
                    .divide(BigDecimal.valueOf(soDichVuTrongNhom), 8, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(soNguoiThucHien), 8, RoundingMode.HALF_UP)
                    ;
            // Money is shared equally by performers of the same service session.
            long thanhTien = donGiaCa.multiply(trongSoKpiMoiNguoi)
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();

            for (NhanSu nhanSu : nhanSuThucHien) {
                TinhKpiChiTiet row = metadata.create(TinhKpiChiTiet.class);
                row.setIdChiTietDieuTri(chiTietDieuTri);
                row.setIdChiTietDichVu(buoi.getIdChiTietDichVu());
                row.setIdBuoiDieuTri(buoi);
                row.setIdNhanSu(nhanSu);
                row.setCaThucHien(caThucHien);
                row.setNgayThucHien(buoi.getNgayThucHien());
                row.setTrongSoKpi(trongSoKpiMoiNguoi.doubleValue());
                row.setThanhTien(thanhTien);
                row.setCreatedAt(now);
                row.setUpdatedAt(now);
                row.setCreatedById(0L);
                row.setUpdatedById(0L);
                saveContext.saving(row);
                newRows.add(row);
            }
        }

        dataManager.save(saveContext);
        syncTinhKpiByMonth(oldRows, newRows);
    }

    private BigDecimal getTiLeNhom(ChiTietDieuTri chiTietDieuTri, NhomDichVu nhomDichVu) {
        Double value = switch (nhomDichVu) {
            case VAT_LY_TRI_LIEU -> chiTietDieuTri.getTrongSoVatLyTriLieu();
            case VAN_DONG_TRI_LIEU -> chiTietDieuTri.getTrongSoVanDongTriLieu();
            case KEO_NAN_TRI_LIEU -> chiTietDieuTri.getTrongSoKeoNanTriLieu();
            case XOA_BOP_TRI_LIEU -> chiTietDieuTri.getTrongSoXoaBopTriLieu();
            case KHAM_LUONG_GIA -> chiTietDieuTri.getTrongSoKhamLuongGia();
        };
        return BigDecimal.valueOf(value != null ? value : 0D);
    }

    private Map<LoaiGiaKPI, BigDecimal> loadDonGiaKpiTheoCa() {
        Map<LoaiGiaKPI, BigDecimal> rates = new EnumMap<>(LoaiGiaKPI.class);
        List<GiaKpi> giaKpis = dataManager.load(GiaKpi.class)
                .query("select g from GiaKpi g")
                .list();
        for (GiaKpi giaKpi : giaKpis) {
            if (giaKpi.getLoai() == null || giaKpi.getGia() == null) {
                continue;
            }
            rates.put(giaKpi.getLoai(), BigDecimal.valueOf(giaKpi.getGia()));
        }
        return rates;
    }

    private CaLamViec resolveCaThucHien(BuoiDieuTri buoiDieuTri) {
        if (buoiDieuTri.getGioBatDau() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(buoiDieuTri.getGioBatDau());
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            if (hour > 17 || (hour == 17 && minute >= 30)) {
                return CaLamViec.TOI;
            }
            return CaLamViec.SANG;
        }
        return buoiDieuTri.getCa() != null ? buoiDieuTri.getCa() : CaLamViec.SANG;
    }

    private void syncTinhKpiByMonth(List<TinhKpiChiTiet> oldRows, List<TinhKpiChiTiet> newRows) {
        Set<MonthlyKey> keys = new HashSet<>();
        collectMonthlyKeys(oldRows, keys);
        collectMonthlyKeys(newRows, keys);
        if (keys.isEmpty()) {
            return;
        }

        Set<Long> nhanSuIds = new HashSet<>();
        for (MonthlyKey key : keys) {
            nhanSuIds.add(key.nhanSuId());
        }
        Map<Long, NhanSu> nhanSuById = new HashMap<>();
        if (!nhanSuIds.isEmpty()) {
            List<NhanSu> nhanSus = dataManager.load(NhanSu.class)
                    .query("select n from NhanSu n where n.id in :ids")
                    .parameter("ids", nhanSuIds)
                    .list();
            for (NhanSu nhanSu : nhanSus) {
                if (nhanSu.getId() != null) {
                    nhanSuById.put(nhanSu.getId(), nhanSu);
                }
            }
        }

        SaveContext saveContext = new SaveContext();
        Date now = new Date();

        for (MonthlyKey key : keys) {
            NhanSu nhanSu = nhanSuById.get(key.nhanSuId());
            if (nhanSu == null) {
                continue;
            }

            Date monthStart = key.monthStart();
            Date monthEnd = getNextMonth(monthStart);

            List<TinhKpiChiTiet> details = dataManager.load(TinhKpiChiTiet.class)
                    .query("select e from TinhKpiChiTiet e " +
                            "where e.idNhanSu = :nhanSu " +
                            "and e.ngayThucHien >= :monthStart and e.ngayThucHien < :monthEnd")
                    .parameter("nhanSu", nhanSu)
                    .parameter("monthStart", monthStart)
                    .parameter("monthEnd", monthEnd)
                    .list();

            double kpiSang = 0D;
            double kpiToi = 0D;
            long thanhTien = 0L;
            for (TinhKpiChiTiet detail : details) {
                double w = detail.getTrongSoKpi() != null ? detail.getTrongSoKpi() : 0D;
                if (detail.getCaThucHien() == CaLamViec.TOI) {
                    kpiToi += w;
                } else {
                    kpiSang += w;
                }
                thanhTien += detail.getThanhTien() != null ? detail.getThanhTien() : 0L;
            }

            TinhKpi tinhKpi = dataManager.load(TinhKpi.class)
                    .query("select e from TinhKpi e where e.idNhanSu = :nhanSu and e.thang = :thang")
                    .parameter("nhanSu", nhanSu)
                    .parameter("thang", monthStart)
                    .optional()
                    .orElse(null);

            if (tinhKpi == null) {
                tinhKpi = metadata.create(TinhKpi.class);
                tinhKpi.setIdNhanSu(nhanSu);
                tinhKpi.setThang(monthStart);
                tinhKpi.setCreatedAt(now);
                tinhKpi.setCreatedById(0L);
            }

            tinhKpi.setKpiSang(kpiSang);
            tinhKpi.setKpiToi(kpiToi);
            tinhKpi.setKpiTong(kpiSang + kpiToi);
            tinhKpi.setThanhTien(thanhTien);
            tinhKpi.setUpdatedAt(now);
            tinhKpi.setUpdatedById(0L);
            saveContext.saving(tinhKpi);
        }

        dataManager.save(saveContext);
    }

    private void collectMonthlyKeys(List<TinhKpiChiTiet> rows, Set<MonthlyKey> keys) {
        for (TinhKpiChiTiet row : rows) {
            if (row.getIdNhanSu() == null || row.getIdNhanSu().getId() == null || row.getNgayThucHien() == null) {
                continue;
            }
            keys.add(new MonthlyKey(row.getIdNhanSu().getId(), normalizeMonthStart(row.getNgayThucHien())));
        }
    }

    private Date normalizeMonthStart(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getNextMonth(Date monthStart) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(monthStart);
        cal.add(Calendar.MONTH, 1);
        return cal.getTime();
    }

    private record MonthlyKey(Long nhanSuId, Date monthStart) {
    }
}
