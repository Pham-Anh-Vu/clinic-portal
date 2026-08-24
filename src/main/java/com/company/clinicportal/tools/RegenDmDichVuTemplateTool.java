package com.company.clinicportal.tools;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Regenerate file Excel mẫu {@code reports/dmDichVu.xlsx} với cột "Trạng thái"
 * (Đang hoạt động / Dừng hoạt động) ở cột E.
 *
 * <p>Cách chạy:
 * <pre>{@code
 * ./gradlew regenDmDichVuTemplate
 * }</pre>
 *
 * <p>Hoặc nếu cần truyền đường d�n output khác, chạy trực tiếp:
 * <pre>{@code
 * java -cp <classpath> com.company.clinicportal.tools.RegenDmDichVuTemplateTool [outputPath]
 * }</pre>
 */
public final class RegenDmDichVuTemplateTool {

    private static final String DEFAULT_OUTPUT =
            "src/main/resources/reports/dmDichVu.xlsx";

    public static void main(String[] args) throws Exception {
        Path output = Path.of(args.length > 0 ? args[0] : DEFAULT_OUTPUT);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Danh mục dịch vụ");

            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Row 0: Tiêu đề
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(22f);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DANH MỤC DỊCH VỤ");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            // Row 1: Header (5 cột)
            String[] headers = {
                    "Tên dịch vụ (*)",
                    "Nhóm dịch vụ (*)",
                    "Mô tả",
                    "Giá",
                    "Trạng thái"
            };
            Row headerRow = sheet.createRow(1);
            headerRow.setHeightInPoints(20f);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Row 2: Ví dụ
            Row sample = sheet.createRow(2);
            sample.createCell(0).setCellValue("Ví dụ: Vật lý trị liệu cơ bản");
            sample.createCell(1).setCellValue("Vật lý trị liệu");
            sample.createCell(2).setCellValue("Mô tả dịch vụ");
            sample.createCell(3).setCellValue(100000);
            sample.createCell(4).setCellValue("Đang hoạt động");

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) < 4000) {
                    sheet.setColumnWidth(i, 4000);
                }
                if (i == 2 || i == 0) {
                    sheet.setColumnWidth(i, 8000);
                }
            }

            Path parent = output.toAbsolutePath().getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            try (FileOutputStream fos = new FileOutputStream(output.toFile())) {
                wb.write(fos);
            }
            System.out.println("Đã tạo file mẫu: " + output.toAbsolutePath());
        }
    }
}
