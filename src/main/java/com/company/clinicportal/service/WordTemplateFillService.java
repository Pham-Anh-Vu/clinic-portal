package com.company.clinicportal.service;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Điền placeholder dạng {@code ${key}} trong mẫu Word (.doc / .docx) và trả về file đã điền.
 */
@Service
public class WordTemplateFillService {

    private static final Logger log = LoggerFactory.getLogger(WordTemplateFillService.class);

    private static final String CHU_KY_NGAY_KEY = "${chuKy.ngay}";
    private static final String CHU_KY_THANG_KEY = "${chuKy.thang}";
    private static final String CHU_KY_NAM_KEY = "${chuKy.nam}";
    private static final Pattern SIGNATURE_DATE_PATTERN = Pattern.compile(
            "Ngày\\s*[\\.…]+\\s*tháng\\s*[\\.…]+\\s*năm\\s*(?:[\\.…]+|\\d{4}\\.?)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Pattern LOWERCASE_DATE_PATTERN = Pattern.compile(
            "ngày\\s*[\\.…]+\\s*tháng\\s*[\\.…]+\\s*năm\\s*[\\.…]+",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    public byte[] fillDocTemplate(InputStream templateStream, Map<String, String> values) throws IOException {
        try (HWPFDocument document = new HWPFDocument(templateStream);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Range range = document.getRange();
            applyReplacements(range::replaceText, values);
            document.write(out);
            return out.toByteArray();
        }
    }

    public byte[] fillDocxTemplate(InputStream templateStream, Map<String, String> values) throws IOException {
        return fillDocxTemplate(templateStream, values, java.util.Collections.emptyList());
    }

    /**
     * Phiên bản mở rộng của {@link #fillDocxTemplate(InputStream, Map)}:
     * cho phép truyền danh sách base64 ảnh chữ ký (mỗi phần tử là base64
     * của ảnh ứng với 1 row trong bảng) để chèn ảnh vào cell "BS chỉ định"
     * CÙNG DÒNG với text tên bác sĩ (không tạo paragraph/dòng mới).
     *
     * @param chuKyBase64ByRow list base64 (không có prefix "data:") theo
     *                          thứ tự row. Có thể null/empty nếu không
     *                          muốn chèn ảnh. Phần tử null/rỗng nghĩa là
     *                          row đó không có chữ ký.
     */
    public byte[] fillDocxTemplate(InputStream templateStream,
                                   Map<String, String> values,
                                   List<String> chuKyBase64ByRow) throws IOException {
        try (XWPFDocument document = new XWPFDocument(templateStream);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // ${chiTietDichVuRows} needs special handling: it represents repeated table rows.
            // If we replace it as plain text first, we can no longer locate the template row.
            replaceTableRowsForSoBenhAn(document, values, chuKyBase64ByRow);

            Map<String, String> textValues = new HashMap<>(values);
            textValues.remove("${chiTietDichVuRows}");

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                replaceInParagraph(paragraph, textValues);
            }
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            replaceInParagraph(paragraph, textValues);
                        }
                    }
                }
            }
            for (XWPFHeader header : document.getHeaderList()) {
                for (XWPFParagraph paragraph : header.getParagraphs()) {
                    replaceInParagraph(paragraph, textValues);
                }
            }
            for (XWPFFooter footer : document.getFooterList()) {
                for (XWPFParagraph paragraph : footer.getParagraphs()) {
                    replaceInParagraph(paragraph, textValues);
                }
            }

            document.write(out);
            return out.toByteArray();
        }
    }

    public byte[] fillTemplate(String resourcePath, Map<String, String> values) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Không tìm thấy mẫu Word: " + resourcePath);
            }
            if (resourcePath.toLowerCase().endsWith(".docx")) {
                return fillDocxTemplate(in, values);
            }
            if (resourcePath.toLowerCase().endsWith(".doc")) {
                return fillDocTemplate(in, values);
            }
            throw new IOException("Định dạng mẫu Word không hỗ trợ: " + resourcePath);
        }
    }

    private void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> values) {
        String text = paragraph.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        String replaced = text.contains("${")
                ? applyReplacementsToText(text, values)
                : text;
        replaced = applyDatePlaceholderReplacement(replaced, values);
        if (replaced.equals(text)) {
            return;
        }
        int runCount = paragraph.getRuns().size();
        if (runCount == 0) {
            paragraph.createRun().setText(replaced, 0);
            return;
        }
        for (int i = runCount - 1; i > 0; i--) {
            paragraph.removeRun(i);
        }
        XWPFRun run = paragraph.getRuns().get(0);
        run.setText(replaced, 0);
    }

    private void replaceTableRowsForSoBenhAn(XWPFDocument document, Map<String, String> values) {
        replaceTableRowsForSoBenhAn(document, values, java.util.Collections.emptyList());
    }

    private void replaceTableRowsForSoBenhAn(XWPFDocument document,
                                             Map<String, String> values,
                                             List<String> chuKyBase64ByRow) {
        String rowBlock = values.get("${chiTietDichVuRows}");
        if (rowBlock == null) {
            return;
        }

        for (XWPFTable table : document.getTables()) {
            List<XWPFTableRow> rows = table.getRows();
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                XWPFTableRow row = rows.get(rowIndex);
                if (!rowContainsPlaceholder(row, "${chiTietDichVuRows}")) {
                    continue;
                }

                int templateCellCount = Math.max(row.getTableCells().size(), 1);
                table.removeRow(rowIndex);

                List<String> rowData = splitRows(rowBlock);
                if (rowData.isEmpty()) {
                    XWPFTableRow newRow = insertRowWithCells(table, rowIndex, templateCellCount);
                    clearAndFillRowCells(newRow, List.of(""));
                    return;
                }

                int insertIndex = rowIndex;
                for (int dataIdx = 0; dataIdx < rowData.size(); dataIdx++) {
                    String singleRow = rowData.get(dataIdx);
                    XWPFTableRow newRow = insertRowWithCells(table, insertIndex++, templateCellCount);
                    List<String> cells = splitCells(singleRow);
                    clearAndFillRowCells(newRow, cells);

                    // Chèn ảnh chữ ký CÙNG DÒNG với text "Tên BS" ở cell cuối.
                    // Phần tử null/rỗng trong chuKyBase64ByRow → bỏ qua row đó.
                    if (chuKyBase64ByRow != null && dataIdx < chuKyBase64ByRow.size()) {
                        String b64 = chuKyBase64ByRow.get(dataIdx);
                        insertChuKyPictureSameLine(newRow, b64);
                    }
                }
                return;
            }
        }
    }

    private List<String> splitRows(String rowBlock) {
        List<String> rows = new ArrayList<>();
        if (rowBlock == null || rowBlock.isBlank()) {
            return rows;
        }

        // Support 2 formats:
        // 1) HTML rows: <tr>...</tr><tr>...</tr>...
        // 2) Plain text rows: col1||col2||...||colN\n
        if (rowBlock.toLowerCase().contains("</tr>")) {
            String[] parts = rowBlock.split("(?i)</tr>");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                rows.add(trimmed + "</tr>");
            }
            return rows;
        }

        for (String line : rowBlock.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            rows.add(trimmed);
        }
        return rows;
    }

    private List<String> splitCells(String htmlRow) {
        if (htmlRow == null) {
            return List.of("");
        }

        String normalized;
        if (htmlRow.toLowerCase().contains("<td") || htmlRow.toLowerCase().contains("<tr")) {
            normalized = htmlRow
                    .replaceAll("(?i)<tr[^>]*>", "")
                    .replaceAll("(?i)</tr>", "")
                    .replaceAll("(?i)<td[^>]*>", "||")
                    .replaceAll("(?i)</td>", "");
        } else {
            // Plain text row, already in "||" delimited format.
            normalized = htmlRow;
        }

        // Keep empty trailing cells so "a||b||" still yields 3 columns.
        String[] parts = normalized.split("\\|\\|", -1);
        List<String> cells = new ArrayList<>();
        for (String part : parts) {
            String cell = part.replaceAll("(?i)<[^>]+>", "").trim();
            cells.add(cell);
        }
        if (cells.isEmpty()) {
            cells.add("");
        }
        return cells;
    }

    private boolean rowContainsPlaceholder(XWPFTableRow row, String placeholder) {
        for (XWPFTableCell cell : row.getTableCells()) {
            for (XWPFParagraph paragraph : cell.getParagraphs()) {
                if (paragraph.getText() != null && paragraph.getText().contains(placeholder)) {
                    return true;
                }
            }
        }
        return false;
    }

    private XWPFTableRow insertRowWithCells(XWPFTable table, int rowIndex, int cellCount) {
        XWPFTableRow row = table.insertNewTableRow(rowIndex);
        for (int i = 0; i < cellCount; i++) {
            row.addNewTableCell();
        }
        return row;
    }

    private void clearAndFillRowCells(XWPFTableRow row, List<String> cellValues) {
        for (int i = 0; i < row.getTableCells().size(); i++) {
            XWPFTableCell cell = row.getCell(i);
            if (cell == null) {
                continue;
            }
            String value = i < cellValues.size() ? cellValues.get(i) : "";
            while (!cell.getParagraphs().isEmpty()) {
                cell.removeParagraph(0);
            }
            XWPFParagraph paragraph = cell.addParagraph();
            paragraph.createRun().setText(value, 0);
        }
    }

    /**
     * Chèn ảnh chữ ký (decode từ base64) vào cùng dòng với text "Tên BS"
     * trong cell cuối cùng của row. KHÔNG tạo paragraph/dòng mới.
     *
     * Cách làm:
     *  - Lấy paragraph hiện có (đã chứa "Tên BS" do clearAndFillRowCells set).
     *  - addPicture() trên run có sẵn → Word/LibreOffice hiển thị text và ảnh
     *    trên cùng 1 dòng, ảnh nằm ngay sau tên bác sĩ.
     *
     * Ghi chú:
     *  - Một run trong DOCX có thể chứa cả <w:t> và <w:drawing>.
     *  - Nếu text quá dài và không vừa 1 dòng với ảnh, Word có thể tự
     *    ngắt dòng trước ảnh (đây là hành vi chuẩn của Word, không
     *    phải do code tạo dòng mới).
     */
    private void insertChuKyPictureSameLine(XWPFTableRow row, String base64Image) {
        if (base64Image == null || base64Image.isBlank()) {
            return;
        }
        int cellCount = row.getTableCells().size();
        if (cellCount == 0) {
            return;
        }
        XWPFTableCell lastCell = row.getCell(cellCount - 1);
        if (lastCell == null) {
            return;
        }

        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(base64Image);
        } catch (IllegalArgumentException ex) {
            log.warn("Base64 ảnh chữ ký không hợp lệ, bỏ qua chèn ảnh.", ex);
            return;
        }

        // Lấy paragraph cuối cùng (đã có "Tên BS") - KHÔNG tạo paragraph mới.
        List<XWPFParagraph> paragraphs = lastCell.getParagraphs();
        XWPFParagraph targetParagraph = !paragraphs.isEmpty()
                ? paragraphs.get(paragraphs.size() - 1)
                : lastCell.addParagraph();

        // Dùng run hiện có để addPicture (cùng run với "Tên BS").
        // Nếu paragraph không có run thì tạo mới.
        XWPFRun pictureRun = !targetParagraph.getRuns().isEmpty()
                ? targetParagraph.getRuns().get(0)
                : targetParagraph.createRun();

        try {
            // Kích thước ảnh ~90pt x 35pt (EMU = pt * 12700).
            int widthEmu = 90 * 12700;
            int heightEmu = 35 * 12700;
            pictureRun.addPicture(
                new java.io.ByteArrayInputStream(imageBytes),
                XWPFDocument.PICTURE_TYPE_JPEG,
                "chu-ky.jpg",
                widthEmu,
                heightEmu
            );
        } catch (Exception ex) {
            log.error("Không chèn được ảnh chữ ký vào DOCX.", ex);
        }
    }

    private String applyDatePlaceholderReplacement(String text, Map<String, String> values) {
        String ngay = values.get(CHU_KY_NGAY_KEY);
        String thang = values.get(CHU_KY_THANG_KEY);
        String nam = values.get(CHU_KY_NAM_KEY);
        if (ngay == null || ngay.isBlank() || thang == null || thang.isBlank() || nam == null || nam.isBlank()) {
            return text;
        }
        String ngayThangNam = ngay + " tháng " + thang + " năm " + nam;
        if (text.contains("Ngày") && text.contains("tháng") && text.contains("năm")) {
            text = SIGNATURE_DATE_PATTERN.matcher(text).replaceAll("Ngày " + ngayThangNam);
        }
        if (text.contains("ngày") && text.contains("tháng") && text.contains("năm")) {
            text = LOWERCASE_DATE_PATTERN.matcher(text).replaceAll("ngày " + ngayThangNam);
        }
        return text;
    }

    private void applyReplacements(Replacer replacer, Map<String, String> values) {
        for (Map.Entry<String, String> entry : sortedEntries(values)) {
            replacer.replace(entry.getKey(), safeValue(entry.getValue()));
        }
    }

    private String applyReplacementsToText(String text, Map<String, String> values) {
        String result = text;
        for (Map.Entry<String, String> entry : sortedEntries(values)) {
            result = result.replace(entry.getKey(), safeValue(entry.getValue()));
        }
        return result;
    }

    private List<Map.Entry<String, String>> sortedEntries(Map<String, String> values) {
        List<Map.Entry<String, String>> entries = new ArrayList<>(values.entrySet());
        entries.sort(Comparator.comparingInt(e -> -e.getKey().length()));
        return entries;
    }

    private String safeValue(String value) {
        return value != null ? value : "";
    }

    @FunctionalInterface
    private interface Replacer {
        void replace(String placeholder, String value);
    }
}
