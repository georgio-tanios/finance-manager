package com.myfinance.finance_manager.controller;

import com.myfinance.finance_manager.dto.ExpenseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
}
