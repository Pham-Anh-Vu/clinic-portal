package com.company.clinicportal.view.donthuoc;

import com.company.clinicportal.entity.DonThuoc;
import com.company.clinicportal.entity.DonThuocChanDoan;
import com.company.clinicportal.entity.DonThuocChiTiet;
import com.company.clinicportal.entity.DonThuocDotDung;
import com.company.clinicportal.service.DonThuocQrCodeService;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Dialog preview đơn thuốc (read-only). Hiển thị đầy đủ thông tin bệnh nhân,
 * chẩn đoán, dòng thuốc, lời dặn. Có nút Print mở trình in của trình duyệt
 * (Ctrl+P) để xuất PDF hoặc in trực tiếp.
 *
 * <p>Không dùng JasperReports/BIRT; render bằng HTML tĩnh đơn giản, dễ tùy biến CSS.</p>
 */
@Route(value = "don-thuoc-preview", layout = MainView.class)
@ViewController(id = "ltcs_DonThuoc.preview")
@ViewDescriptor(path = "don-thuoc-preview-dialog-view.xml")
@DialogMode(width = "80em", height = "60em")
public class DonThuocPreviewDialogView extends StandardView {

    @Autowired
    private DonThuocQrCodeService qrService;

    private DonThuoc donThuoc;
    @ViewComponent
    private Div contentDiv;
    @ViewComponent
    private JmixButton printButton;

    public void setDonThuoc(DonThuoc donThuoc) {
        this.donThuoc = donThuoc;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        render();
    }

    @Subscribe("printButton")
    public void onPrint(com.vaadin.flow.component.ClickEvent<JmixButton> e) {
        // Mở trình in của trình duyệt - người dùng có thể chọn "Lưu PDF"
        getUI().ifPresent(ui -> ui.getPage().executeJs("window.print();"));
    }

    @Subscribe(id = "closeButton")
    public void onClose(com.vaadin.flow.component.ClickEvent<JmixButton> e) {
        close(StandardOutcome.CLOSE);
    }

