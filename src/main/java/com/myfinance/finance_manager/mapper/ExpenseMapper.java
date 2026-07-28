package com.myfinance.finance_manager.mapper;

import com.myfinance.finance_manager.dto.ExpenseDTO;
import com.myfinance.finance_manager.model.Expense;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Maps between Expense and ExpenseDTO.
 * Keep mapping rules centralized for maintainability.
 */
@Component
public class ExpenseMapper {

    private final ModelMapper modelMapper;

    public ExpenseMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ExpenseDTO toDto(Expense expense) {
        return modelMapper.map(expense, ExpenseDTO.class);
    }

    public Expense toEntity(ExpenseDTO dto) {
        return modelMapper.map(dto, Expense.class);
    }
}
