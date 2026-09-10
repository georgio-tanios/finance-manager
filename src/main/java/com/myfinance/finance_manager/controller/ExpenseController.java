package com.myfinance.finance_manager.controller;

import com.myfinance.finance_manager.dto.ExpenseDTO;
import com.myfinance.finance_manager.dto.ExpenseStatisticsDTO;
import com.myfinance.finance_manager.dto.PageResponseDTO;
import com.myfinance.finance_manager.model.Expense;
import com.myfinance.finance_manager.service.ExpenseService;
import com.myfinance.finance_manager.mapper.ExpenseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Endpoints for managing expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseMapper expenseMapper;

    // GET all expenses
    @GetMapping
    public ResponseEntity<PageResponseDTO<ExpenseDTO>> getAllExpenses(
            @RequestParam(defaultValue = "0")
            @Min(value= 0, message = "Page must be positive or zero")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Size must be between 1 and 100")
            @Max(value = 100, message = "Size must be between 1 and 100")
            int size,

            @RequestParam(defaultValue = "expenseDate")
            @Pattern(
                    regexp = "name|amount|expenseDate",
                    message = "Sort field must be name, amount or expenseDate"
            )
            String sortBy,

            @RequestParam(defaultValue = "desc")
            @Pattern(
                    regexp = "(?i)asc|desc",
                    message = "Direction must be asc or desc"
            )
            String direction
    ){
        Sort.Direction sortDirection =
                Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Page<ExpenseDTO> expensePage = expenseService.getAllExpenses(pageable).map(expenseMapper::toDto);

        PageResponseDTO<ExpenseDTO> response =
                new PageResponseDTO<>(
                        expensePage.getContent(),
                        expensePage.getNumber(),
                        expensePage.getSize(),
                        expensePage.getTotalElements(),
                        expensePage.getTotalPages(),
                        expensePage.isFirst(),
                        expensePage.isLast()
                );

        return ResponseEntity.ok(response);
    }

    // CREATE a new expense
    @Operation(summary = "Create new expense", description = "Add a new expense record to the database")
    @ApiResponse(responseCode = "201", description = "Expense created successfully")
    @PostMapping
    public ResponseEntity<ExpenseDTO> createExpense(@Valid @RequestBody ExpenseDTO expenseDTO) {
        Expense expense = expenseMapper.toEntity(expenseDTO);
        Expense savedExpense = expenseService.saveExpense(expense);
        ExpenseDTO responseDTO = expenseMapper.toDto(savedExpense);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    // GET expense by ID
    @Operation(summary = "Get expense by ID", description = "Retrieve a specific expense by its ID")
    @ApiResponse(responseCode = "200", description = "Expense retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Expense not found")
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable Long id) {
        return expenseService.getExpensesById(id)
                .map(expense ->
                        ResponseEntity.ok(expenseMapper.toDto(expense))
                )
                .orElse(ResponseEntity.notFound().build());
    }

    // SEARCH expenses by keyword (name contains)
    @Operation(summary = "Search expenses by keyword", description = "Search expenses whose name contains the given keyword")
    @ApiResponse(responseCode = "200", description = "Expenses found successfully")
    @GetMapping("/search")
    public ResponseEntity<List<ExpenseDTO>> searchExpenses(@RequestParam String keyword) {
        List<ExpenseDTO> result = expenseService.searchByName(keyword)
                .stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // FILTER expenses by amount range
    @Operation(summary = "Filter expenses by amount range", description = "Return expenses between the given min and max amount")
    @ApiResponse(responseCode = "200", description = "Expenses filtered successfully")
    @GetMapping("/filter")
    public ResponseEntity<List<ExpenseDTO>> filterByAmountRange(@RequestParam BigDecimal min, @RequestParam BigDecimal max) {
        List<ExpenseDTO> result = expenseService.findByAmountBetween(min, max)
                .stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // UPDATE an existing expense
    @Operation(summary = "Update expense", description = "Update an existing expense by ID")
    @ApiResponse(responseCode = "200", description = "Expense updated successfully")
    @ApiResponse(responseCode = "404", description = "Expense not found")
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable Long id,
                                                    @Valid @RequestBody ExpenseDTO expenseDTO) {
        try {
            Expense updatedExpense = expenseService.updateExpense(id, expenseMapper.toEntity(expenseDTO));
            return ResponseEntity.ok(expenseMapper.toDto(updatedExpense));
        } catch (NoSuchElementException e) {
            log.error("Expense not found for update with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE expense by ID
    @Operation(summary = "Delete expense", description = "Delete an expense record by ID")
    @ApiResponse(responseCode = "204", description = "Expense deleted successfully")
    @ApiResponse(responseCode = "404", description = "Expense not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.error("Expense not found for deletion with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<ExpenseStatisticsDTO> getMonthlyStatistics(
            @RequestParam
            @Min(value = 1, message = "Year should be positive")
            int year,

            @RequestParam
            @Min(value = 1, message = "Month should be between 1 and 12")
            @Max(value = 12, message = "Month should be between 1 and 12")
            int month
    ) {
        ExpenseStatisticsDTO statistics = expenseService.getMonthlyStatistics(year, month);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/top")
    public ResponseEntity<List<ExpenseDTO>> getTopExpenses(
            @RequestParam
            @DecimalMin(
                    value = "0.00",
                    inclusive = true,
                    message = "Minimum amount must be positive or zero")
            BigDecimal minAmount,

            @RequestParam
            @Min(value = 1, message = "Limit must be between 1 and 100")
            @Max(value = 100, message = "Limit must be between 1 and 100")
            int limit
    ) {
        List<ExpenseDTO> result = expenseService
                .getTopExpenses(minAmount, limit)
                .stream()
                .map(expenseMapper::toDto)
                .toList();

        return ResponseEntity.ok(result);
    }
}