    public void render() {
        if (donThuoc == null) {
            contentDiv.add(new Html("<p>Không có dữ liệu đơn thuốc.</p>"));
            return;
        }
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='don-thuoc-preview'>")
          .append("<style>")
          .append(".don-thuoc-preview { font-family: Arial, sans-serif; padding: 1em; color: #222; }")
          .append(".don-thuoc-preview h1 { text-align: center; margin: 0 0 0.25em 0; font-size: 1.4em; }")
          .append(".don-thuoc-preview .ma-don { text-align: right; color: #888; font-size: 0.9em; }")
          .append(".don-thuoc-preview table { width: 100%; border-collapse: collapse; margin: 0.5em 0; }")
          .append(".don-thuoc-preview th, .don-thuoc-preview td { border: 1px solid #999; padding: 4px 8px; text-align: left; }")
          .append(".don-thuoc-preview th { background: #f0f0f0; }")
          .append(".don-thuoc-preview .info-row td { border: none; padding: 2px 0; }")
          .append(".don-thuoc-preview .info-row .label { width: 22%; color: #666; }")
          .append(".don-thuoc-preview .info-row .val { width: 28%; }")
          .append(".don-thuoc-preview .signature { margin-top: 2em; display: flex; gap: 2em; }")
          .append(".don-thuoc-preview .signature .column { flex: 1; text-align: center; }")
          .append(".don-thuoc-preview .qr-placeholder { width: 100px; height: 100px; border: 1px dashed #999; display: inline-block; line-height: 100px; color: #aaa; font-size: 0.8em; }")
          .append("@media print { .no-print { display: none; } }")
          .append("</style>")
          .append("<h1>ĐƠN THUỐC NGOẠI TRÚ</h1>")
          .append("<div class='ma-don'>Mã đơn: <b>").append(esc(donThuoc.getMaDonThuoc())).append("</b></div>");

        // Bệnh nhân snapshot
        sb.append("<h3>I. Thông tin bệnh nhân</h3>")
          .append("<table>")
          .append(row("Họ tên", nullSafe(donThuoc.getHoVaTenBenhNhan())))
          .append(row("Ngày sinh", donThuoc.getNgaySinh() == null ? "" : df.format(donThuoc.getNgaySinh())))
          .append(row("Giới tính", nullSafe(donThuoc.getGioiTinh())))
          .append(row("CCCD", nullSafe(donThuoc.getMaDinhDanhCongDan())))
          .append(row("Mã định danh y tế", nullSafe(donThuoc.getMaDinhDanhYTe())))
          .append(row("SĐT", nullSafe(donThuoc.getSoDienThoai())))
          .append(row("Địa chỉ", nullSafe(donThuoc.getDiaChi())))
          .append(row("Cân nặng", donThuoc.getCanNang() == null ? "" : donThuoc.getCanNang() + " kg"))
          .append(row("Người giám hộ", nullSafe(donThuoc.getNguoiGiamHoHoTen())))
          .append("</table>");

        // Chẩn đoán
        sb.append("<h3>II. Chẩn đoán</h3>");
        List<DonThuocChanDoan> cds = donThuoc.getChanDoans();
        if (cds == null || cds.isEmpty()) {
            sb.append("<p><i>Chưa có chẩn đoán.</i></p>");
        } else {
            sb.append("<table><thead><tr><th>STT</th><th>Mã ICD</th><th>Tên bệnh</th><th>Kết luận</th></tr></thead><tbody>");
            for (DonThuocChanDoan cd : cds) {
                sb.append("<tr><td>").append(cd.getStt() == null ? "" : cd.getStt()).append("</td>")
                  .append("<td>").append(esc(cd.getMaIcdSnapshot())).append("</td>")
                  .append("<td>").append(esc(cd.getTenIcdSnapshot())).append("</td>")
                  .append("<td>").append(esc(cd.getKetLuan())).append("</td></tr>");
            }
            sb.append("</tbody></table>");
        }

        // Loại đơn + hình thức
        sb.append("<h3>III. Thông tin đơn</h3><table>")
          .append(row("Loại đơn", nullSafe(donThuoc.getLoaiDon())))
          .append(row("Hình thức điều trị", nullSafe(donThuoc.getHinhThucDieuTri())))
          .append(row("Ngày kê", donThuoc.getNgayKe() == null ? "" : df.format(donThuoc.getNgayKe())))
          .append(row("Ngày tái khám", donThuoc.getNgayTaiKham() == null ? "" : df.format(donThuoc.getNgayTaiKham())))
          .append(row("Bác sĩ", nullSafe(donThuoc.getTenBacSi())))
          .append("</table>");

        // Dòng thuốc
        sb.append("<h3>IV. Dòng thuốc</h3>");
        List<DonThuocChiTiet> lines = donThuoc.getChiTiets();
        if (lines == null || lines.isEmpty()) {
            sb.append("<p><i>Chưa có dòng thuốc.</i></p>");
        } else {
            sb.append("<table><thead><tr>")
              .append("<th>STT</th><th>Mã</th><th>Tên thuốc</th><th>Hàm lượng</th><th>SL</th><th>Liều dùng</th><th>Cách dùng</th><th>Ghi chú</th>")
              .append("</tr></thead><tbody>");
            for (DonThuocChiTiet ct : lines) {
                sb.append("<tr>")
                  .append("<td>").append(ct.getStt() == null ? "" : ct.getStt()).append("</td>")
                  .append("<td>").append(esc(ct.getMaThuocSnapshot())).append("</td>")
                  .append("<td>").append(esc(ct.getTenThuocSnapshot())).append("</td>")
                  .append("<td>").append(esc(ct.getHamLuongSnapshot())).append("</td>")
                  .append("<td>").append(ct.getSoLuong() == null ? "" : ct.getSoLuong().toPlainString()).append("</td>")
                  .append("<td>").append(esc(ct.getLieuDung())).append("</td>")
                  .append("<td>").append(esc(ct.getCachDung())).append("</td>")
                  .append("<td>").append(esc(ct.getGhiChu())).append("</td>")
                  .append("</tr>");
            }
            sb.append("</tbody></table>");
        }

        // Đợt dùng (YHCT)
        if (donThuoc.getDotDungs() != null && !donThuoc.getDotDungs().isEmpty()) {
            sb.append("<h3>V. Đợt dùng</h3><table><thead><tr><th>Đợt</th><th>Từ ngày</th><th>Đến ngày</th><th>Số thang thuốc</th></tr></thead><tbody>");
            for (DonThuocDotDung dd : donThuoc.getDotDungs()) {
                sb.append("<tr>")
                  .append("<td>").append(dd.getSoDot() == null ? "" : dd.getSoDot()).append("</td>")
                  .append("<td>").append(dd.getTuNgay() == null ? "" : df.format(dd.getTuNgay())).append("</td>")
                  .append("<td>").append(dd.getDenNgay() == null ? "" : df.format(dd.getDenNgay())).append("</td>")
                  .append("<td>").append(dd.getSoThangThuoc() == null ? "" : dd.getSoThangThuoc()).append("</td>")
                  .append("</tr>");
            }
            sb.append("</tbody></table>");
        }

        // Lời dặn
        if (donThuoc.getLoiDan() != null && !donThuoc.getLoiDan().isBlank()) {
            sb.append("<h3>VI. Lời dặn</h3>")
              .append("<p>").append(esc(donThuoc.getLoiDan())).append("</p>");
        }

        // Ký tên (chữ ký số + QR placeholder)
        String qrDataUri = qrService != null ? qrService.qrPngDataUri(donThuoc, 120, 120) : null;
        if (qrDataUri == null) qrDataUri = "";
        sb.append("<div class='signature'>")
          .append("<div class='column'>Bệnh nhân<br><i>(Ký, ghi rõ họ tên)</i></div>")
          .append("<div class='column'>Bác sĩ kê đơn<br><i>").append(esc(donThuoc.getTenBacSi())).append("</i><br>")
          .append(qrDataUri.isEmpty()
                  ? "<div class='qr-placeholder'>QR / Ký số</div>"
                  : "<img alt='QR' class='qr-img' src='" + qrDataUri + "' width='120' height='120'/>")
          .append("</div>")
          .append("</div>")
          .append("</div>");

        contentDiv.removeAll();
        contentDiv.add(new Html(sb.toString()));
    }

    private static String row(String label, String value) {
        return "<tr class='info-row'><td class='label'>" + esc(label) + "</td>"
                + "<td class='val' colspan='3'>" + esc(value) + "</td></tr>";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private static String nullSafe(Object o) {
        return o == null ? "" : o.toString();
    }
}
