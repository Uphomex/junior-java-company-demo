package com.example.companydemo.service;

import com.example.companydemo.common.BusinessException;
import com.example.companydemo.dto.DepartmentCreateRequest;
import com.example.companydemo.entity.Department;
import com.example.companydemo.mapper.DepartmentMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentService {

    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    public List<Department> listActive() {
        return departmentMapper.findAllActive();
    }

    public Department getActiveDepartment(Long id) {
        Department department = departmentMapper.findById(id);
        if (department == null || !Integer.valueOf(1).equals(department.getStatus())) {
            throw new BusinessException("部门不存在或已停用");
        }
        return department;
    }

    @Transactional
    public Department create(DepartmentCreateRequest request) {
        Department exists = departmentMapper.findByCode(request.getCode());
        if (exists != null) {
            throw new BusinessException("部门编码已存在");
        }

        Department department = new Department(request.getCode(), request.getName());
        departmentMapper.insert(department);
        return department;
    }

    public int countActiveEmployees(Long id) {
        return departmentMapper.countActiveEmployees(id);
    }
}

