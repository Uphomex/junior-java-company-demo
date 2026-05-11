package com.example.companydemo.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AttendanceCheckOutRequest {

    @NotNull(message = "员工 ID 不能为空")
    private Long employeeId;

    @Size(max = 255, message = "备注最多 255 个字符")
    private String note;

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

