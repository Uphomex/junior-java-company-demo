package com.example.companydemo.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SalaryCalculator {

    private static final BigDecimal WORK_DAYS_PER_MONTH = new BigDecimal("21.75");
    private static final BigDecimal LATE_DEDUCTION = new BigDecimal("50");
    private static final BigDecimal EARLY_LEAVE_DEDUCTION = new BigDecimal("50");

    private SalaryCalculator() {
    }

    public static BigDecimal calculateActualSalary(BigDecimal baseSalary, int lateCount, int earlyLeaveCount, int absentDays) {
        BigDecimal dailySalary = baseSalary.divide(WORK_DAYS_PER_MONTH, 2, RoundingMode.HALF_UP);
        BigDecimal absentDeduction = dailySalary.multiply(BigDecimal.valueOf(absentDays));
        BigDecimal lateDeduction = LATE_DEDUCTION.multiply(BigDecimal.valueOf(lateCount));
        BigDecimal earlyLeaveDeduction = EARLY_LEAVE_DEDUCTION.multiply(BigDecimal.valueOf(earlyLeaveCount));
        BigDecimal actualSalary = baseSalary.subtract(absentDeduction).subtract(lateDeduction).subtract(earlyLeaveDeduction);
        return actualSalary.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}

