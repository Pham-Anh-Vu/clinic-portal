package com.company.clinicportal.lienthong.dto;

import java.util.List;

/**
 * Request/Response cho API thêm bác sĩ.
 */
public class BacSiDtos {

    public static class AddBacSiRequest {
        public String ma_lien_thong_co_so_kham_chua_benh;
        public String ma_lien_thong_bac_si;
        public String password;
        public String ho_va_ten;
        public String so_cchn;
        public String ngay_cap_cchn;
        public String noi_cap_cchn;
        public String chuyen_mon;
        public String so_dien_thoai;
        public String email;
    }

    public static class AddBacSiResponse {
        public boolean success;
        public String error_code;
        public String message;
        public List<String> validation_errors;
    }

    public static class LoginBacSiRequest {
        public String ma_lien_thong_co_so_kham_chua_benh;
        public String ma_lien_thong_bac_si;
        public String password;
    }

    public static class LoginBacSiResponse {
        public boolean success;
        public String token;
        public String expires_at;
        public String error_code;
        public String message;
    }

    public static class LoginNhanVienRequest {
        public String ma_lien_thong_co_so_kham_chua_benh;
        public String username;
        public String password;
    }

    public static class LoginNhanVienResponse {
        public boolean success;
        public String token;
        public String expires_at;
        public String error_code;
    }
}
