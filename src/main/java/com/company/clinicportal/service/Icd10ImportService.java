package com.company.clinicportal.service;

import com.company.clinicportal.entity.Icd10;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Import danh mục ICD-10 từ Excel. Cột kỳ vọng (dòng 1 = header):
 * A: ma_icd (*)        - mã ICD duy nhất
 * B: ten_benh (*)      - tên bệnh tiếng Việt
 * C: ten_benh_en       - tên bệnh tiếng Anh
 * D: chuong            - chương (VD: I, II, ...)
 * E: nhom_chinh        - nhóm chính
 * F: nhom_phu          - nhóm phụ
 * G: mo_ta             - mô tả thêm
 */
@Service
public class Icd10ImportService {

    private static final Logger log = LoggerFactory.getLogger(Icd10ImportService.class);

    @Autowired
    private DataManager dataManager;
    @Autowired
    private CatalogVersionService catalogVersionService;

    public List<ImportPreviewRow> parsePreview(InputStream inputStream) {
        List<ImportPreviewRow> out = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            int startRow = 1;
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                try {
                    Icd10 e = parseRow(row, i + 1);
                    if (e != null) out.add(ImportPreviewRow.ok(i + 1, e.getMaIcd()));
                } catch (Exception ex) {
                    out.add(ImportPreviewRow.error(i + 1, ex.getMessage()));
                }
            }
        } catch (Exception e) {
            out.add(ImportPreviewRow.error(0, "Lỗi đọc file: " + e.getMessage()));
        }
        return out;
    }

    @Transactional
    public DmThuocImportService.ImportSummary importFromExcel(InputStream inputStream, Integer phienBan) {
        DmThuocImportService.ImportSummary summary = new DmThuocImportService.ImportSummary();
        Date importedAt = new Date();
        if (phienBan == null) phienBan = catalogVersionService.nextVersion("Icd10", "phienBan");
        try (Workbook wb = new XSSFWorkbook(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            int startRow = 1;
            List<Icd10> entities = new ArrayList<>();
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                try {
                    Icd10 e = parseRow(row, i + 1);
                    if (e != null) {
                        e.setImportedAt(importedAt);
                        e.setPhienBan(phienBan == null ? 1 : phienBan);
                        entities.add(e);
                    }
                } catch (Exception ex) {
                    summary.getErrors().add("Dòng " + (i + 1) + ": " + ex.getMessage());
                    log.warn("Skip ICD row {}: {}", i + 1, ex.getMessage());
                }
            }
            if (!entities.isEmpty()) {
                SaveContext ctx = new SaveContext().saving(entities);
                dataManager.save(ctx);
                summary.setInserted(entities.size());
            }
        } catch (Exception e) {
            summary.getErrors().add("Lỗi đọc file: " + e.getMessage());
            log.error("Import Icd10 failed", e);
        }
        return summary;
    }

    private Icd10 parseRow(Row row, int rowNumber) {
        Icd10 e = dataManager.create(Icd10.class);
        String ma = readString(row.getCell(0));
        if (ma.isBlank()) throw new IllegalArgumentException("ma_icd (cột A) bắt buộc");
        e.setMaIcd(ma);

        String ten = readString(row.getCell(1));
        if (ten.isBlank()) throw new IllegalArgumentException("ten_benh (cột B) bắt buộc");
        e.setTenBenh(ten);

        e.setTenBenhEn(blankToNull(readString(row.getCell(2))));
        e.setChuong(blankToNull(readString(row.getCell(3))));
        e.setNhomChinh(blankToNull(readString(row.getCell(4))));
        e.setNhomPhu(blankToNull(readString(row.getCell(5))));
        e.setMoTa(blankToNull(readString(row.getCell(6))));
        return e;
    }

    private String readString(Cell c) {
        if (c == null) return "";
        switch (c.getCellType()) {
            case STRING: return c.getStringCellValue() == null ? "" : c.getStringCellValue().trim();
            case NUMERIC:
                double v = c.getNumericCellValue();
                if (v == (long) v) return String.valueOf((long) v);
                return String.valueOf(v);
            case BOOLEAN: return String.valueOf(c.getBooleanCellValue());
            default: return "";
        }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell c = row.getCell(i);
            if (c != null && c.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}
