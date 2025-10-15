package com.myfinance.finance_manager.controller;

import com.myfinance.finance_manager.dto.ExpenseDTO;
import com.myfinance.finance_manager.model.Expense;
import com.myfinance.finance_manager.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Operation(
            summary = "Get all expenses",
            description = "Returns a list of all saved expenses"
    )
    @ApiResponse(responseCode = "200", description = "List fetched successfully")
    // 🔹 Get all expenses
    @GetMapping
    public List<ExpenseDTO> getAllExpenses() {
        return expenseService.getAllExpenses()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 🔹 Create a new expense
    @PostMapping
    public ResponseEntity<ExpenseDTO> createExpense(@Valid @RequestBody ExpenseDTO expenseDTO) {
        Expense expense = convertToEntity(expenseDTO);
        Expense savedExpense = expenseService.saveExpense(expense);
        return ResponseEntity.ok(convertToDTO(savedExpense));
    }

    // 🔹 Get expense by ID
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable Long id) {
        return expenseService.getExpensesById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔹 Update an expense
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable Long id,
                                                    @Valid @RequestBody ExpenseDTO expenseDTO) {
        try {
            Expense updatedExpense = expenseService.updateExpense(id, convertToEntity(expenseDTO));
            return ResponseEntity.ok(convertToDTO(updatedExpense));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 🔹 Delete an expense
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 🧩 Utility methods for conversion
    private ExpenseDTO convertToDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setName(expense.getName());
        dto.setAmount(expense.getAmount());
        dto.setDate(expense.getDate());
        return dto;
    }

    private Expense convertToEntity(ExpenseDTO dto) {
        Expense expense = new Expense();
        expense.setId(dto.getId());
        expense.setName(dto.getName());
        expense.setAmount(dto.getAmount());
        expense.setDate(dto.getDate());
        return expense;
    }
}
