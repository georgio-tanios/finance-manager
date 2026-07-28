package com.myfinance.finance_manager.service;

import com.myfinance.finance_manager.dto.ExpenseDTO;
import com.myfinance.finance_manager.dto.ExpenseStatisticsDTO;
import com.myfinance.finance_manager.exception.ResourceNotFoundException;
import com.myfinance.finance_manager.mapper.ExpenseMapper;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseMapper expenseMapper;

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

    @Test
    void getMonthlyStatistics_shouldThrowException_whenMonthIsInvalid() {
        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> expenseService.getMonthlyStatistics(2026, 13)
        );

        // Assert
        assertEquals(
                "Month should be between 1 and 12",
                exception.getMessage()
        );

        verifyNoInteractions(expenseRepository, expenseMapper);
    }

    @Test
    void getMonthlyStatistics_shouldThrowException_whenYearIsInvalid(){
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> expenseService.getMonthlyStatistics(-12, 1)
        );

        assertEquals("Year should be positive",
                exception.getMessage());
        verifyNoInteractions(expenseRepository,  expenseMapper);
    }

    @Test
    void getMonthlyStatistics_shouldReturnZeroStatistics_whenNoExpenseExists(){
        //Arrange
        LocalDate startDate = LocalDate.of(2026, 7, 1);
        LocalDate endDate = LocalDate.of(2026, 7, 31);

        when(expenseRepository.findByExpenseDateBetween(startDate, endDate)).thenReturn(List.of());

        //Act
        ExpenseStatisticsDTO result = expenseService.getMonthlyStatistics(2026, 7);

        //Assert
        assertAll(
                () -> assertEquals(2026, result.getYear()),
                () -> assertEquals(7, result.getMonth()),
                () -> assertEquals(0L, result.getExpenseCount()),
                () -> assertEquals(
                        new BigDecimal("0.00"),
                        result.getTotalAmount()
                ),
                () -> assertEquals(
                        new BigDecimal("0.00"),
                        result.getAverageAmount()
                ),
                () -> assertNull(result.getHighestExpense())
        );

        verify(expenseRepository, times(1))
                .findByExpenseDateBetween(startDate, endDate);

        verifyNoInteractions(expenseMapper);
    }

    @Test
    void getMonthlyStatistics_shouldCalculateStatistics_whenExpensesExist() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 7, 1);
        LocalDate endDate = LocalDate.of(2026, 7, 31);

        Expense foodExpense = new Expense(
                "Alimentation",
                new BigDecimal("100.00"),
                LocalDate.of(2026, 7, 12)
        );

        Expense rentExpense = new Expense(
                "Loyer",
                new BigDecimal("150.00"),
                LocalDate.of(2026, 7, 15)
        );

        Expense transportExpense = new Expense(
                "Transport",
                new BigDecimal("75.50"),
                LocalDate.of(2026, 7, 22)
        );

        Expense internetExpense = new Expense(
                "Internet",
                new BigDecimal("25.00"),
                LocalDate.of(2026, 7, 31)
        );

        List<Expense> expenses = List.of(
                foodExpense,
                rentExpense,
                transportExpense,
                internetExpense
        );

        ExpenseDTO rentExpenseDto = new ExpenseDTO();
        rentExpenseDto.setName("Loyer");
        rentExpenseDto.setAmount(new BigDecimal("150.00"));
        rentExpenseDto.setExpenseDate(LocalDate.of(2026, 7, 15));

        when(expenseRepository.findByExpenseDateBetween(startDate, endDate))
                .thenReturn(expenses);

        when(expenseMapper.toDto(rentExpense))
                .thenReturn(rentExpenseDto);

        // Act
        ExpenseStatisticsDTO result =
                expenseService.getMonthlyStatistics(2026, 7);

        // Assert
        assertAll(
                () -> assertEquals(2026, result.getYear()),
                () -> assertEquals(7, result.getMonth()),
                () -> assertEquals(4L, result.getExpenseCount()),
                () -> assertEquals(
                        new BigDecimal("350.50"),
                        result.getTotalAmount()
                ),
                () -> assertEquals(
                        new BigDecimal("87.63"),
                        result.getAverageAmount()
                ),
                () -> assertSame(
                        rentExpenseDto,
                        result.getHighestExpense()
                )
        );

        verify(expenseRepository)
                .findByExpenseDateBetween(startDate, endDate);

        verify(expenseMapper).toDto(rentExpense);

        verifyNoMoreInteractions(expenseMapper);
    }
}
