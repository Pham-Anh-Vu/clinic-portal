package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

import java.util.Calendar;
import java.util.Date;


public enum CaLamViec implements EnumClass<String> {

    TOI("toi"),
    SANG("sang");

    /** Giờ bắt đầu từ 17:30 trở đi được tính là ca tối. */
    private static final int TOI_SHIFT_HOUR = 17;
    private static final int TOI_SHIFT_MINUTE = 30;

    private final String id;

    CaLamViec(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static CaLamViec fromId(String id) {
        for (CaLamViec at : CaLamViec.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }

    public static CaLamViec fromGioBatDau(@Nullable Date gioBatDau) {
        return resolveFromGioBatDau(gioBatDau, null);
    }

    public static CaLamViec resolveFromGioBatDau(@Nullable Date gioBatDau, @Nullable CaLamViec fallback) {
        if (gioBatDau == null) {
            return fallback != null ? fallback : SANG;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(gioBatDau);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        if (hour > TOI_SHIFT_HOUR || (hour == TOI_SHIFT_HOUR && minute >= TOI_SHIFT_MINUTE)) {
            return TOI;
        }
        return SANG;
    }
}