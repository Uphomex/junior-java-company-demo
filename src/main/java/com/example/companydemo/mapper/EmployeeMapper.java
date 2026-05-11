package com.example.companydemo.mapper;

import com.example.companydemo.entity.Employee;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EmployeeMapper {

    List<Employee> search(@Param("keyword") String keyword,
                          @Param("departmentId") Long departmentId,
                          @Param("offset") int offset,
                          @Param("pageSize") int pageSize);

    int count(@Param("keyword") String keyword, @Param("departmentId") Long departmentId);

    Employee findById(@Param("id") Long id);

    Employee findByPhone(@Param("phone") String phone);

    int insert(Employee employee);

    int update(Employee employee);

    int disable(@Param("id") Long id);
}

