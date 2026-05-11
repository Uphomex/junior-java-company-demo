package com.example.companydemo.mapper;

import com.example.companydemo.entity.AttendanceRecord;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AttendanceMapper {

    AttendanceRecord findByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("workDate") LocalDate workDate);

    List<AttendanceRecord> findByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    int insert(AttendanceRecord record);

    int updateCheckOut(AttendanceRecord record);
}

