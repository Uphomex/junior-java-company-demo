package com.example.companydemo.support;

public enum AttendanceStatus {

    NORMAL(1, "正常"),
    LATE(2, "迟到"),
    EARLY_LEAVE(3, "早退");

    private final int code;
    private final String description;

    AttendanceStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}

