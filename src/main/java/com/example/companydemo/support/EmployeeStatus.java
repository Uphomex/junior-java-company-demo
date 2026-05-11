package com.example.companydemo.support;

public enum EmployeeStatus {

    ACTIVE(1, "在职"),
    LEFT(0, "离职");

    private final int code;
    private final String description;

    EmployeeStatus(int code, String description) {
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

