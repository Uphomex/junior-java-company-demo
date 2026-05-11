package com.example.companydemo.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SalaryCalculatorTest {

    @Test
    void shouldCalculateActualSalary() {
        BigDecimal actualSalary = SalaryCalculator.calculateActualSalary(new BigDecimal("10000"), 2, 1, 1);

        assertEquals(new BigDecimal("9390.23"), actualSalary);
    }

    @Test
    void shouldNotReturnNegativeSalary() {
        BigDecimal actualSalary = SalaryCalculator.calculateActualSalary(new BigDecimal("1000"), 0, 0, 30);

        assertEquals(new BigDecimal("0.00"), actualSalary);
    }
}
