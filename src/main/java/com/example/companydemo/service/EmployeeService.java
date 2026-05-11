package com.example.companydemo.service;

import com.example.companydemo.common.BusinessException;
import com.example.companydemo.common.PageResult;
import com.example.companydemo.dto.EmployeeCreateRequest;
import com.example.companydemo.dto.EmployeeUpdateRequest;
import com.example.companydemo.entity.Employee;
import com.example.companydemo.mapper.EmployeeMapper;
import com.example.companydemo.support.EmployeeStatus;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final DepartmentService departmentService;

    public EmployeeService(EmployeeMapper employeeMapper, DepartmentService departmentService) {
        this.employeeMapper = employeeMapper;
        this.departmentService = departmentService;
    }

    public PageResult<Employee> page(String keyword, Long departmentId, int pageNum, int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;

        List<Employee> records = employeeMapper.search(keyword, departmentId, offset, safePageSize);
        int total = employeeMapper.count(keyword, departmentId);
        return new PageResult<>(records, total, safePageNum, safePageSize);
    }

    public Employee getById(Long id) {
        Employee employee = employeeMapper.findById(id);
        if (employee == null || !employee.isActive()) {
            throw new BusinessException("员工不存在或已离职");
        }
        return employee;
    }

    @Transactional
    public Employee create(EmployeeCreateRequest request) {
        departmentService.getActiveDepartment(request.getDepartmentId());
        ensurePhoneNotUsed(request.getPhone(), null);

        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setPhone(request.getPhone());
        employee.setEmail(request.getEmail());
        employee.setDepartmentId(request.getDepartmentId());
        employee.setPosition(request.getPosition());
        employee.setHireDate(request.getHireDate());
        employee.setSalary(request.getSalary());
        employee.setStatus(EmployeeStatus.ACTIVE.getCode());

        employeeMapper.insert(employee);
        return employee;
    }

    @Transactional
    public Employee update(Long id, EmployeeUpdateRequest request) {
        Employee employee = getById(id);
        departmentService.getActiveDepartment(request.getDepartmentId());
        ensurePhoneNotUsed(request.getPhone(), id);

        employee.setName(request.getName());
        employee.setPhone(request.getPhone());
        employee.setEmail(request.getEmail());
        employee.setDepartmentId(request.getDepartmentId());
        employee.setPosition(request.getPosition());
        employee.setHireDate(request.getHireDate());
        employee.setSalary(request.getSalary());

        int rows = employeeMapper.update(employee);
        if (rows == 0) {
            throw new BusinessException("员工修改失败");
        }
        return getById(id);
    }

    @Transactional
    public void leave(Long id) {
        Employee employee = getById(id);
        int departmentEmployeeCount = departmentService.countActiveEmployees(employee.getDepartmentId());
        if (departmentEmployeeCount <= 1) {
            throw new BusinessException("部门至少保留 1 名在职员工");
        }
        int rows = employeeMapper.disable(id);
        if (rows == 0) {
            throw new BusinessException("员工离职失败");
        }
    }

    private void ensurePhoneNotUsed(String phone, Long selfId) {
        Employee exists = employeeMapper.findByPhone(phone);
        if (exists != null && !exists.getId().equals(selfId)) {
            throw new BusinessException("手机号已被其他员工使用");
        }
    }
}

