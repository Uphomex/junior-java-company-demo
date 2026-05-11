package com.example.companydemo.service;

import com.example.companydemo.common.BusinessException;
import com.example.companydemo.dto.AttendanceCheckInRequest;
import com.example.companydemo.dto.AttendanceCheckOutRequest;
import com.example.companydemo.entity.AttendanceRecord;
import com.example.companydemo.mapper.AttendanceMapper;
import com.example.companydemo.support.AttendanceStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {

    private static final LocalTime LATE_TIME = LocalTime.of(9, 30);
    private static final LocalTime EARLY_LEAVE_TIME = LocalTime.of(18, 0);

    private final AttendanceMapper attendanceMapper;
    private final EmployeeService employeeService;

    public AttendanceService(AttendanceMapper attendanceMapper, EmployeeService employeeService) {
        this.attendanceMapper = attendanceMapper;
        this.employeeService = employeeService;
    }

    @Transactional
    public AttendanceRecord checkIn(AttendanceCheckInRequest request) {
        employeeService.getById(request.getEmployeeId());

        LocalDate today = LocalDate.now();
        AttendanceRecord exists = attendanceMapper.findByEmployeeAndDate(request.getEmployeeId(), today);
        if (exists != null) {
            throw new BusinessException("今天已经上班打卡");
        }

        LocalDateTime now = LocalDateTime.now();
        AttendanceRecord record = new AttendanceRecord();
        record.setEmployeeId(request.getEmployeeId());
        record.setWorkDate(today);
        record.setCheckInTime(now);
        record.setStatus(resolveCheckInStatus(now));
        record.setNote(request.getNote());

        attendanceMapper.insert(record);
        return record;
    }

    @Transactional
    public AttendanceRecord checkOut(AttendanceCheckOutRequest request) {
        employeeService.getById(request.getEmployeeId());

        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceMapper.findByEmployeeAndDate(request.getEmployeeId(), today);
        if (record == null) {
            throw new BusinessException("今天还没有上班打卡");
        }
        if (record.getCheckOutTime() != null) {
            throw new BusinessException("今天已经下班打卡");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setCheckOutTime(now);
        record.setStatus(resolveCheckOutStatus(record.getStatus(), now));
        record.setNote(mergeNote(record.getNote(), request.getNote()));

        attendanceMapper.updateCheckOut(record);
        return record;
    }

    public List<AttendanceRecord> list(Long employeeId, LocalDate startDate, LocalDate endDate) {
        employeeService.getById(employeeId);
        if (startDate == null || endDate == null) {
            throw new BusinessException("开始日期和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        return attendanceMapper.findByEmployeeAndDateRange(employeeId, startDate, endDate);
    }

    private int resolveCheckInStatus(LocalDateTime checkInTime) {
        if (checkInTime.toLocalTime().isAfter(LATE_TIME)) {
            return AttendanceStatus.LATE.getCode();
        }
        return AttendanceStatus.NORMAL.getCode();
    }

    private int resolveCheckOutStatus(Integer currentStatus, LocalDateTime checkOutTime) {
        if (checkOutTime.toLocalTime().isBefore(EARLY_LEAVE_TIME)) {
            return AttendanceStatus.EARLY_LEAVE.getCode();
        }
        return currentStatus;
    }

    private String mergeNote(String oldNote, String newNote) {
        if (newNote == null || newNote.trim().isEmpty()) {
            return oldNote;
        }
        if (oldNote == null || oldNote.trim().isEmpty()) {
            return newNote;
        }
        return oldNote + "；" + newNote;
    }
}

