package com.company.clinicportal.service;

import com.company.clinicportal.entity.BenhNhan;
import io.jmix.core.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Validation rule cho bệnh nhân. Dùng để chặn save nếu vi phạm.
 * Đơn giản, không thay thế Jmix Bean Validation, chỉ bổ sung các rule "yu" theo
 * Quyết định 808/QĐ-BYT (CCCD, BHYT, người giám hộ cho trẻ em, ...).
 */
@Service
public class BenhNhanValidator {

    @Autowired
    private Messages messages;

    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{9,12}$");
    private static final Pattern SSKT_PATTERN = Pattern.compile("^[A-Z]{2}\\d{10}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+][0-9\\s-]{6,18}$");
    private static final Pattern GUARDIAN_PHONE_PATTERN = PHONE_PATTERN;

    /**
     * Tích luỹ lỗi vào {@code errors}, mỗi lỗi là 1 message key.
     * @param roughAgeInYears tuổi ước lượng (để quyết định có bắt buộc người giám hộ không).
     */
    public void validate(BenhNhan bn, java.util.List<String> errors, Integer roughAgeInYears) {
        if (bn == null) {
            errors.add("bn-empty");
            return;
        }
        if (isBlank(bn.getHoVaTen())) {
            errors.add("bn.hoVaTenRequired");
        }
        if (bn.getNgaySinh() == null) {
            errors.add("bn.ngaySinhRequired");
        } else {
            LocalDate today = LocalDate.now();
            LocalDate ns = toLocalDate(bn.getNgaySinh());
            if (ns == null) {
                errors.add("bn.ngaySinhInvalid");
            } else if (ns.isAfter(today)) {
                errors.add("bn.ngaySinhInFuture");
            } else if (ns.isBefore(today.minusYears(150))) {
                errors.add("bn.ngaySinhTooOld");
            }
        }
        if (isBlank(bn.getDienThoai())) {
            errors.add("bn.dienThoaiRequired");
        } else if (!PHONE_PATTERN.matcher(bn.getDienThoai().trim()).matches()) {
            errors.add("bn.dienThoaiInvalid");
        }
        if (isBlank(bn.getDiaChi())) {
            errors.add("bn.diaChiRequired");
        }
        if (!isBlank(bn.getMaDinhDanhCongDan())
                && !CCCD_PATTERN.matcher(bn.getMaDinhDanhCongDan().trim()).matches()) {
            errors.add("bn.cccdInvalid");
        }
        if (!isBlank(bn.getMaSoTheBaoHiemYTe())) {
            String bhyt = bn.getMaSoTheBaoHiemYTe().trim().toUpperCase();
            if (!SSKT_PATTERN.matcher(bhyt).matches()) {
                errors.add("bn.bhytInvalid");
            }
        }
        if (bn.getCanNang() != null && (bn.getCanNang() < 0 || bn.getCanNang() > 500)) {
            errors.add("bn.canNangOutOfRange");
        }
        int age = roughAgeInYears != null ? roughAgeInYears : roughAgeFromNgaySinh(bn.getNgaySinh());
        // Người giám hộ bắt buộc nếu bệnh nhân dưới 18 tuổi.
        if (age >= 0 && age < 18) {
            if (isBlank(bn.getHoTenNguoiThan())) {
                errors.add("bn.giamHoHoTenRequired");
            }
            if (isBlank(bn.getQuanHeVoiBenhNhan())) {
                errors.add("bn.giamHoQuanHeRequired");
            }
            if (!isBlank(bn.getSdtNguoiThan())
                    && !GUARDIAN_PHONE_PATTERN.matcher(bn.getNguoiGiamHoSoDienThoai().trim()).matches()) {
                errors.add("bn.giamHoSdtInvalid");
            }
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static LocalDate toLocalDate(Date d) {
        if (d == null) return null;
        if (d instanceof java.sql.Date sql) return sql.toLocalDate();
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    private static int roughAgeFromNgaySinh(Date ngaySinh) {
        LocalDate ns = toLocalDate(ngaySinh);
        if (ns == null) return -1;
        LocalDate today = LocalDate.now();
        if (ns.isAfter(today)) return -1;
        return java.time.Period.between(ns, today).getYears();
    }
}
