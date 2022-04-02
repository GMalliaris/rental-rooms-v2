package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.entity.ConfirmationToken;
import org.gmalliaris.rental.rooms.entity.ConfirmationTokenStatus;
import org.gmalliaris.rental.rooms.repository.ConfirmationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ConfirmationTokenService {

    @Value("${uuid.confirmation.expiration.days:7}")
    private int durationInDays;

    private final ConfirmationTokenRepository repository;

    public ConfirmationTokenService(ConfirmationTokenRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ConfirmationToken createTokenForUser(AccountUser user){

        var confirmationToken = new ConfirmationToken();
        confirmationToken.setStatus(ConfirmationTokenStatus.PENDING);
        confirmationToken.setExpirationDate(LocalDate.now().plusDays(durationInDays));
        confirmationToken.setAccountUser(user);
        return repository.save(confirmationToken);
    }

}
