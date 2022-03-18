package org.gmalliaris.rental.rooms.repository;

import org.gmalliaris.rental.rooms.entity.UserRole;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    Optional<UserRole> findByName(UserRoleName name);
}
