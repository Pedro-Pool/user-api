package com.user.tenant.repository;

import com.user.tenant.entity.OrganizationMembership;
import com.user.tenant.enums.MembershipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationMembershipRepository extends JpaRepository<OrganizationMembership, UUID> {

    Page<OrganizationMembership> findAllByUser_Id(UUID userId, Pageable pageable);

    Page<OrganizationMembership> findAllByOrganization_Id(UUID organizationId, Pageable pageable);

    Page<OrganizationMembership> findAllByOrganization_IdAndStatus(
            UUID organizationId,
            MembershipStatus status,
            Pageable pageable);

    Optional<OrganizationMembership> findByOrganization_IdAndUser_Id(UUID organizationId, UUID userId);

    boolean existsByOrganization_IdAndUser_Id(UUID organizationId, UUID userId);


}
