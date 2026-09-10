package com.myfinance.finance_manager.service;

import com.myfinance.finance_manager.dto.ExpenseStatisticsDTO;
import com.myfinance.finance_manager.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Expense domain operations.
 * Keep business logic in implementation; controller should remain thin.
 */
public interface ExpenseService {

    Expense saveExpense(Expense expense);

    List<Expense> getAllExpenses();

    Optional<Expense> getExpensesById(Long id);

    /**
     * Update an existing expense. Throws ResourceNotFoundException if not found.
     */
    Expense updateExpense(Long id, Expense updatedExpense);

    /**
     * Delete an expense by id. Throws ResourceNotFoundException if not found.
     */
    void deleteExpense(Long id);

    // Search / filter helpers
    List<Expense> searchByName(String keyword);

    List<Expense> findByAmountBetween(BigDecimal min, BigDecimal max);

    List<Expense> findByDateBetween(LocalDate start, LocalDate end);

    /**
     * Flexible combined filter.
     */
    List<Expense> filterCombined(String name, BigDecimal minAmount, BigDecimal maxAmount,
                                 LocalDate startDate, LocalDate endDate);

    /**
     * Pagination + sorting support.
     *
     * @param page zero-based page index
     * @param size page size
     * @param sortBy property to sort by
     * @param direction "asc" or "desc"
     */
    Page<Expense> getExpensesPaginatedAndSorted(int page, int size, String sortBy, String direction);

    ExpenseStatisticsDTO getMonthlyStatistics(int year, int month);
    List<Expense> getTopExpenses(BigDecimal minAmount, int limit);
    Page<Expense> getAllExpenses(Pageable pageable);
}
