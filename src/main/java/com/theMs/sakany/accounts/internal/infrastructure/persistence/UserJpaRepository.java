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
					OR (:status = 'ACTIVE' AND u.isActive = TRUE)
					OR (:status = 'INACTIVE' AND u.isActive = FALSE)
			  )
			  AND (
					:searchTerm IS NULL
					OR LOWER(CONCAT(COALESCE(u.firstName, ''), ' ', COALESCE(u.lastName, ''))) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
					OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
					OR LOWER(COALESCE(u.phone, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
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
