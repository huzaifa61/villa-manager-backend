package com.villamanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.villamanager.entity.Villa;
import java.util.List;

@Repository
public interface VillaRepository extends JpaRepository<Villa, Long> {
    List<Villa> findByIsActiveTrue();
}
