package com.company.clinicportal.service;

import com.company.clinicportal.entity.BenhNhan;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validation rule cho bệnh nhân. Dùng để chặn save nếu vi phạm.
 * Đơn giản, không thay thế Jmix Bean Validation, chỉ bổ sung các rule "yu" theo
 * Quyết định 808/QĐ-BYT (CCCD, BHYT, người giám hộ cho trẻ em, ...).
 *
 * Trả về danh sách {@link ValidationError}; mỗi lỗi gắn với id của field trên
 * view để có thể hiển thị inline (qua Jmix ValidationEvent) thay vì ném
 * exception → dialog lỗi hệ thống.
 */
@Service
public class BenhNhanValidator {

    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{9,12}$");
    private static final Pattern SSKT_PATTERN = Pattern.compile("^[A-Z]{2}\\d{10}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+][0-9\\s-]{6,18}$");
    private static final Pattern GUARDIAN_PHONE_PATTERN = PHONE_PATTERN;

    /**
     * Validate và trả về danh sách lỗi (rỗng nếu hợp lệ).
     *
     * @param bn               bệnh nhân cần validate.
     * @param roughAgeInYears  tuổi ước lượng (để quyết định có bắt buộc người giám hộ không).
     *                         Nếu null sẽ tự tính từ {@code bn.getNgaySinh()}.
     */
    public List<ValidationError> validate(BenhNhan bn, Integer roughAgeInYears) {
        List<ValidationError> errors = new ArrayList<>();
        if (bn == null) {
            errors.add(ValidationError.withoutField("bn-empty"));
            return errors;
        }
        if (isBlank(bn.getHoVaTen())) {
            errors.add(ValidationError.of("hoVaTenField", "bn.hoVaTenRequired"));
        }
        if (bn.getNgaySinh() == null) {
            errors.add(ValidationError.of("ngaySinhField", "bn.ngaySinhRequired"));
        } else {
            LocalDate today = LocalDate.now();
            LocalDate ns = toLocalDate(bn.getNgaySinh());
            if (ns == null) {
                errors.add(ValidationError.of("ngaySinhField", "bn.ngaySinhInvalid"));
            } else if (ns.isAfter(today)) {
                errors.add(ValidationError.of("ngaySinhField", "bn.ngaySinhInFuture"));
            } else if (ns.isBefore(today.minusYears(150))) {
                errors.add(ValidationError.of("ngaySinhField", "bn.ngaySinhTooOld"));
            }
        }
        if (isBlank(bn.getDienThoai())) {
            errors.add(ValidationError.of("dienThoaiField", "bn.dienThoaiRequired"));
        } else if (!PHONE_PATTERN.matcher(bn.getDienThoai().trim()).matches()) {
            errors.add(ValidationError.of("dienThoaiField", "bn.dienThoaiInvalid"));
        }
        if (isBlank(bn.getDiaChi())) {
            errors.add(ValidationError.of("diaChiField", "bn.diaChiRequired"));
        }
        if (!isBlank(bn.getMaDinhDanhCongDan())
                && !CCCD_PATTERN.matcher(bn.getMaDinhDanhCongDan().trim()).matches()) {
            errors.add(ValidationError.withoutField("bn.cccdInvalid"));
        }
        if (!isBlank(bn.getMaSoTheBaoHiemYTe())) {
            String bhyt = bn.getMaSoTheBaoHiemYTe().trim().toUpperCase();
            if (!SSKT_PATTERN.matcher(bhyt).matches()) {
                errors.add(ValidationError.withoutField("bn.bhytInvalid"));
            }
        }
        if (bn.getCanNang() != null && (bn.getCanNang() < 0 || bn.getCanNang() > 500)) {
            errors.add(ValidationError.withoutField("bn.canNangOutOfRange"));
        }
        int age = roughAgeInYears != null ? roughAgeInYears : roughAgeFromNgaySinh(bn.getNgaySinh());
        // Người giám hộ bắt buộc nếu bệnh nhân dưới 18 tuổi.
        if (age >= 0 && age < 18) {
            if (isBlank(bn.getHoTenNguoiThan())) {
                errors.add(ValidationError.of("hoTenNguoiThanField", "bn.giamHoHoTenRequired"));
            }
            if (isBlank(bn.getQuanHeVoiBenhNhan())) {
                errors.add(ValidationError.of("quanHeVoiBenhNhanField", "bn.giamHoQuanHeRequired"));
            }
            if (!isBlank(bn.getSdtNguoiThan())
                    && !GUARDIAN_PHONE_PATTERN.matcher(bn.getSdtNguoiThan().trim()).matches()) {
                errors.add(ValidationError.of("sdtNguoiThanField", "bn.giamHoSdtInvalid"));
            }
        }
        return errors;
    }

    /**
     * Backwards-compatible overload: tích luỹ chỉ message key vào {@code sink}
     * (bỏ qua fieldKey). Giữ để tương thích với code cũ / test cũ nếu cần.
     */
    public void validate(BenhNhan bn, List<String> sink, Integer roughAgeInYears) {
        for (ValidationError e : validate(bn, roughAgeInYears)) {
            sink.add(e.messageKey());
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