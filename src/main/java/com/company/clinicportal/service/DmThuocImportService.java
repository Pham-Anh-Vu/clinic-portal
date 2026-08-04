package com.company.clinicportal.service;

import com.company.clinicportal.entity.DmThuoc;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Import danh mục thuốc từ Excel (.xlsx). Dùng cho MVP khi chưa đồng bộ từ danh mục BYT.
 *
 * Cột Excel kỳ vọng (dòng 1 = header, 6 trường):
 * A: ma_thuoc (tùy chọn)   - để trống sẽ tự sinh từ hoat_chat + ten_thuoc
 * B: ten_thuoc (*)         - tên thuốc (bắt buộc)
 * C: hoat_chat             - hoạt chất
 * D: don_vi_tinh           - viên/ống/gói/chai/hộp/tuýp/ml/g/mg/liều
 * E: ham_luong             - hàm lượng
 * F: ghi_chu               - ghi chú
 */
@Service
public class DmThuocImportService {

    private static final Logger log = LoggerFactory.getLogger(DmThuocImportService.class);

    @Autowired
    private DataManager dataManager;
    @Autowired
    private CatalogVersionService catalogVersionService;

    public List<ImportPreviewRow> parsePreview(InputStream inputStream) {
        List<ImportPreviewRow> out = new ArrayList<>();
        Set<String> usedInBatch = new HashSet<>();
        try (Workbook wb = new XSSFWorkbook(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            int startRow = 1; // skip header row
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                try {
                    DmThuoc e = parseRow(row, i + 1, usedInBatch);
                    if (e != null) out.add(ImportPreviewRow.ok(i + 1, e.getMaThuoc()));
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
    public ImportSummary importFromExcel(InputStream inputStream, Integer phienBan) {
        ImportSummary summary = new ImportSummary();
        Date importedAt = new Date();
        if (phienBan == null) phienBan = catalogVersionService.nextVersion("DmThuoc", "phienBan");
        Set<String> usedInBatch = new HashSet<>();
        try (Workbook wb = new XSSFWorkbook(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            int startRow = 1;
            List<DmThuoc> entities = new ArrayList<>();
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                try {
                    DmThuoc e = parseRow(row, i + 1, usedInBatch);
                    if (e != null) {
                        if (maThuocExists(e.getMaThuoc())) {
                            throw new IllegalArgumentException("ma_thuoc '" + e.getMaThuoc() + "' đã tồn tại trong DB");
                        }
                        e.setImportedAt(importedAt);
                        e.setPhienBan(phienBan == null ? 1 : phienBan);
                        entities.add(e);
                    }
                } catch (Exception ex) {
                    summary.getErrors().add("Dòng " + (i + 1) + ": " + ex.getMessage());
                    log.warn("Skip row {}: {}", i + 1, ex.getMessage());
                }
            }
            if (!entities.isEmpty()) {
                SaveContext ctx = new SaveContext().saving(entities);
                dataManager.save(ctx);
                summary.setInserted(entities.size());
            }
        } catch (Exception e) {
            summary.getErrors().add("Lỗi đọc file: " + e.getMessage());
            log.error("Import DmThuoc failed", e);
        }
        return summary;
    }

    private DmThuoc parseRow(Row row, int rowNumber, Set<String> usedMaThuocInBatch) {
        DmThuoc e = dataManager.create(DmThuoc.class);
        // Cột Excel (6 trường - header tiếng Việt):
        // A: Mã thuốc       (tùy chọn - để trống sẽ tự sinh từ Hoạt chất + Tên thuốc)
        // B: Tên thuốc      (bắt buộc)
        // C: Hoạt chất
        // D: Đơn vị tính
        // E: Hàm lượng
        // F: Ghi chú
        String maThuocRaw = nullIfBlank(readString(row.getCell(0)));
        String tenThuoc = nullIfBlank(readString(row.getCell(1)));
        String hoatChat = nullIfBlank(readString(row.getCell(2)));
        if (tenThuoc == null) throw new IllegalArgumentException("ten_thuoc (cột B) bắt buộc");

        String maThuoc;
        if (maThuocRaw == null) {
            maThuoc = generateMaThuoc(tenThuoc, hoatChat, usedMaThuocInBatch);
        } else {
            maThuoc = maThuocRaw;
        }
        if (usedMaThuocInBatch.contains(maThuoc)) {
            throw new IllegalArgumentException("ma_thuoc '" + maThuoc + "' trùng với dòng khác trong file");
        }
        usedMaThuocInBatch.add(maThuoc);
        e.setMaThuoc(maThuoc);
        e.setTenThuoc(tenThuoc);

        e.setHoatChat(hoatChat);
        e.setDonViTinh(nullIfBlank(readString(row.getCell(3))));
        e.setHamLuong(nullIfBlank(readString(row.getCell(4))));
        e.setGhiChu(nullIfBlank(readString(row.getCell(5))));
        return e;
    }

    // -------- helpers --------

    /**
     * Sinh ma_thuoc tự động theo quy tắc:
     * <pre>UPPER(slug(hoat_chat)) "-" UPPER(slug(ten_thuoc)) [- suffix3]</pre>
     * Nếu trùng với ma_thuoc đã dùng trong batch hoặc trong DB, cộng suffix
     * ngẫu nhiên 3 ký tự chữ-số (in hoa) cho đến khi hết trùng (tối đa 10 lần thử).
     */
    String generateMaThuoc(String tenThuoc, String hoatChat, Set<String> usedInBatch) {
        String base = slug(hoatChat) + "-" + slug(tenThuoc);
        if (base.length() > 60) base = base.substring(0, 60);
        if (base.endsWith("-")) base = base.substring(0, base.length() - 1);
        String candidate = base;
        for (int attempt = 0; attempt < 10; attempt++) {
            if (!usedInBatch.contains(candidate) && !maThuocExists(candidate)) {
                return candidate;
            }
            candidate = base + "-" + randomSuffix();
        }
        throw new IllegalArgumentException("Không tạo được ma_thuoc tự sinh duy nhất cho '" + tenThuoc + "'");
    }

    boolean maThuocExists(String maThuoc) {
        if (maThuoc == null || maThuoc.isBlank()) return false;
        try {
            Long count = dataManager.loadValue(
                    "select count(e) from DmThuoc e where e.maThuoc = :ma",
                    Long.class)
                    .parameter("ma", maThuoc)
                    .one();
            return count != null && count > 0;
        } catch (Exception ex) {
            return false;
        }
    }

    private static final SecureRandom RNG = new SecureRandom();
    private static final char[] SUFFIX_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private String randomSuffix() {
        StringBuilder sb = new StringBuilder(3);
        for (int i = 0; i < 3; i++) {
            sb.append(SUFFIX_ALPHABET[RNG.nextInt(SUFFIX_ALPHABET.length)]);
        }
        return sb.toString();
    }

    private String slug(String s) {
        if (s == null) return "THUOC";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String cleaned = n.replaceAll("[^A-Za-z0-9]+", "-").toUpperCase();
        if (cleaned.startsWith("-")) cleaned = cleaned.substring(1);
        if (cleaned.endsWith("-")) cleaned = cleaned.substring(0, cleaned.length() - 1);
        return cleaned.isEmpty() ? "THUOC" : cleaned;
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
            case FORMULA: return readString(c); // simplified
            default: return "";
        }
    }

    private Long parseLong(Cell c) {
        if (c == null) return null;
        try {
            switch (c.getCellType()) {
                case NUMERIC: return (long) c.getNumericCellValue();
                case STRING:
                    String s = c.getStringCellValue();
                    if (s == null || s.isBlank()) return null;
                    return Long.parseLong(s.replaceAll("[^0-9-]", ""));
                default: return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private Boolean parseBool(Cell c) {
        if (c == null) return Boolean.FALSE;
        switch (c.getCellType()) {
            case BOOLEAN: return c.getBooleanCellValue();
            case NUMERIC: return c.getNumericCellValue() != 0;
            case STRING: {
                String s = c.getStringCellValue();
                if (s == null) return Boolean.FALSE;
                return s.trim().equalsIgnoreCase("1")
                        || s.trim().equalsIgnoreCase("true")
                        || s.trim().equalsIgnoreCase("có")
                        || s.trim().equalsIgnoreCase("yes");
            }
            default: return Boolean.FALSE;
        }
    }

    private String nullIfBlank(String s) {
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

    public static class ImportSummary {
        private int inserted;
        private final List<String> errors = new ArrayList<>();
        public int getInserted() { return inserted; }
        public void setInserted(int inserted) { this.inserted = inserted; }
        public List<String> getErrors() { return errors; }
        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("inserted", inserted);
            m.put("errors", errors);
            return m;
        }
    }
}