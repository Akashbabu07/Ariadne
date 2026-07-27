package com.Ariadne.auth.repository;

import com.Ariadne.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByOrgIdAndEmail(UUID orgId, String email);
    boolean existsByOrgIdAndEmail(UUID orgId, String email);
    Optional<User> findByEmailIgnoreCase(String email);

}