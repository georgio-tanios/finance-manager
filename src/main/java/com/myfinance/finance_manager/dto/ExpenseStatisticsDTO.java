package com.myfinance.finance_manager.dto;

import java.math.BigDecimal;

public class ExpenseStatisticsDTO {

    private final int year;
    private final int month;
    private final long expenseCount;
    private final BigDecimal totalAmount;
    private final BigDecimal averageAmount;
    private final ExpenseDTO highestExpense;

    public ExpenseStatisticsDTO(
            int year,
            int month,
            long expenseCount,
            BigDecimal totalAmount,
            BigDecimal averageAmount,
            ExpenseDTO highestExpense
    ) {
        this.year = year;
        this.month = month;
        this.expenseCount = expenseCount;
        this.totalAmount = totalAmount;
        this.averageAmount = averageAmount;
        this.highestExpense = highestExpense;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public long getExpenseCount() {
        return expenseCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getAverageAmount() {
        return averageAmount;
    }

    public ExpenseDTO getHighestExpense() {
        return highestExpense;
    }
}