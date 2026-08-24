package com.company.clinicportal.service;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.DmDichVu;
import com.company.clinicportal.entity.ToDieuTri;
import com.company.clinicportal.entity.ToDieuTriKyThuat;
import io.jmix.core.DataManager;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Sinh bảng "Theo dõi bệnh nhân điều trị tại phòng khám Nhật Minh" dựa trên file
 * template {@code /reports/sổ theo dõi BN điều trị hàng ngày.xlsx} (sheet "mẫu").
 * <p>
 * Mỗi {@link ToDieuTri} của {@link ChiTietDieuTri} tương ứng một dòng trong bảng;
 * cột "Dịch vụ kỹ thuật" được đánh "x" nếu {@code kyThuatList} của dòng đó có
 * {@link DmDichVu} khớp tên (theo cột cố định của template, fuzzy match);
 * cột "BN ký tên" ghi tên BS chỉ định và chèn ảnh chữ ký BS chỉ định vào ô đó.
 * <p>
 * Kết quả trả về là mảng byte PDF (sau khi LibreOffice convert từ xlsx đã fill).
 */
@Service
public class TheoDoiBNPrintService {

    private static final Logger log = LoggerFactory.getLogger(TheoDoiBNPrintService.class);

    /** Đường dẫn classpath tới template xlsx. */
    private static final String TEMPLATE_PATH = "/reports/sổ theo dõi BN điều trị hàng ngày.xlsx";

    /** Tên sheet dùng để fill cho 1 bệnh nhân. */
    private static final String SHEET_NAME = "mẫu";

    /** STT cột 0 (A), Ngày 1 (B). Từ cột 2 trở đi sẽ được fill động theo tên dịch vụ
     *  thực tế của từng bệnh nhân (xem {@code SERVICE_NAME_ROW_INDEX}). */
    private static final int STT_COL = 0;
    private static final int NGAY_COL = 1;
    /** Cột bắt đầu ghi tên dịch vụ (cùng cột với C trong template). */
    private static final int FIRST_SERVICE_COL = 2;
    /** Số cột tối đa dành cho tên dịch vụ (đủ lớn để bao nhiều DV khác nhau). */
    private static final int MAX_SERVICE_COLS = 14;

    /** Header của bệnh nhân đặt tại các ô cố định. */
    private static final int HO_TEN_CELL_ROW = 2;
    private static final int HO_TEN_CELL_COL = 6;        // G3
    private static final int NGAY_SINH_CELL_COL = 13;     // N3
    private static final int NGAY_VAO_DT_CELL_COL = 17;   // T3
    private static final int DIA_CHI_CELL_ROW = 3;
    private static final int DIA_CHI_CELL_COL = 6;       // G4
    private static final int CHAN_DOAN_CELL_ROW = 4;
    private static final int CHAN_DOAN_CELL_COL = 6;     // G5

    /** Hàng header cuối cùng (row 7 trong Excel, 0-based = 6) — hàng đầu tiên của dữ liệu là row 7 (0-based = 7). */
    private static final int FIRST_DATA_ROW_INDEX = 7;

    /** Tổng số dòng dữ liệu trong template (STT 1..312 trong file mẫu). */
    private static final int MAX_DATA_ROWS = 312;

    /** Font size cho các cell header & data fill vào (theo yêu cầu: 12px). */
    private static final short FILL_FONT_SIZE_PT = 12;

    /** Row index (0-based) của hàng sub-header (hàng 7 trong Excel 1-based) —
     *  vị trí ghi tên dịch vụ bên dưới hàng "Dịch vụ kỹ thuật" (C6) và bên trái
     *  ô "Người T.Hiện" (D7 trong template gốc). */
    private static final int SERVICE_NAME_ROW_INDEX = 6;

