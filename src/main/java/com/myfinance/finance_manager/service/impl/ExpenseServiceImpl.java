package com.myfinance.finance_manager.service.impl;

import com.myfinance.finance_manager.dto.ExpenseStatisticsDTO;
import com.myfinance.finance_manager.exception.ResourceNotFoundException;
import com.myfinance.finance_manager.mapper.ExpenseMapper;
import com.myfinance.finance_manager.model.Expense;
import com.myfinance.finance_manager.repository.ExpenseRepository;
import com.myfinance.finance_manager.service.ExpenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Enterprise-style Expense service implementation.
 * - Uses constructor injection
 * - Declares transactional boundaries
 * - Keeps business logic here (not in controller)
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, ExpenseMapper expenseMapper) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
    }

    @Override
    @Transactional // write operation
    public Expense saveExpense(Expense expense) {
        log.info("Saving expense: {}", expense);
        return expenseRepository.save(expense);
    }

    @Override
    public List<Expense> getAllExpenses() {
        log.debug("Fetching all expenses");
        return expenseRepository.findAll();
    }

    @Override
    public Optional<Expense> getExpensesById(Long id) {
        log.debug("Fetching expense by id: {}", id);
        return expenseRepository.findById(id);
    }

    @Override
    @Transactional
    public Expense updateExpense(Long id, Expense updatedExpense) {
        log.info("Updating expense id={} with values {}", id, updatedExpense);
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));

        // Update fields explicitly (avoid replacing the managed entity instance)
        existing.setName(updatedExpense.getName());
        existing.setAmount(updatedExpense.getAmount());
        existing.setExpenseDate(updatedExpense.getExpenseDate());

        // Save and return managed entity
        Expense saved = expenseRepository.save(existing);
        log.debug("Expense updated: {}", saved);
        return saved;
    }

    @Override
    @Transactional
    public void deleteExpense(Long id) {
        log.info("Deleting expense id={}", id);
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense", id);
        }
        expenseRepository.deleteById(id);
    }

    @Override
    public List<Expense> searchByName(String keyword) {
        log.debug("Searching expenses by name containing: {}", keyword);
        if (keyword == null || keyword.isBlank()) {
            return expenseRepository.findAll();
        }
        return expenseRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public List<Expense> findByAmountBetween(
            BigDecimal min,
            BigDecimal max
    ) {
        log.debug(
                "Finding expenses with amount between {} and {}",
                min,
                max
        );

        if (min == null || max == null) {
            throw new IllegalArgumentException(
                    "min and max must be provided"
            );
        }

        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException(
                    "min amount must be less than or equal to max amount"
            );
        }

        return expenseRepository.findByAmountBetween(min, max);
    }

    @Override
    public List<Expense> findByDateBetween(
            LocalDate start,
            LocalDate end
    ) {
        log.debug(
                "Finding expenses by date between {} and {}",
                start,
                end
        );

        if (start == null || end == null) {
            throw new IllegalArgumentException(
                    "start and end dates must be provided"
            );
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException(
                    "start date must be before or equal to end date"
            );
        }

        return expenseRepository.findByExpenseDateBetween(start, end);
    }

    @Override
    public List<Expense> filterCombined(
            String name,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.debug(
                "Filtering combined: name={}, min={}, max={}, start={}, end={}",
                name,
                minAmount,
                maxAmount,
                startDate,
                endDate
        );

        return expenseRepository.filterExpenses(
                name == null || name.isBlank() ? null : name,
                minAmount,
                maxAmount,
                startDate,
                endDate
        );
    }

    @Override
    public Page<Expense> getExpensesPaginatedAndSorted(int page, int size, String sortBy, String direction) {
        log.debug("Paginated request page={} size={} sortBy={} dir={}", page, size, sortBy, direction);
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sort);
        return expenseRepository.findAll(pageable);
    }

    @Override
    public ExpenseStatisticsDTO getMonthlyStatistics(int year, int month) {

        if (year <= 0) {
            throw new IllegalArgumentException(
                    "Year should be positive"
            );
        }

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Month should be between 1 and 12"
            );
        }

        YearMonth selectedMonth = YearMonth.of(year, month);

        LocalDate startDate = selectedMonth.atDay(1);
        LocalDate endDate = selectedMonth.atEndOfMonth();

        List<Expense> expensesOfMonth =
                expenseRepository.findByExpenseDateBetween(
                        startDate,
                        endDate
                );

        BigDecimal zero = BigDecimal.ZERO.setScale(2);

        if (expensesOfMonth.isEmpty()) {
            return new ExpenseStatisticsDTO(
                    year,
                    month,
                    0,
                    zero,
                    zero,
                    null
            );
        }

        BigDecimal total = expensesOfMonth
                .stream()
                .map(Expense::getAmount)
                .reduce(zero, BigDecimal::add);

        Expense highestExpense = expensesOfMonth
                .stream()
                .max(Comparator.comparing(Expense::getAmount))
                .orElseThrow();

        BigDecimal average = total.divide(
                BigDecimal.valueOf(expensesOfMonth.size()),
                2,
                RoundingMode.HALF_UP
        );

        return new ExpenseStatisticsDTO(
                year,
                month,
                expensesOfMonth.size(),
                total,
                average,
                expenseMapper.toDto(highestExpense)
        );
    }

    @Override
    public List<Expense> getTopExpenses(BigDecimal minAmount, int limit) {
        if(minAmount == null){
            throw new IllegalArgumentException("Minimum amount must be provided");
        }

        if (minAmount.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Minimum amount must be positive or zero");
        }

        if (limit<1 || limit>100){
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }

        return expenseRepository.findAll()
                .stream()
                .filter(expense -> expense.getAmount().compareTo(minAmount) >= 0)
                .sorted(Comparator.comparing(Expense::getAmount).reversed())
                .limit(limit)
                .toList();
    }

}
