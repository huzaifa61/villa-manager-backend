package com.villamanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.villamanager.entity.Apartment;
import java.util.List;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
    List<Apartment> findByVillaId(Long villaId);
    void deleteByVillaId(Long villaId);
    Long countByVillaIdAndStatusOrderByCreatedAtDesc(Long villaId, String status);
}
