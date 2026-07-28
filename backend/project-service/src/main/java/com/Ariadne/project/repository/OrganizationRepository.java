package com.Ariadne.project.repository;

import com.Ariadne.project.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
