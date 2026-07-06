package com.villamanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.villamanager.entity.User;
import com.villamanager.entity.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByInviteToken(String inviteToken);
    Boolean existsByEmail(String email);
    List<User> findByVillaId(Long villaId);
    List<User> findByVillaIdAndRole(Long villaId, UserRole role);
}
