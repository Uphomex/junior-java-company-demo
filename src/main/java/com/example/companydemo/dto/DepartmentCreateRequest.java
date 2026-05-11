package com.example.companydemo.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class DepartmentCreateRequest {

    @NotBlank(message = "部门编码不能为空")
    @Pattern(regexp = "^[A-Z0-9_]{2,20}$", message = "部门编码只能包含大写字母、数字、下划线，长度 2-20")
    private String code;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 64, message = "部门名称最多 64 个字符")
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

