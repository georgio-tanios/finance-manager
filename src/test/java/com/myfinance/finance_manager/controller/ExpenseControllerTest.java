package com.myfinance.finance_manager.controller;

import com.myfinance.finance_manager.dto.ExpenseStatisticsDTO;
import com.myfinance.finance_manager.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    /*
     * ExpenseController dépend également de ModelMapper.
     * Spring a donc besoin de ce mock pour construire le contrôleur.
     */
    @MockitoBean
    private ModelMapper modelMapper;

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
}