package com.example.companydemo.mapper;

import com.example.companydemo.entity.Department;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DepartmentMapper {

    List<Department> findAllActive();

    Department findById(@Param("id") Long id);

    Department findByCode(@Param("code") String code);

    int insert(Department department);

    int countActiveEmployees(@Param("id") Long id);
}

