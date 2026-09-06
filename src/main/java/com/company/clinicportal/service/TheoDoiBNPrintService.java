package com.company.clinicportal.service;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.DmDichVu;
import com.company.clinicportal.entity.ToDieuTri;
import com.company.clinicportal.entity.ToDieuTriKyThuat;
import io.jmix.core.DataManager;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    /**
     * Số cột tối đa dành cho tên dịch vụ. Được nâng lên 30 để đủ chứa nhiều DV
     * thực tế của một phiếu điều trị; khi vượt quá sẽ tự động cắt bớt DV dư.
     */
    private static final int MAX_SERVICE_COLS = 30;

    /**
     * Cột "BN ký tên" trong template gốc (0-based = 5, tức F).
     * Khi số DV thực tế &gt; 0, vùng từ cột này trở đi sẽ được shift sang phải
     * để nhường chỗ cho các cột DV mới.
     */
    private static final int BN_KY_TEN_COL = 5;
    /**
     * Cột "Người nhận tiền" trong template gốc (0-based = 6, tức G).
     */
    private static final int NGUOI_NHAN_TIEN_COL = 6;
    /**
     * Cột "Người T.Hiện" trong template gốc (0-based = 3, tức D, nằm ở row 7).
     */
    private static final int NGUOI_THUC_HIEN_COL = 3;
    /**
     * Cột "Tiền nộp" trong template gốc (0-based = 4, tức E, nằm ở row 7).
     */
    private static final int TIEN_NOP_COL = 4;

    /** Header của bệnh nhân đặt tại các ô cố định. */
    private static final int HO_TEN_CELL_ROW = 2;
    private static final int HO_TEN_CELL_COL = 5;        // G3
    private static final int NGAY_SINH_CELL_COL = 12;     // N3
    private static final int NGAY_VAO_DT_CELL_COL = 16;   // T3
    private static final int DIA_CHI_CELL_ROW = 3;
    private static final int DIA_CHI_CELL_COL = 5;       // G4
    private static final int CHAN_DOAN_CELL_ROW = 4;
    private static final int CHAN_DOAN_CELL_COL = 5;     // G5

    /** Hàng header cuối cùng (row 7 trong Excel, 0-based = 6) — hàng đầu tiên của dữ liệu là row 7 (0-based = 7). */
    private static final int FIRST_DATA_ROW_INDEX = 7;

    /** Tổng số dòng dữ liệu trong template (STT 1..312 trong file mẫu). */
    private static final int MAX_DATA_ROWS = 312;

    /**
     * Row index 0-based cuối cùng luôn được áp border bảng DV (kể cả khi BN có ít
     * {@code ToDieuTri} hơn - các dòng trống phía dưới vẫn có border để người dùng
     * điền tay). Mặc định = 20 tương ứng row 21 0-based / row 22 Excel 1-based.
     */
    private static final int PRINT_LAST_DATA_ROW_INDEX = 20;

    /** Font size cho các cell header & data fill vào (theo yêu cầu: 12px). */
    private static final short FILL_FONT_SIZE_PT = 13;

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
                        .add("idNguoiThucHien", ns -> ns.addFetchPlan("_base"))
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
                    resolveChuanDoanForReport(chiTietDieuTri));

            // 2) Gom tất cả dịch vụ thực tế được sử dụng trong các dòng ToDieuTri của bệnh nhân,
            //    giữ thứ tự xuất hiện đầu tiên (LinkedHashSet). Mỗi tên dịch vụ sẽ được
            //    ghi vào 1 cột ở hàng sub-header (row 7, idx 6).
            //
            //    Lưu ý: set chứa tên đã normalize (lowercase + collapse space) để so khớp
            //    với collectKyThuatNames(). Display name gốc lấy từ lần xuất hiện đầu tiên.
            Map<String, String> firstDisplayByNormalized = new LinkedHashMap<>();
            for (ToDieuTri line : toDieuTris) {
                if (line == null || line.getKyThuatList() == null) {
                    continue;
                }
                for (ToDieuTriKyThuat kt : line.getKyThuatList()) {
                    DmDichVu dv = kt.getIdDichVu();
                    if (dv != null && dv.getTenDichVu() != null) {
                        String normalized = normalize(dv.getTenDichVu());
                        if (!normalized.isEmpty() && !firstDisplayByNormalized.containsKey(normalized)) {
                            firstDisplayByNormalized.put(normalized, dv.getTenDichVu());
                        }
                    }
                }
            }
            List<String> normalizedServiceKeys = new ArrayList<>(firstDisplayByNormalized.keySet());
            List<String> actualServiceNames = new ArrayList<>(firstDisplayByNormalized.values());

            // Cắt bớt nếu vượt quá giới hạn (giữ thứ tự đầu tiên).
            int numServices = Math.min(actualServiceNames.size(), MAX_SERVICE_COLS);
            if (actualServiceNames.size() > MAX_SERVICE_COLS) {
                log.warn("Bệnh nhân có {} dịch vụ khác nhau, chỉ in {} cột đầu tiên",
                        actualServiceNames.size(), MAX_SERVICE_COLS);
                actualServiceNames = new ArrayList<>(actualServiceNames.subList(0, MAX_SERVICE_COLS));
                normalizedServiceKeys = new ArrayList<>(normalizedServiceKeys.subList(0, MAX_SERVICE_COLS));
            }

            // 3) Relocate 4 ô header phụ sang phải numServices cột (công thức user xác nhận).
            //    Template gốc:  D7="Người T.Hiện"  E7="Tiền nộp"
            //                   F6="BN ký tên"      G6="Người nhận tiền"
            //    Sau relocate:  D7→row 7, col (3+N);  E7→row 7, col (4+N)
            //                   F6→row 6, col (5+N);  G6→row 6, col (6+N)
            //    Sau đó clear 4 ô gốc để khỏi cột trống thừa.
            if (numServices > 0) {
                int nguoiTHNewCol       = FIRST_SERVICE_COL + numServices;       // 3+N
                int tienNopNewCol       = FIRST_SERVICE_COL + numServices + 1;   // 4+N
                int bnKyTenNewCol       = FIRST_SERVICE_COL + numServices + 2;   // 5+N
                int nguoiNhanTienNewCol = FIRST_SERVICE_COL + numServices + 3;   // 6+N

                setStringCell(sheet, SERVICE_NAME_ROW_INDEX,     nguoiTHNewCol,       "Người T.Hiện");
                setStringCell(sheet, SERVICE_NAME_ROW_INDEX,     tienNopNewCol,       "Tiền nộp");
                setStringCell(sheet, SERVICE_NAME_ROW_INDEX - 1, bnKyTenNewCol,       "BN ký tên");
                setStringCell(sheet, SERVICE_NAME_ROW_INDEX - 1, nguoiNhanTienNewCol, "Người nhận tiền");

                clearCellValue(sheet, SERVICE_NAME_ROW_INDEX,     NGUOI_THUC_HIEN_COL); // D7
                clearCellValue(sheet, SERVICE_NAME_ROW_INDEX,     TIEN_NOP_COL);        // E7
                clearCellValue(sheet, SERVICE_NAME_ROW_INDEX - 1, BN_KY_TEN_COL);       // F6
                clearCellValue(sheet, SERVICE_NAME_ROW_INDEX - 1, NGUOI_NHAN_TIEN_COL); // G6
            }

            // 4) Ghi tên dịch vụ vào row 7 (sub-header) theo thứ tự, bắt đầu từ cột C.
            for (int i = 0; i < numServices; i++) {
                int col = FIRST_SERVICE_COL + i;
                setStringCell(sheet, SERVICE_NAME_ROW_INDEX, col, actualServiceNames.get(i));
            }

            // 5) (đã gộp vào bước 3 ở trên)

            // 6) Fill từng dòng dữ liệu từ ToDieuTri (giới hạn MAX_DATA_ROWS).
            //    Cột "Ngày, tháng năm" (B) KHÔNG fill - để trống theo yêu cầu.
            //    Cột dịch vụ giờ là FIRST_SERVICE_COL..(FIRST_SERVICE_COL + numServices - 1).
            //    Cột "Người T.Hiện" ở (FIRST_SERVICE_COL + numServices) - ghi tên người TH.
            //    Các cột "BN ký tên" / "Người nhận tiền" / "Tiền nộp" để trống.
            int nguoiTHCol = FIRST_SERVICE_COL + numServices;
            int rowsToPrint = Math.min(toDieuTris.size(), MAX_DATA_ROWS);
            for (int i = 0; i < rowsToPrint; i++) {
                ToDieuTri line = toDieuTris.get(i);
                int rowIndex = FIRST_DATA_ROW_INDEX + i;

                setIntCell(sheet, rowIndex, STT_COL, i + 1);
                // Cột "Ngày, tháng năm" (B) cố ý để trống theo yêu cầu.

//                Set<String> kyThuatNames = collectKyThuatNames(line);
//                for (int svcIdx = 0; svcIdx < numServices; svcIdx++) {
//                    String normalizedKey = normalizedServiceKeys.get(svcIdx);
//                    if (kyThuatNames.contains(normalizedKey)) {
//                        int col = FIRST_SERVICE_COL + svcIdx;
//                        setStringCell(sheet, rowIndex, col, "x");
//                    }
//                }

                // Cột "Người T.Hiện": lấy tên người thực hiện (ưu tiên theo thứ tự).
//                String tenNguoiTH = resolveNguoiThucHienName(line);
//                if (tenNguoiTH != null && !tenNguoiTH.isEmpty()) {
//                    setStringCell(sheet, rowIndex, nguoiTHCol, tenNguoiTH);
//                }
            }

            // 7) Áp border mỏng cho toàn bộ bảng DV (header + data + cột phụ đã relocate).
            //    Vùng: cột FIRST_SERVICE_COL (=C) → cột "Người nhận tiền" (= col 6+N).
            //    Hàng: header DV (SERVICE_NAME_ROW_INDEX = row 7) → dòng dữ liệu cuối
            //    cố định (PRINT_LAST_DATA_ROW_INDEX = 20, tức row 21) để luôn có đủ 12
            //    dòng border cho người dùng điền tay khi BN có ít ToDieuTri.
            int lastColBorder = FIRST_SERVICE_COL + numServices + 3; // cột "Người nhận tiền"
            int lastRowBorder = PRINT_LAST_DATA_ROW_INDEX;
            // 7a) Pre-fill blank cells cho 2 cột "BN ký tên" và "Người nhận tiền" ở các
            //     row data (cột K, L). Các cột này không được setStringCell nên cell
            //     vẫn NULL → border có thể bị LibreOffice bỏ qua. Clone style từ cột
            //     "Tiền nộp" (col 4+N) để giữ font/fill, setBlank để đảm bảo render.
            if (numServices > 0) {
                int tienNopCol = FIRST_SERVICE_COL + numServices + 1;       // 4+N
                int bnKyTenCol = FIRST_SERVICE_COL + numServices + 2;       // 5+N
                int nguoiNhanTienCol = FIRST_SERVICE_COL + numServices + 3; // 6+N
                for (int r = FIRST_DATA_ROW_INDEX; r <= lastRowBorder; r++) {
                    Row dataRow = sheet.getRow(r);
                    if (dataRow == null) {
                        dataRow = sheet.createRow(r);
                    }
                    Cell tienNopCell = dataRow.getCell(tienNopCol);
                    CellStyle srcStyle = tienNopCell != null ? tienNopCell.getCellStyle() : null;
                    for (int c : new int[]{bnKyTenCol, nguoiNhanTienCol}) {
                        Cell target = dataRow.getCell(c);
                        if (target == null) {
                            target = dataRow.createCell(c);
                        }
                        if (srcStyle != null && target.getCellStyle() == null) {
                            CellStyle newStyle = sheet.getWorkbook().createCellStyle();
                            newStyle.cloneStyleFrom(srcStyle);
                            target.setCellStyle(newStyle);
                        }
                        target.setBlank();
                    }
                }
            }
            applyBorderToRange(sheet, FIRST_SERVICE_COL, lastColBorder,
                    SERVICE_NAME_ROW_INDEX - 1, lastRowBorder); // bao luôn row header phụ (excel row 6)

            // Gộp vùng "Dịch vụ kỹ thuật" ở hàng header phụ (excel row 6) thành 1 ô duy nhất:
            // chỉ áp dụng cho các cột DV (C → cột DV cuối); giữ border ngăn với
            // cột "Người T.Hiện", "Tiền nộp", "BN ký tên", "Người nhận tiền".
            mergeDichVuKyThuatHeader(sheet,
                    FIRST_SERVICE_COL,
                    FIRST_SERVICE_COL + numServices - 1,
                    SERVICE_NAME_ROW_INDEX - 1);

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
     * Dời các cột phụ trong template sang phải để nhường chỗ cho {@code numServices} cột DV mới.
     * <p>
     * Template gốc (sheet "mẫu") có cấu trúc cột:
     * <pre>
     *   A=STT  B=Ngày  C..(C+numServices-1)=DV mới  F=BN ký tên  G=Người nhận tiền
     *   D7=Người T.Hiện  E7=Tiền nộp
     * </pre>
     * Khi {@code numServices > 0}, toàn bộ cột từ {@code FIRST_SERVICE_COL + numServices}
     * trở đi (gồm cả F, G, ...) sẽ được dịch sang phải thêm đúng {@code numServices} cột,
     * đảm bảo các giá trị cũ (nhãn "BN ký tên", "Người nhận tiền" và "Người T.Hiện",
     * "Tiền nộp") không bị đè bởi tên DV.
     * <p>
     * Lưu ý: dùng {@link Sheet#shiftColumns(int, int, int)} của POI — hàm này tự xử lý
     * style, merged region và cell value của toàn bộ cột. Các cột nguồn sẽ trống sau khi
     * shift (sẽ được ghi đè bằng tên DV ngay sau đó).
     */
    private static void shiftTrailingColumns(Sheet sheet, int numServices) {
        int startCol = FIRST_SERVICE_COL + numServices;
        int lastCol = computeLastColumn(sheet);
        if (lastCol < startCol) {
            return;
        }
        try {
            sheet.shiftColumns(startCol, lastCol, numServices);
        } catch (Exception e) {
            log.warn("Không thể shift cột phụ ({}..{}) sang phải {} cột: {}",
                    startCol, lastCol, numServices, e.getMessage());
        }
    }

    /**
     * Duyệt tất cả row để tìm chỉ số cột (0-based) lớn nhất có dữ liệu trong sheet.
     * Trả về -1 nếu sheet rỗng. Dùng chung cho {@link #shiftTrailingColumns} và
     * {@link #applyBorderToRange}.
     */
    private static int computeLastColumn(Sheet sheet) {
        int lastCol = -1;
        for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            short last = row.getLastCellNum();
            if (last > 0 && last - 1 > lastCol) {
                lastCol = last - 1;
            }
        }
        return lastCol;
    }

    /**
     * Lấy tên người thực hiện cho 1 dòng ToDieuTri (từ {@link ToDieuTri#getIdNguoiThucHien()}).
     * Entity {@link ToDieuTriKyThuat} hiện chưa có trường người thực hiện riêng.
     * Trả về chuỗi rỗng nếu không có.
     */
    private static String resolveNguoiThucHienName(ToDieuTri line) {
        if (line == null || line.getIdNguoiThucHien() == null) {
            return "";
        }
        return nullSafe(line.getIdNguoiThucHien().getHoTen());
    }

    private static String nullSafe(String s) {
        return s != null ? s : "";
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
     * Xoá giá trị của cell nhưng giữ nguyên style/border. Nếu cell chưa tồn tại thì không làm gì.
     * Dùng khi cần di chuyển nhãn từ ô gốc sang ô mới mà không phát sinh thêm ô trống có border.
     */
    private static void clearCellValue(Sheet sheet, int rowIndex, int colIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return;
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return;
        }
        cell.setBlank();
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

    private String safeText(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

    private String resolveChuanDoanForReport(ChiTietDieuTri ctdt) {
        if (ctdt == null) {
            return "";
        }
        String icdText = ctdt.getDsChanDoanIcdText();
        if (icdText != null && !icdText.isBlank()) {
            return icdText;
        }
        return safeText(ctdt.getChuanDoan());
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

    /**
     * Áp viền mỏng (THIN, đen) cho cả 4 cạnh của 1 cell. Clone style hiện có để
     * giữ font và các thuộc tính khác; chỉ ghi đè 4 thuộc tính border.
     * <p>
     * Cache style theo (existingStyleIndex, workbook) để tránh tạo CellStyle mới
     * cho mỗi cell — giữ file xlsx nhẹ và render ổn định hơn.
     */
    private static final Map<Workbook, Map<Short, CellStyle>> BORDER_STYLE_CACHE = new ConcurrentHashMap<>();

    private static void applyThinBorder(Cell cell, Workbook workbook) {
        if (cell == null || workbook == null) {
            return;
        }
        CellStyle existing = cell.getCellStyle();
        Short existingIdx = existing != null ? Short.valueOf(existing.getIndex()) : null;

        Map<Short, CellStyle> cache = BORDER_STYLE_CACHE.computeIfAbsent(workbook, k -> new ConcurrentHashMap<>());
        CellStyle style = existingIdx != null ? cache.get(existingIdx) : null;
        if (style == null) {
            style = workbook.createCellStyle();
            if (existing != null) {
                style.cloneStyleFrom(existing);
            }
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            if (existingIdx != null) {
                cache.put(existingIdx, style);
            }
        }
        cell.setCellStyle(style);
    }

    /**
     * Áp viền mỏng cho toàn bộ cell nằm trong vùng {@code [firstCol..lastCol]} x
     * {@code [firstRow..lastRow]}. Các ô chưa tồn tại sẽ được tạo mới (kèm style clone
     * từ neighbor có value + setBlank) để đảm bảo LibreOffice render border đầy đủ —
     * nếu cell hoàn toàn NULL, border có thể bị bỏ qua khi convert sang PDF.
     * <p>
     * Chú ý: việc {@code createCell} chỉ được gọi trong vùng đã biết là cần border
     * — tránh tạo cell thừa ngoài bảng.
     */
    private static void applyBorderToRange(Sheet sheet, int firstCol, int lastCol,
                                           int firstRow, int lastRow) {
        if (sheet == null || firstCol > lastCol || firstRow > lastRow) {
            return;
        }
        Workbook wb = sheet.getWorkbook();
        for (int r = firstRow; r <= lastRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                row = sheet.createRow(r);
            }
            for (int c = firstCol; c <= lastCol; c++) {
                Cell cell = row.getCell(c);
                if (cell == null) {
                    cell = row.createCell(c);
                    // Clone style từ neighbor bên trái có value để giữ font/fill.
                    CellStyle neighborStyle = null;
                    for (int nc = c - 1; nc >= firstCol; nc--) {
                        Cell nb = row.getCell(nc);
                        if (nb != null && nb.getCellStyle() != null) {
                            neighborStyle = nb.getCellStyle();
                            break;
                        }
                    }
                    if (neighborStyle != null) {
                        CellStyle newStyle = wb.createCellStyle();
                        newStyle.cloneStyleFrom(neighborStyle);
                        cell.setCellStyle(newStyle);
                    }
                    // setBlank để cell không phải NULL — giúp LibreOffice render border.
                    cell.setBlank();
                }
                applyThinBorder(cell, wb);
            }
        }
    }

    /**
     * Gộp vùng "Dịch vụ kỹ thuật" ở hàng header phụ (excel row {@code headerRowIndex + 1})
     * thành 1 ô duy nhất — bỏ border ngăn dọc giữa các cột DV.
     * <ul>
     *   <li>Vùng: [firstCol..lastCol] x headerRowIndex.</li>
     *   <li>Cell giữa (firstCol &lt; c &lt; lastCol): clear borderLeft + borderRight,
     *       giữ borderTop + borderBottom để vẫn ngăn với row trên/dưới.</li>
     *   <li>Cell biên (firstCol, lastCol): giữ nguyên border bao (left/right + top/bottom).</li>
     * </ul>
     * Nếu {@code lastCol < firstCol} (không có cột DV) thì không làm gì.
     */
    private static void mergeDichVuKyThuatHeader(Sheet sheet, int firstCol,
                                                 int lastCol, int headerRowIndex) {
        if (sheet == null || lastCol < firstCol) {
            return;
        }
        Row row = sheet.getRow(headerRowIndex);
        if (row == null) {
            return;
        }
        Workbook wb = sheet.getWorkbook();
        // Clear border left/right cho các cell giữa region (giữ top/bottom).
        for (int c = firstCol + 1; c < lastCol; c++) {
            Cell cell = row.getCell(c);
            if (cell == null) {
                cell = row.createCell(c);
                cell.setBlank();
            }
            CellStyle existing = cell.getCellStyle();
            CellStyle style = wb.createCellStyle();
            if (existing != null) {
                style.cloneStyleFrom(existing);
            }
            style.setBorderLeft(BorderStyle.NONE);
            style.setBorderRight(BorderStyle.NONE);
            // borderTop + borderBottom giữ nguyên từ style gốc (applyBorderToRange đã set).
            cell.setCellStyle(style);
        }
        // Merge region C(headerRowIndex+1) → lastCol(headerRowIndex+1).
        sheet.addMergedRegion(new CellRangeAddress(
                headerRowIndex, headerRowIndex, firstCol, lastCol));
    }
}
