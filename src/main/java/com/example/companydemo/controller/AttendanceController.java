package com.example.companydemo.controller;

import com.example.companydemo.common.ApiResponse;
import com.example.companydemo.dto.AttendanceCheckInRequest;
import com.example.companydemo.dto.AttendanceCheckOutRequest;
import com.example.companydemo.entity.AttendanceRecord;
import com.example.companydemo.service.AttendanceService;
import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    public ApiResponse<AttendanceRecord> checkIn(@Valid @RequestBody AttendanceCheckInRequest request) {
        return ApiResponse.success(attendanceService.checkIn(request));
    }

    @PostMapping("/check-out")
    public ApiResponse<AttendanceRecord> checkOut(@Valid @RequestBody AttendanceCheckOutRequest request) {
        return ApiResponse.success(attendanceService.checkOut(request));
    }

    @GetMapping
    public ApiResponse<List<AttendanceRecord>> list(@RequestParam Long employeeId,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(attendanceService.list(employeeId, startDate, endDate));
    }
}

