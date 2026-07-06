package com.villamanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.villamanager.entity.RecurringExpenseTemplate;
import java.util.List;

@Repository
public interface RecurringExpenseTemplateRepository extends JpaRepository<RecurringExpenseTemplate, Long> {
    List<RecurringExpenseTemplate> findByVillaId(Long villaId);
    List<RecurringExpenseTemplate> findByIsActiveTrue();
    void deleteByVillaId(Long villaId);
}
