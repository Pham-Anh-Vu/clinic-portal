package com.company.clinicportal.service;

import com.company.clinicportal.entity.DmDichVu;
import com.company.clinicportal.entity.GiaKpi;
import com.company.clinicportal.enumentity.LoaiGiaKPI;
import com.company.clinicportal.enumentity.NhomDichVu;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DmDichVuImportService {

    private static final Logger log = LoggerFactory.getLogger(DmDichVuImportService.class);

    @Autowired
    private DataManager dataManager;

    /**
     * Parse Excel file và trả về danh sách preview (không save)
     */
    public List<DmDichVuPreviewItem> parsePreview(InputStream inputStream) {
        List<DmDichVuPreviewItem> previewItems = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row (row 0 and 1 - title and headers)
            int startRow = 2;
            
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                // Check if row is empty
                if (isRowEmpty(row)) {
                    continue;
                }
                
                try {
                    DmDichVu entity = parseRow(row, i + 1);
                    if (entity != null) {
                        previewItems.add(new DmDichVuPreviewItem(i + 1, entity));
                    }
                } catch (Exception e) {
                    previewItems.add(new DmDichVuPreviewItem(i + 1, String.format("Dòng %d: %s", i + 1, e.getMessage())));
                    log.error("Error parsing row {}: {}", i + 1, e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error parsing Excel file for preview", e);
            previewItems.add(new DmDichVuPreviewItem(0, "Lỗi khi đọc file: " + e.getMessage()));
        }
        
        return previewItems;
    }

    @Transactional
    public ImportResult importFromExcel(InputStream inputStream) {
        ImportResult result = new ImportResult();
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row (row 0 and 1 - title and headers)
            int startRow = 2;
            
            List<DmDichVu> entitiesToSave = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                // Check if row is empty
                if (isRowEmpty(row)) {
                    continue;
                }
                
                try {
                    DmDichVu entity = parseRow(row, i + 1);
                    if (entity != null) {
                        entitiesToSave.add(entity);
                    }
                } catch (Exception e) {
                    errors.add(String.format("Dòng %d: %s", i + 1, e.getMessage()));
                    log.error("Error parsing row {}: {}", i + 1, e.getMessage(), e);
                }
            }
            
            // Save entities
            if (!entitiesToSave.isEmpty()) {
                SaveContext saveContext = new SaveContext();
                saveContext.saving(entitiesToSave);
                dataManager.save(saveContext);
                result.setSuccessCount(entitiesToSave.size());
            }
            
            result.setErrors(errors);
            
        } catch (Exception e) {
            log.error("Error importing Excel file", e);
            result.getErrors().add("Lỗi khi đọc file: " + e.getMessage());
        }
        
        return result;
    }
    
    private DmDichVu parseRow(Row row, int rowNumber) throws Exception {
        DmDichVu entity = dataManager.create(DmDichVu.class);
        
        // Column A: Tên dịch vụ (*) - required
        Cell tenDichVuCell = row.getCell(0);
        if (tenDichVuCell == null || getCellValueAsString(tenDichVuCell).trim().isEmpty()) {
            throw new Exception("Tên dịch vụ không được để trống");
        }
        String tenDichVu = getCellValueAsString(tenDichVuCell).trim();
        entity.setTenDichVu(tenDichVu);
        
        // Column B: Nhóm dịch vụ (*) - required
        Cell nhomDichVuCell = row.getCell(1);
        if (nhomDichVuCell == null || getCellValueAsString(nhomDichVuCell).trim().isEmpty()) {
            throw new Exception("Nhóm dịch vụ không được để trống");
        }
        String nhomDichVuStr = getCellValueAsString(nhomDichVuCell).trim();
        NhomDichVu nhomDichVu = mapNhomDichVu(nhomDichVuStr);
        if (nhomDichVu == null) {
            throw new Exception("Nhóm dịch vụ không hợp lệ: " + nhomDichVuStr + ". Các giá trị hợp lệ: Điện trị liệu, Kéo giãn (hoặc Kéo giãn, kéo nắn), Vận động trị liệu, Tập phục hồi chức năng (hoặc Tập PHCN)");
        }
        entity.setNhomDichVu(nhomDichVu);
        
        // Column C: Mô tả - optional
        Cell moTaCell = row.getCell(2);
        if (moTaCell != null) {
            String moTa = getCellValueAsString(moTaCell).trim();
            if (!moTa.isEmpty()) {
                entity.setMoTa(moTa);
            }
        }
        
        // Column D: Giá - optional
        Cell giaCell = row.getCell(3);
        if (giaCell != null) {
            try {
                Double giaValue = getCellValueAsDouble(giaCell);
                if (giaValue != null && giaValue > 0) {
                    // Convert to Long (multiply by 1000 if needed, or just cast)
                    entity.setGia(giaValue.longValue());
                }
            } catch (Exception e) {
                log.warn("Row {}: Invalid price value, skipping", rowNumber);
            }
        }

        Map<LoaiGiaKPI, GiaKpi> giaKpiMap = dataManager.load(GiaKpi.class)
                .query("select g from GiaKpi g where g.loai in :loai")
                .parameter("loai", List.of("sang", "toi"))
                .list()
                .stream()
                .collect(Collectors.toMap(GiaKpi::getLoai, Function.identity()));

        GiaKpi giaKpiSang = giaKpiMap.get(LoaiGiaKPI.SANG);
        GiaKpi giaKpiToi = giaKpiMap.get(LoaiGiaKPI.TOI);

        if (giaKpiSang != null) {
            entity.setGiaKpiSang(giaKpiSang.getGia());
        }
        if (giaKpiToi != null) {
            entity.setGiaKpiToi(giaKpiToi.getGia());
        }
        
        // Set created date
        entity.setCreatedAt(new Date());
        
        return entity;
    }
    
    private NhomDichVu mapNhomDichVu(String value) {
        if (value == null) {
            return null;
        }
        
        String normalized = value.trim().toLowerCase();
        
        // Map Vietnamese names to enum
        // DIEN_TRI_LIEU = "Điện trị liệu"
        if (normalized.contains("điện trị liệu") || normalized.contains("dien_tri_lieu") || normalized.equals("điện trị liệu")) {
            return NhomDichVu.DIEN_TRI_LIEU;
        } 
        // KEO_GIAN = "Kéo giãn, kéo nắn"
        else if (normalized.contains("kéo giãn") || normalized.contains("keo_gian") || normalized.contains("kéo nắn")) {
            return NhomDichVu.KEO_GIAN;
        } 
        // VAN_DONG_TRI_LIEU = "Vận động trị liệu"
        else if (normalized.contains("vận động trị liệu") || normalized.contains("van_dong_tri_lieu") || normalized.equals("vận động trị liệu")) {
            return NhomDichVu.VAN_DONG_TRI_LIEU;
        } 
        // TAP_PHCN = "Tập phục hồi chức năng"
        else if (normalized.contains("tập phcn") || normalized.contains("tap_phcn") || normalized.contains("tập phục hồi chức năng") || normalized.contains("phục hồi chức năng")) {
            return NhomDichVu.TAP_PHCN;
        }
        
        return null;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Format numeric value without decimal if it's a whole number
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue().replace(",", "."));
                } catch (NumberFormatException e) {
                    return null;
                }
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    return null;
                }
            default:
                return null;
        }
    }
    
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell).trim();
                if (!value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static class ImportResult {
        private int successCount = 0;
        private List<String> errors = new ArrayList<>();
        
        public int getSuccessCount() {
            return successCount;
        }
        
        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
}

