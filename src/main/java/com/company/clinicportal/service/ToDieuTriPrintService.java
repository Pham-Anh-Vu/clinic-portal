package com.company.clinicportal.service;

import com.company.clinicportal.entity.BenhNhan;
import com.company.clinicportal.entity.ChiTietDieuTri;
import com.company.clinicportal.entity.ToDieuTri;
import com.company.clinicportal.entity.ToDieuTriKyThuat;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import io.jmix.core.DataManager;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ToDieuTriPrintService {

    private static final String TO_DIEU_TRI_TEMPLATE_HTML = "/reports/tờ điều trị BN BCB.html";
    private static final Set<String> RAW_HTML_PLACEHOLDERS = Set.of("${ROWS}");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private final DataManager dataManager;

    public ToDieuTriPrintService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public byte[] generatePdf(ChiTietDieuTri chiTietDieuTri) throws Exception {
        List<ToDieuTri> lines = dataManager.load(ToDieuTri.class)
                .query("select e from ToDieuTri e where e.chiTietDieuTri = :ctdt order by e.tuNgay, e.id")
                .parameter("ctdt", chiTietDieuTri)
                .fetchPlan(fp -> fp
                        .addFetchPlan("_base")
                        .add("idNguoiThucHien", n -> n.addFetchPlan("_base"))
                        .add("idBacSiChiDinh", b -> b.addFetchPlan("_base"))
                        .add("kyThuatList", k -> k
                                .addFetchPlan("_base")
                                .add("idDichVu", d -> d.addFetchPlan("_base"))))
                .list();

        BenhNhan benhNhan = chiTietDieuTri.getIdBenhNhan();
        if (benhNhan != null && benhNhan.getId() != null) {
            benhNhan = dataManager.load(BenhNhan.class).id(benhNhan.getId()).optional().orElse(benhNhan);
        }

        StringBuilder rows = new StringBuilder();
        for (ToDieuTri line : lines) {
            appendPrintRow(rows, line);
        }
        if (rows.isEmpty()) {
            rows.append("<tr><td colspan=\"6\" style=\"text-align:center;\">(Chưa có dòng điều trị)</td></tr>");
        }

        Map<String, String> values = new HashMap<>();
        values.put("${BenhNhan.hoVaTen}", safeText(benhNhan != null ? benhNhan.getHoVaTen() : null));
        values.put("${BenhNhan.ngaySinh}", formatDate(benhNhan != null ? benhNhan.getNgaySinh() : null));
        values.put("${BenhNhan.tuoi}", safeText(benhNhan != null ? BenhNhan.calculateTuoi(benhNhan.getNgaySinh()) : null));
        values.put("${BenhNhan.gioiTinh}", formatGioiTinh(benhNhan != null ? benhNhan.getGioiTinh() : null));
        values.put("${BenhNhan.diaChi}", safeText(benhNhan != null ? benhNhan.getDiaChi() : null));
        values.put("${ChiTietDieuTri.chuanDoan}", safeText(chiTietDieuTri.getChuanDoan()));
        values.put("${ROWS}", rows.toString());

        String html = applyTemplateValues(loadHtmlTemplate(TO_DIEU_TRI_TEMPLATE_HTML), values);
        return htmlToPdfBytes(html);
    }

    private void appendPrintRow(StringBuilder rows, ToDieuTri line) {
        String ngayGio = formatKhoangNgay(line.getTuNgay(), line.getDenNgay());
        String tenDichVu = formatKyThuatList(line.getKyThuatList(), true);
        String thoiGian = formatKyThuatList(line.getKyThuatList(), false);
        String nguoiThucHien = line.getIdNguoiThucHien() != null ? safeText(line.getIdNguoiThucHien().getHoTen()) : "";
        String bacSi = line.getIdBacSiChiDinh() != null ? safeText(line.getIdBacSiChiDinh().getHoTen()) : "";

        rows.append("<tr>")
                .append("<td>").append(escapeHtml(ngayGio)).append("</td>")
                .append("<td>").append(escapeHtml(safeText(line.getMoTaDienBienBenh()))).append("</td>")
                .append("<td>").append(escapeHtml(tenDichVu)).append("</td>")
                .append("<td style=\"text-align:center;\">").append(escapeHtml(thoiGian)).append("</td>")
                .append("<td>").append(escapeHtml(nguoiThucHien)).append("</td>")
                .append("<td>").append(escapeHtml(bacSi)).append("</td>")
                .append("</tr>");
    }

    private String formatKyThuatList(List<ToDieuTriKyThuat> list, boolean tenDichVu) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ToDieuTriKyThuat kt : list) {
            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            if (tenDichVu) {
                String name = kt.getIdDichVu() != null ? safeText(kt.getIdDichVu().getTenDichVu()) : "";
                sb.append("• ").append(name);
            } else {
                sb.append("• ").append(kt.getThoiGianPhut() != null ? kt.getThoiGianPhut() : "");
            }
        }
        return sb.toString();
    }

    private String formatKhoangNgay(Date tuNgay, Date denNgay) {
        String tu = formatDate(tuNgay);
        String den = formatDate(denNgay);
        if (!tu.isBlank() && !den.isBlank()) {
            return tu + " Đến " + den;
        }
        return tu.isBlank() ? den : tu;
    }

    private String loadHtmlTemplate(String templateResource) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(templateResource)) {
            if (inputStream == null) {
                throw new IOException("Template not found: " + templateResource);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String applyTemplateValues(String htmlTemplate, Map<String, String> values) {
        String result = htmlTemplate;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() != null ? entry.getValue() : "";
            if (RAW_HTML_PLACEHOLDERS.contains(key)) {
                result = result.replace(key, value);
            } else {
                result = result.replace(key, escapeHtml(value));
            }
        }
        return result;
    }

    private byte[] htmlToPdfBytes(String html) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(Jsoup.parse(html));
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            registerWindowsFonts(builder);
            builder.withW3cDocument(w3cDoc, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        }
    }

    private void registerWindowsFonts(PdfRendererBuilder builder) {
        File fontsDir = new File("C:/Windows/Fonts");
        if (!fontsDir.isDirectory()) {
            return;
        }
        registerFontIfExists(builder, new File(fontsDir, "times.ttf"), "Times New Roman");
        registerFontIfExists(builder, new File(fontsDir, "arial.ttf"), "Arial");
        registerFontIfExists(builder, new File(fontsDir, "tahoma.ttf"), "Tahoma");
    }

    private void registerFontIfExists(PdfRendererBuilder builder, File file, String family) {
        if (file.isFile()) {
            builder.useFont(file, family);
        }
    }

    private String escapeHtml(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String safeText(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

    private String formatDate(Date date) {
        return date != null ? DATE_FORMAT.format(date) : "";
    }

    private String formatGioiTinh(Object gioiTinh) {
        if (gioiTinh == null) {
            return "";
        }
        String raw = String.valueOf(gioiTinh).trim();
        if (raw.equalsIgnoreCase("NU")) {
            return "Nữ";
        }
        if (raw.equalsIgnoreCase("NAM")) {
            return "Nam";
        }
        return raw;
    }
}
