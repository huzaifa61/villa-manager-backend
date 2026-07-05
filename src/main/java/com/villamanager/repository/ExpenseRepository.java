package com.villamanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.villamanager.entity.Expense;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByVillaId(Long villaId);
    List<Expense> findByApartmentId(Long apartmentId);
}
