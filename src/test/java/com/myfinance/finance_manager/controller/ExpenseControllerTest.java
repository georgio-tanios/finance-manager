package com.myfinance.finance_manager.controller;

import com.myfinance.finance_manager.dto.ExpenseStatisticsDTO;
import com.myfinance.finance_manager.service.ExpenseService;
import com.myfinance.finance_manager.mapper.ExpenseMapper;
import com.myfinance.finance_manager.dto.ExpenseDTO;
import com.myfinance.finance_manager.model.Expense;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    @MockitoBean
    private ExpenseMapper expenseMapper;

    @Test
    void getMonthlyStatistics_shouldReturnStatistics_whenRequestIsValid()
            throws Exception {

        // Arrange
        ExpenseStatisticsDTO statistics = new ExpenseStatisticsDTO(
                2026,
                7,
                4,
                new BigDecimal("350.50"),
                new BigDecimal("87.63"),
                null
        );

        when(expenseService.getMonthlyStatistics(2026, 7))
                .thenReturn(statistics);

        // Act + Assert
        mockMvc.perform(
                        get("/api/expenses/statistics")
                                .param("year", "2026")
                                .param("month", "7")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(7))
                .andExpect(jsonPath("$.expenseCount").value(4))
                .andExpect(jsonPath("$.totalAmount").value(350.50))
                .andExpect(jsonPath("$.averageAmount").value(87.63))
                .andExpect(jsonPath("$.highestExpense").isEmpty());

        verify(expenseService).getMonthlyStatistics(2026, 7);
    }

    @Test
    void getMonthlyStatistics_shouldReturnBadRequest_whenMonthIsInvalid()
            throws Exception {
        // Act + Assert
        mockMvc.perform(get("/api/expenses/statistics")
                        .param("year", "2026")
                        .param("month", "13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Month should be between 1 and 12"));
        verifyNoInteractions(expenseService);
    }

    @Test
    void getMonthlyStatistics_shouldReturnBadRequest_whenMonthIsMissing()
            throws Exception {
        // Act + Assert
        mockMvc.perform(get("/api/expenses/statistics")
                        .param("year", "2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Required parameter 'month' is missing"));
        verifyNoInteractions(expenseService);
    }

    @Test
    void getTopExpenses_shouldReturnTopExpenses_whenRequestIsValid()
            throws Exception {

        // Arrange
        Expense transportation = new Expense(
                "Transportation",
                new BigDecimal("150.00"),
                LocalDate.of(2026, 7, 12)
        );

        Expense food = new Expense(
                "Food expense",
                new BigDecimal("100.00"),
                LocalDate.of(2026, 7, 12)
        );

        ExpenseDTO transportationDTO = new ExpenseDTO();
        transportationDTO.setName("Transportation");
        transportationDTO.setAmount(new BigDecimal("150.00"));
        transportationDTO.setExpenseDate(LocalDate.of(2026, 7, 12));

        ExpenseDTO foodDTO = new ExpenseDTO();
        foodDTO.setName("Food expense");
        foodDTO.setAmount(new BigDecimal("100.00"));
        foodDTO.setExpenseDate(LocalDate.of(2026, 7, 12));

        when(expenseService.getTopExpenses(
                new BigDecimal("75.50"),
                2
        )).thenReturn(List.of(transportation, food));

        when(expenseMapper.toDto(transportation))
                .thenReturn(transportationDTO);

        when(expenseMapper.toDto(food))
                .thenReturn(foodDTO);

        // Act + Assert
        mockMvc.perform(
                        get("/api/expenses/top")
                                .param("minAmount", "75.50")
                                .param("limit", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name")
                        .value("Transportation"))
                .andExpect(jsonPath("$[0].amount")
                        .value(150.00))
                .andExpect(jsonPath("$[1].name")
                        .value("Food expense"))
                .andExpect(jsonPath("$[1].amount")
                        .value(100.00));

        verify(expenseService).getTopExpenses(
                new BigDecimal("75.50"),
                2
        );

        verify(expenseMapper).toDto(transportation);
        verify(expenseMapper).toDto(food);
    }

    @Test
    void getAllExpenses_shouldReturnPaginatedExpenses() throws Exception{
        //Arrange
        Expense expense = new Expense(
                "Food",
                new BigDecimal("20.00"),
                LocalDate.of(2026, 9, 8)
        );

        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setName("Food");
        expenseDTO.setAmount(new BigDecimal("20.00"));
        expenseDTO.setExpenseDate(LocalDate.of(2026, 9, 8));

        Pageable pageable = PageRequest.of(
                0,
                2,
                Sort.by("expenseDate").descending()
        );

        Page<Expense> expensePage = new PageImpl<>(
                List.of(expense),
                pageable,
                5
        );

        when(expenseService.getAllExpenses(pageable)).thenReturn(expensePage);
        when(expenseMapper.toDto(expense)).thenReturn(expenseDTO);

        //Act + Assert
        mockMvc.perform(
                        get("/api/expenses")
                                .param("page", "0")
                                .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Food"))
                .andExpect(jsonPath("$.content[0].amount").value(20.00))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));

        verify(expenseService).getAllExpenses(pageable);
        verify(expenseMapper).toDto(expense);
    }

    @Test
    void getAllExpenses_shouldReturnBadRequest_whenPageIsNegative()
            throws Exception {

        mockMvc.perform(
                        get("/api/expenses")
                                .param("page", "-1")
                                .param("size", "10")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(expenseService);
    }

    @Test
    void getAllExpenses_shouldReturnBadRequest_whenSortFieldIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/expenses")
                                .param("sortBy", "invalidField")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(expenseService);
    }

    @Test
    void getAllExpenses_shouldReturnBadRequest_whenDirectionIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/expenses")
                                .param("direction", "random")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(expenseService);
    }
}