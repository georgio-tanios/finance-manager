package com.myfinance.finance_manager.service;

import java.time.LocalDate;
import java.util.*;
import com.myfinance.finance_manager.model.Expense;
import com.myfinance.finance_manager.repository.ExpenseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j // <-- Lombok creates a logger named “log”
public class ExpenseService {
// the code below is used when we saved data in application memory
// this data will be removed after stopping the application

//        private final List<Expense> expenses = new ArrayList<>();
//
//        public Expense saveExpense(Expense expense){
//            expense.setId((long) (expenses.size()+1));
//            expenses.add(expense);
//            return expense;
//        }
//
//        public List<Expense> getAllExpenses(){
//            return expenses;
//        }

    //now will we use a database for persist data,
    //with that data will be saved in this memory
    // and we can return to this data even if the application is stoped
    // (we don't lose data when we stoop the app because are saved in database)

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public Expense saveExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public Optional<Expense> getExpensesById(Long id){
        return expenseRepository.findById(id);
    }

    public Expense updateExpense(Long id, Expense updatedExpense) {
        return expenseRepository.findById(id)
                .map(expense -> {
                    expense.setDate(updatedExpense.getDate());
                    expense.setAmount(updatedExpense.getAmount());
                    expense.setName(updatedExpense.getName());
                    return expenseRepository.save(expense);
                })
                .orElseThrow(() -> new NoSuchElementException("Expense not found with id : " +id));
    }

    public void deleteExpense(Long id){
        if (!expenseRepository.existsById(id)){
            throw new NoSuchElementException("Expense not found with id : " +id);
        }
        expenseRepository.deleteById(id);
    }
}