    /**
     * Hằng số giữ lại để tương thích ngược - KHÔNG còn dùng để map cứng cột dịch vụ nữa.
     * Việc fill dịch vụ giờ dựa trên tên DV thực tế của tờ điều trị, được ghi động vào
     * các cột trống ở row 7 (bên trái vị trí "Người T.Hiện" / "Tiền nộp" ban đầu).
     * Để tránh phá vỡ các tham chiếu khác, giữ nguyên map này nhưng không dùng trong fill.
     */
    @SuppressWarnings("unused")
    private static final Map<Integer, String> SERVICE_COLUMN_NAMES = new HashMap<>();
    static {
        SERVICE_COLUMN_NAMES.put(2,  "điều trị bằng các dòng điện xung");
        SERVICE_COLUMN_NAMES.put(3,  "Điều trị bằng dòng điện một chiều có dẫn thuốc  10 phút");
        SERVICE_COLUMN_NAMES.put(4,  "điều trị bằng tia hồng ngoại 10 phút");
        SERVICE_COLUMN_NAMES.put(5,  "điều trị bằng  siêu âm 10 phút");
        SERVICE_COLUMN_NAMES.put(6,  "điều trị b�ng tia hồng ngoại 10 phút");
        SERVICE_COLUMN_NAMES.put(7,  "kỹ thuật xoa bóp vùng 30 phút");
        SERVICE_COLUMN_NAMES.put(8,  "Kỹ thuật kéo nắn trị liệu");
        SERVICE_COLUMN_NAMES.put(9,  "kỹ thuật di động khớp");
        SERVICE_COLUMN_NAMES.put(10, "điều trị bằng nhiệt lạnh");
        SERVICE_COLUMN_NAMES.put(11, "tập VĐ có trợ giúp");
        SERVICE_COLUMN_NAMES.put(12, "tập VĐ có kháng trở");
        SERVICE_COLUMN_NAMES.put(13, "điều trị bằng nhiệt nóng");
        SERVICE_COLUMN_NAMES.put(14, "tắm nắng ");
        SERVICE_COLUMN_NAMES.put(15, "tập vận động cột sống");
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("dd/MM");

    private final DataManager dataManager;
    private final LibreOfficeDocumentConversionService conversionService;

    public TheoDoiBNPrintService(DataManager dataManager,
                                 LibreOfficeDocumentConversionService conversionService) {
        this.dataManager = dataManager;
        this.conversionService = conversionService;
    }

    /**
     * Sinh PDF bảng theo dõi cho một phiếu điều trị.
     */
    public byte[] generatePdf(ChiTietDieuTri chiTietDieuTri) throws IOException {
        if (chiTietDieuTri == null) {
            throw new IllegalArgumentException("ChiTietDieuTri is null");
        }

        List<ToDieuTri> toDieuTris = dataManager.load(ToDieuTri.class)
                .query("select e from ToDieuTri e where e.chiTietDieuTri = :ctdt order by e.tuNgay, e.id")
                .parameter("ctdt", chiTietDieuTri)
                .fetchPlan(fp -> fp
                        .addFetchPlan("_base")
                        .add("kyThuatList", k -> k
                                .addFetchPlan("_base")
                                .add("idDichVu", d -> d.addFetchPlan("_base"))))
                .list();

        BenhNhan benhNhan = chiTietDieuTri.getIdBenhNhan();
        if (benhNhan != null && benhNhan.getId() != null) {
            benhNhan = dataManager.load(BenhNhan.class)
                    .id(benhNhan.getId())
                    .fetchPlan(fp -> fp.addFetchPlan("_base"))
                    .one();
        }

        byte[] xlsxBytes = fillTemplate(chiTietDieuTri, benhNhan, toDieuTris);
        return conversionService.convertSpreadsheetToPdf(xlsxBytes);
    }

    private byte[] fillTemplate(ChiTietDieuTri chiTietDieuTri,
                                BenhNhan benhNhan,
                                List<ToDieuTri> toDieuTris) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(TEMPLATE_PATH);
             Workbook workbook = new XSSFWorkbook(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            if (in == null) {
                throw new IOException("Template not found: " + TEMPLATE_PATH);
            }

            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                throw new IOException("Sheet '" + SHEET_NAME + "' not found in template");
            }

            // 1) Fill header bệnh nhân.
            // Template đặt label "Họ tên bệnh nhân:" ở C3 (top-left của merged C3:J3) →
            // vùng merge che D3,J3 không hiển thị value. Phải unmerge trước khi ghi value vào D3.
            unmergeHeaderRegions(sheet);
            // Đặt font 12pt cho các ô value (G3..U3, G4..J4, G5..J5) để đồng nhất với data bên dưới.
            applyHeaderFont(sheet);
            setStringCell(sheet, HO_TEN_CELL_ROW, HO_TEN_CELL_COL,
                    benhNhan != null ? benhNhan.getHoVaTen() : "");
            setStringCell(sheet, HO_TEN_CELL_ROW, NGAY_SINH_CELL_COL,
                    formatDate(benhNhan != null ? benhNhan.getNgaySinh() : null));
            setStringCell(sheet, HO_TEN_CELL_ROW, NGAY_VAO_DT_CELL_COL,
                    formatDate(resolveNgayVaoDieuTri(chiTietDieuTri, toDieuTris)));
            setStringCell(sheet, DIA_CHI_CELL_ROW, DIA_CHI_CELL_COL,
                    benhNhan != null ? benhNhan.getDiaChi() : "");
            setStringCell(sheet, CHAN_DOAN_CELL_ROW, CHAN_DOAN_CELL_COL,
                    chiTietDieuTri.getChuanDoan());

            // 2) Gom tất cả dịch vụ thực tế được sử dụng trong các dòng ToDieuTri của bệnh nhân,
            //    giữ thứ tự xuất hiện đầu tiên (LinkedHashSet). Mỗi tên dịch vụ sẽ được
            //    ghi vào 1 cột ở hàng sub-header (row 7, idx 6) - bên dưới "Dịch vụ kỹ thuật"
            //    (C6) và bên trái "Người T.Hiện" (D7 trong template gốc, sẽ được chuyển lên row 6).
            LinkedHashSet<String> allUsedServiceNames = new LinkedHashSet<>();
            for (ToDieuTri line : toDieuTris) {
                allUsedServiceNames.addAll(collectKyThuatNames(line));
            }
            // Lưu lại display name (chưa normalize) của từng DV thực tế để hiển thị & map 'x'.
            List<String> actualServiceNames = new ArrayList<>(allUsedServiceNames);

            // 3) Ghi tên dịch vụ vào row 7 (sub-header) theo thứ tự.
            //    Cột bắt đầu = FIRST_SERVICE_COL (= 2, cột C). Mỗi DV chiếm 1 cột.
            //    "Người T.Hiện" (D7) và "Tiền nộp" (E7) sẽ được dời sang row 6.
            int numServices = actualServiceNames.size();
            for (int i = 0; i < numServices && i < MAX_SERVICE_COLS; i++) {
                int col = FIRST_SERVICE_COL + i;
                setStringCell(sheet, SERVICE_NAME_ROW_INDEX, col, actualServiceNames.get(i));
            }

            // 4) Chuyển 2 sub-header "Người T.Hiện" / "Tiền nộp" lên row 6 ngay sau cột DV cuối
            //    để không bị đè bởi tên dịch vụ ở row 7.
            int afterLastServiceCol = FIRST_SERVICE_COL + numServices;
            if (numServices < MAX_SERVICE_COLS) {
                setStringCell(sheet, SERVICE_NAME_ROW_INDEX - 1, afterLastServiceCol, "Người T.Hiện");
            }
            if (numServices + 1 < MAX_SERVICE_COLS) {
                setStringCell(sheet, SERVICE_NAME_ROW_INDEX - 1, afterLastServiceCol + 1, "Tiền nộp");
            }

            // 5) Fill từng dòng dữ liệu từ ToDieuTri (giới hạn MAX_DATA_ROWS).
            //    Cột "Ngày, tháng năm" (B) KHÔNG fill - để trống theo yêu cầu.
            //    Cột "BN ký tên" để trống — BN tự ký tay khi in.
            //    Cột dịch vụ giờ là FIRST_SERVICE_COL..(FIRST_SERVICE_COL + numServices - 1).
            int rowsToPrint = Math.min(toDieuTris.size(), MAX_DATA_ROWS);
            for (int i = 0; i < rowsToPrint; i++) {
                ToDieuTri line = toDieuTris.get(i);
                int rowIndex = FIRST_DATA_ROW_INDEX + i;

                setIntCell(sheet, rowIndex, STT_COL, i + 1);
                // B� fill cột "Ngày, tháng năm" theo yêu cầu.
                // setStringCell(sheet, rowIndex, NGAY_COL, formatKhoangNgay(line));

                Set<String> kyThuatNames = collectKyThuatNames(line);
                for (int svcIdx = 0; svcIdx < actualServiceNames.size(); svcIdx++) {
                    String actualService = actualServiceNames.get(svcIdx);
                    if (kyThuatNames.contains(actualService)) {
                        int col = FIRST_SERVICE_COL + svcIdx;
                        setStringCell(sheet, rowIndex, col, "x");
                    }
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Lấy ngày vào điều trị: ưu tiên {@code tuNgay} sớm nhất của {@link ToDieuTri},
     * fallback sang {@code ChiTietDieuTri.ngayBatDau} (đã auto-fill ở tổng kết).
     */
    private Date resolveNgayVaoDieuTri(ChiTietDieuTri chiTietDieuTri, List<ToDieuTri> toDieuTris) {
        Date earliest = null;
        for (ToDieuTri td : toDieuTris) {
            Date tuNgay = td.getTuNgay();
            if (tuNgay != null && (earliest == null || tuNgay.before(earliest))) {
                earliest = tuNgay;
            }
        }
        if (earliest != null) {
            return earliest;
        }
        return chiTietDieuTri.getNgayBatDau();
    }

    /**
     * Chuẩn hoá một chuỗi để so khớp: lowercase + collapse whitespace + trim.
     */
    private static String normalize(String s) {
        if (s == null) {
            return "";
        }
        return s.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    /**
     * Gom tất cả {@code tenDichVu} của {@code kyThuatList} trong 1 tờ điều trị,
     * đã được normalize. Bỏ qua các dòng có dịch vụ null.
     */
    private static Set<String> collectKyThuatNames(ToDieuTri line) {
        Set<String> result = new HashSet<>();
        if (line == null || line.getKyThuatList() == null) {
            return result;
        }
        for (ToDieuTriKyThuat kt : line.getKyThuatList()) {
            DmDichVu dv = kt.getIdDichVu();
            if (dv != null && dv.getTenDichVu() != null) {
                result.add(normalize(dv.getTenDichVu()));
            }
        }
        return result;
    }

    /**
     * Format khoảng ngày cho cột B: dd/MM hoặc dd/MM - dd/MM nếu khác ngày.
     * Nếu cùng ngày (so ngày/tháng/năm) → chỉ hiển thị 1 lần.
     */
    private static String formatKhoangNgay(ToDieuTri line) {
        Date tu = line.getTuNgay();
        Date den = line.getDenNgay();
        if (tu == null && den == null) {
            return "";
        }
        if (tu == null) {
            return SHORT_DATE_FORMAT.format(den);
        }
        if (den == null) {
            return SHORT_DATE_FORMAT.format(tu);
        }
        if (sameDay(tu, den)) {
            return SHORT_DATE_FORMAT.format(tu);
        }
        return SHORT_DATE_FORMAT.format(tu) + " - " + SHORT_DATE_FORMAT.format(den);
    }

    private static boolean sameDay(Date a, Date b) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(a);
        Calendar cb = Calendar.getInstance();
        cb.setTime(b);
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR)
                && ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR);
    }

    private static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return DATE_FORMAT.format(date);
    }

