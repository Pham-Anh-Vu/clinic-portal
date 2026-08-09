package com.company.clinicportal.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Sinh file Excel (.xlsx) mẫu cho các danh mục để admin tải về điền dữ liệu.
 * Cấu trúc cột giữ thống nhất với các import service tương ứng.
 */
@Service
public class CatalogTemplateService {

    public static final String TEMPLATE_DM_THUOC = "dm_thuoc";
    public static final String TEMPLATE_ICD10 = "icd10";

    public byte[] buildTemplate(String templateKind) {
        switch (templateKind == null ? "" : templateKind) {
            case TEMPLATE_DM_THUOC: return buildDmThuocTemplate();
            case TEMPLATE_ICD10: return buildIcd10Template();
            default: throw new IllegalArgumentException("Unknown template kind: " + templateKind);
        }
    }

    private byte[] buildDmThuocTemplate() {
        String[] headers = {
                "Mã thuốc", "Tên thuốc – Hàm lượng", "Hoạt chất", "Đơn vị tính", "Phân loại", "Ghi chú"
        };
        String[] sample = {
                "VD001", "Paracetamol 500mg", "Panadol", "Viên", "Thuốc", "Mẫu - xoá dòng này trước khi import"
        };
        return writeWorkbook(headers, sample);
    }

    private byte[] buildIcd10Template() {
        String[] headers = {
                "ma_icd", "ten_benh", "ten_benh_en", "chuong", "nhom_chinh", "nhom_phu", "mo_ta"
        };
        String[] sample = {
                "J00", "Viêm họng cấp", "Acute nasopharyngitis", "X", "J00-J99", "J00", "Mẫu - xoá dòng này trước khi import"
        };
        return writeWorkbook(headers, sample);
    }

    private byte[] writeWorkbook(String[] headers, String[] sample) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("template");
            CellStyle headerStyle = wb.createCellStyle();
            Font f = wb.createFont();
            f.setBold(true);
            headerStyle.setFont(f);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }
            Row sampleRow = sheet.createRow(1);
            for (int i = 0; i < sample.length; i++) {
                Cell c = sampleRow.createCell(i);
                c.setCellValue(sample[i]);
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, Math.max(20, headers[i].length() + 4) * 256);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Cannot build template", e);
        }
    }
}
