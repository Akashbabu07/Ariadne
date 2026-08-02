package com.Ariadne.project.repository;

import com.Ariadne.project.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    List<Member> findByOrganizationId(UUID orgId);
    Optional<Member> findByOrganizationIdAndUserId(UUID orgId, UUID userId);
}
