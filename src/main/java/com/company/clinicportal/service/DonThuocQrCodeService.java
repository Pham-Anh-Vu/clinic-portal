package com.company.clinicportal.service;

import com.company.clinicportal.entity.DonThuoc;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;

/**
 * Sinh QR code cho đơn thuốc. Payload đơn giản ở dạng JSON-like text chứa
 * mã đơn, ngày kê, tên bệnh nhân. QR sẽ hiển thị ở preview/print đơn.
 *
 * <p>Trong Giai đoạn 5 sẽ chuyển sang QR code ký số (signed JWT) để tra cứu.</p>
 */
@Service
public class DonThuocQrCodeService {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String buildPayload(DonThuoc dt) {
        if (dt == null) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{")
          .append("\"ma\":\"").append(nullSafe(dt.getMaDonThuoc())).append("\",")
          .append("\"ngay\":\"").append(dt.getNgayKe() == null ? "" : YMD.format(dt.getNgayKe().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate())).append("\",")
          .append("\"bn\":\"").append(nullSafe(dt.getHoVaTenBenhNhan())).append("\",")
          .append("\"bs\":\"").append(nullSafe(dt.getTenBacSi())).append("\"")
          .append("}");
        return sb.toString();
    }

    public String qrPngDataUri(DonThuoc dt, int width, int height) {
        String payload = buildPayload(dt);
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix m = writer.encode(payload, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(m, "PNG", os);
            String b64 = Base64.getEncoder().encodeToString(os.toByteArray());
            return "data:image/png;base64," + b64;
        } catch (WriterException | IOException e) {
            return null;
        }
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
