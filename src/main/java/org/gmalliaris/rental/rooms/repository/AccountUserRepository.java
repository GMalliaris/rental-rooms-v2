package org.gmalliaris.rental.rooms.repository;

import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AccountUserRepository extends JpaRepository<AccountUser, UUID>
{
    Optional<AccountUser> findByEmail(String email);

    long countByEmail(String email);

    long countByPhoneNumber(String phoneNumber);

    @Query("SELECT count(u) FROM AccountUser u " +
            "JOIN u.roles r " +
            "WHERE r.name = 'ROLE_ADMIN'")
    long countAdminAccountUsers();
}
