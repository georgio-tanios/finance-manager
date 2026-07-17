package com.myfinance.finance_manager.service.impl;

import com.myfinance.finance_manager.exception.ResourceNotFoundException;
import com.myfinance.finance_manager.model.Expense;
import com.myfinance.finance_manager.repository.ExpenseRepository;
import com.myfinance.finance_manager.service.ExpenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    public ExpenseServiceImpl(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
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
        existing.setDate(updatedExpense.getDate());

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
    public List<Expense> findByAmountBetween(Double min, Double max) {
        log.debug("Finding expenses with amount between {} and {}", min, max);
        if (min == null || max == null) {
            throw new IllegalArgumentException("min and max must be provided");
        }
        return expenseRepository.findByAmountBetween(min, max);
    }

    @Override
    public List<Expense> findByDateBetween(LocalDate start, LocalDate end) {
        log.debug("Finding expenses by date between {} and {}", start, end);
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end dates must be provided");
        }
        return expenseRepository.findByDateBetween(start, end);
    }

    @Override
    public List<Expense> filterCombined(String name, Double minAmount, Double maxAmount, LocalDate startDate, LocalDate endDate) {
        log.debug("Filtering combined: name={}, min={}, max={}, start={}, end={}", name, minAmount, maxAmount, startDate, endDate);
        return expenseRepository.filterExpenses(
                (name == null || name.isBlank()) ? null : name,
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
}
