package org.gmalliaris.rental.rooms.repository;

import org.gmalliaris.rental.rooms.entity.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, UUID> {
}