    /**
     * Ghi giá trị chuỗi vào cell; trả về cell để caller có thể chỉnh thêm (font/style).
     * Mặc định áp font 12pt cho mọi cell do service ghi — đảm bảo kích thước đồng nhất,
     * không bị ảnh hưởng bởi font mặc định (thường 11pt) của workbook.
     */
    private static Cell setStringCell(Sheet sheet, int rowIndex, int colIndex, String value) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }
        cell.setCellValue(value != null ? value : "");
        applyFillFont(cell, sheet.getWorkbook(), FILL_FONT_SIZE_PT, false);
        return cell;
    }

    private static void setIntCell(Sheet sheet, int rowIndex, int colIndex, int value) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }
        cell.setCellValue(value);
        applyFillFont(cell, sheet.getWorkbook(), FILL_FONT_SIZE_PT, false);
    }

    /**
     * Đặt font cho cell (không tạo mới font mỗi lần — dùng lại qua cache Font trong workbook).
     */
    private static void setCellFont(Cell cell, short sizePt, boolean bold) {
        if (cell == null || cell.getSheet() == null) {
            return;
        }
        applyFillFont(cell, cell.getSheet().getWorkbook(), sizePt, bold);
    }

    private static void applyFillFont(Cell cell, Workbook workbook, short sizePt, boolean bold) {
        if (cell == null || workbook == null) {
            return;
        }
        String fontName = "Times New Roman";
        org.apache.poi.ss.usermodel.CellStyle existingStyle = cell.getCellStyle();
        if (existingStyle != null) {
            try {
                int fontIdx = existingStyle.getFontIndexAsInt();
                if (fontIdx >= 0) {
                    Font existing = workbook.getFontAt(fontIdx);
                    if (existing != null && existing.getFontName() != null && !existing.getFontName().isEmpty()) {
                        fontName = existing.getFontName();
                    }
                }
            } catch (Exception ignored) {
                // some style subclasses may not support getFontIndexAsInt; fallback to default fontName.
            }
        }
        Font newFont = workbook.findFont(
                bold,
                (short) Font.COLOR_NORMAL,
                sizePt,
                fontName,
                false,
                false,
                Font.SS_NONE,
                Font.U_NONE);
        if (newFont == null) {
            newFont = workbook.createFont();
            newFont.setFontName(fontName);
            newFont.setFontHeightInPoints(sizePt);
            newFont.setBold(bold);
            newFont.setColor(Font.COLOR_NORMAL);
        }
        org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
        if (existingStyle != null) {
            style.cloneStyleFrom(existingStyle);
        }
        style.setFont(newFont);
        cell.setCellStyle(style);
    }

    /**
     * Unmerge các merged region ở header để giá trị fill vào cell không bị "che" bởi merge
     * (POI/Excel chỉ hiển thị value ở top-left của merge).
     * <p>
     * Theo cấu trúc hiện tại của template: label nằm ở cột C (C3/C4/C5) với merge C:F,
     * value nằm ở cột G (merge G:J); ngày sinh merge L:O với value ở P, ngày vào ĐT
     * merge R:S với value ở T. Hàm này gỡ tất cả merge ở 3 hàng header để cell G/P/T
     * có thể nhận value.
     */
    private static void unmergeHeaderRegions(Sheet sheet) {
        for (int i = sheet.getNumMergedRegions() - 1; i >= 0; i--) {
            CellRangeAddress mr = sheet.getMergedRegion(i);
            // Chỉ gỡ merge ở 3 hàng header (rows 2, 3, 4 - 0-based).
            if (mr.getFirstRow() < 2 || mr.getFirstRow() > 4) {
                continue;
            }
            // Gỡ mọi merge có điểm đầu nằm ở row 2/3/4 (header) — đủ rộng để bao C3:J3, L3:Q3,
            // R3:U3, C4:J4, C5:J5 (và tương tự các biến thể sau khi user tinh chỉnh template).
            sheet.removeMergedRegion(i);
        }
    }

    /**
     * Đặt font Times New Roman 12pt cho các cell header chứa value bệnh nhân:
     * G3 (họ tên), P3 (ngày sinh), T3 (ngày vào điều trị), G4 (địa chỉ), G5 (chẩn đoán).
     * Mục đích: đồng nhất font với các ô dữ liệu và tránh font 12pt mặc định của template.
     */
    private static void applyHeaderFont(Sheet sheet) {
        Workbook wb = sheet.getWorkbook();
        int[][] cells = {
                {2, 6},  // G3
                {2, 15}, // P3
                {2, 19}, // T3
                {3, 6},  // G4
                {4, 6},  // G5
        };
        for (int[] pos : cells) {
            int r = pos[0];
            int c = pos[1];
            Row row = sheet.getRow(r);
            if (row == null) {
                row = sheet.createRow(r);
            }
            Cell cell = row.getCell(c);
            if (cell == null) {
                cell = row.createCell(c);
            }
            applyFillFont(cell, wb, FILL_FONT_SIZE_PT, false);
        }
    }
}
