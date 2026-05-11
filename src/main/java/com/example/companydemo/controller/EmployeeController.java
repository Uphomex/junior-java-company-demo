package com.example.companydemo.controller;

import com.example.companydemo.common.ApiResponse;
import com.example.companydemo.common.PageResult;
import com.example.companydemo.dto.EmployeeCreateRequest;
import com.example.companydemo.dto.EmployeeUpdateRequest;
import com.example.companydemo.entity.Employee;
import com.example.companydemo.service.EmployeeService;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ApiResponse<PageResult<Employee>> page(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Long departmentId,
                                                  @RequestParam(defaultValue = "1") @Min(1) int pageNum,
                                                  @RequestParam(defaultValue = "10") @Min(1) @Max(100) int pageSize) {
        return ApiResponse.success(employeeService.page(keyword, departmentId, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Employee> detail(@PathVariable Long id) {
        return ApiResponse.success(employeeService.getById(id));
    }

    @PostMapping
    public ApiResponse<Employee> create(@Valid @RequestBody EmployeeCreateRequest request) {
        return ApiResponse.success(employeeService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Employee> update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateRequest request) {
        return ApiResponse.success(employeeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> leave(@PathVariable Long id) {
        employeeService.leave(id);
        return ApiResponse.success();
    }
}

