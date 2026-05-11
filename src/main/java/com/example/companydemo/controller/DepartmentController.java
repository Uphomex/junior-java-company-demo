package com.example.companydemo.controller;

import com.example.companydemo.common.ApiResponse;
import com.example.companydemo.dto.DepartmentCreateRequest;
import com.example.companydemo.entity.Department;
import com.example.companydemo.service.DepartmentService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ApiResponse<List<Department>> list() {
        return ApiResponse.success(departmentService.listActive());
    }

    @PostMapping
    public ApiResponse<Department> create(@Valid @RequestBody DepartmentCreateRequest request) {
        return ApiResponse.success(departmentService.create(request));
    }
}

