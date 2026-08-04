package com.company.clinicportal.enumentity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

/**
 * Trạng thái đơn thuốc liên thông 808/QĐ-BYT.
 */
public enum TrangThaiDonThuoc implements EnumClass<String> {

    NHAP("Nhap", "Nháp"),
    CHO_GUI("ChoGui", "Chờ gửi"),
    DA_GUI("DaGui", "Đã gửi"),
    GUI_LOI("GuiLoi", "Gửi lỗi"),
    HUY("Huy", "Đã huỷ"),
    PHAT_HANH("PhatHanh", "Đã phát hành"),
    TU_CHOI("TuChoi", "Bị từ chối");

    private final String id;
    private final String tenHienThi;

    TrangThaiDonThuoc(String id, String tenHienThi) {
        this.id = id;
        this.tenHienThi = tenHienThi;
    }

    public String getId() { return id; }
    public String getTenHienThi() { return tenHienThi; }

    @Nullable
    public static TrangThaiDonThuoc fromId(String id) {
        if (id == null) return null;
        for (TrangThaiDonThuoc v : values()) {
            if (v.id.equalsIgnoreCase(id)) return v;
        }
        return null;
    }
}
