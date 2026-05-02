package com.theMs.sakany.accounts.internal.infrastructure.persistence;

import com.theMs.sakany.accounts.internal.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
	Optional<UserEntity> findByPhone(String phone);
	Optional<UserEntity> findByEmail(String email);

	@Query("""
			SELECT u
			FROM UserEntity u
			WHERE u.role IN :roles
			  AND (
					:status IS NULL
					OR :status = 'ALL'
					OR (
						:status = 'ACTIVE'
						AND (
							u.employmentStatus = com.theMs.sakany.accounts.internal.domain.EmployeeAccountStatus.ACTIVE
							OR (u.employmentStatus IS NULL AND u.isActive = TRUE)
						)
					)
					OR (
						:status = 'INACTIVE'
						AND (
							u.employmentStatus = com.theMs.sakany.accounts.internal.domain.EmployeeAccountStatus.INACTIVE
							OR (u.employmentStatus IS NULL AND u.isActive = FALSE)
						)
					)
					OR (
						:status = 'SUSPENDED'
						AND u.employmentStatus = com.theMs.sakany.accounts.internal.domain.EmployeeAccountStatus.SUSPENDED
					)
			  )
			  AND (
					:searchTerm IS NULL
					OR LOWER(CONCAT(COALESCE(u.firstName, ''), ' ', COALESCE(u.lastName, ''))) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%'))
					OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%'))
					OR LOWER(COALESCE(u.phone, '')) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%'))
			  )
			ORDER BY u.createdAt DESC
			""")
	List<UserEntity> findStaffForAdmin(
			@Param("searchTerm") String searchTerm,
			@Param("status") String status,
			@Param("roles") List<Role> roles
	);

	List<UserEntity> findAllByRoleIn(List<Role> roles);
}
