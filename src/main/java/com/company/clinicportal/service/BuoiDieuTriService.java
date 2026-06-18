package com.company.clinicportal.service;

import com.company.clinicportal.entity.BuoiDieuTri;
import com.company.clinicportal.entity.TinhKpiChiTiet;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class BuoiDieuTriService {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private TinhKpiChiTietService tinhKpiChiTietService;

    /**
     * Xóa buổi điều trị cùng các dòng KPI chi tiết tham chiếu (tránh lỗi FK).
     */
    @Transactional
    public void deleteAll(Collection<BuoiDieuTri> buoiDieuTris) {
        if (buoiDieuTris == null || buoiDieuTris.isEmpty()) {
            return;
        }

        List<Long> ids = buoiDieuTris.stream()
                .map(BuoiDieuTri::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return;
        }

        List<TinhKpiChiTiet> kpiRows = dataManager.load(TinhKpiChiTiet.class)
                .query("select e from TinhKpiChiTiet e where e.idBuoiDieuTri.id in :ids")
                .parameter("ids", ids)
                .list();

        SaveContext saveContext = new SaveContext();
        if (!kpiRows.isEmpty()) {
            saveContext.removing(kpiRows);
        }
        buoiDieuTris.stream()
                .filter(b -> b.getId() != null)
                .forEach(saveContext::removing);

        dataManager.save(saveContext);
        tinhKpiChiTietService.recalcTinhKpiAfterRemovingDetails(kpiRows);
    }
}
