package com.myfinance.finance_manager.controller;

import com.myfinance.finance_manager.dto.ExpenseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.myfinance.finance_manager.model.Expense;
import com.myfinance.finance_manager.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Test
    void createExpense_success() throws Exception {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setName("Test Expense");
        dto.setAmount(new BigDecimal("100.00"));
        dto.setExpenseDate(LocalDate.now());

        mockMvc.perform(
                        post("/api/expenses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Expense"))
                .andExpect(jsonPath("$.amount").value(100.0));
    }

    @Test
    @Transactional
    void getAllExpenses_shouldPaginateAndSortUsingDatabase()
            throws Exception {

        // Arrange
        expenseRepository.deleteAll();

        Expense expense1 = new Expense(
                "Food",
                new BigDecimal("30.00"),
                LocalDate.of(2026, 9, 8)
        );

        Expense expense2 = new Expense(
                "Transport",
                new BigDecimal("10.00"),
                LocalDate.of(2026, 9, 9)
        );

        Expense expense3 = new Expense(
                "Internet",
                new BigDecimal("20.00"),
                LocalDate.of(2026, 9, 10)
        );

        expenseRepository.saveAll(
                List.of(expense1, expense2, expense3)
        );

        // Act + Assert
        mockMvc.perform(
                        get("/api/expenses")
                                .param("page", "0")
                                .param("size", "2")
                                .param("sortBy", "amount")
                                .param("direction", "asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Transport"))
                .andExpect(jsonPath("$.content[0].amount").value(10.00))
                .andExpect(jsonPath("$.content[1].name").value("Internet"))
                .andExpect(jsonPath("$.content[1].amount").value(20.00))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }
}
