package com.villamanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.villamanager.entity.PaymentCategory;

@Repository
public interface PaymentCategoryRepository extends JpaRepository<PaymentCategory, Long> {
}
