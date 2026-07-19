package com.myfinance.finance_manager.service;

import com.myfinance.finance_manager.exception.ResourceNotFoundException;
import com.myfinance.finance_manager.model.Expense;
import com.myfinance.finance_manager.repository.ExpenseRepository;
import com.myfinance.finance_manager.service.impl.ExpenseServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Test
    void updateExpense_whenNotFound_throwsException() {
        Expense updatedExpense = new Expense(
                "Food",
                new BigDecimal("20.00"),
                LocalDate.now()
        );


        when(expenseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> expenseService.updateExpense(1L, updatedExpense)
        );

    }

    @Test
    void saveExpense_success() {
        Expense expense = new Expense("Food", new BigDecimal("20.00"), LocalDate.now());
        when(expenseRepository.save(expense)).thenReturn(expense);

        Expense saved = expenseService.saveExpense(expense);
        assertEquals("Food", saved.getName());
        verify(expenseRepository, times(1)).save(expense);
    }

    @Test
    void deleteExpense_whenNotFound_throwsException() {
        when(expenseRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> expenseService.deleteExpense(99L));
    }
}
