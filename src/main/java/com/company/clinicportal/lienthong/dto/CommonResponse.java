package com.company.clinicportal.lienthong.dto;

/**
 * Response chuẩn BYT: trường {@code result.data} hoặc {@code result.errors}.
 */
public class CommonResponse {

    public boolean success;
    public Integer code;
    public String message;
    public Object data;
    public Object errors;
}
