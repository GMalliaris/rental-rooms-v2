package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.config.exception.ApiException;
import org.gmalliaris.rental.rooms.config.exception.ApiExceptionMessageConstants;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.entity.ConfirmationToken;
import org.gmalliaris.rental.rooms.entity.ConfirmationTokenStatus;
import org.gmalliaris.rental.rooms.repository.ConfirmationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConfirmationTokenService {

    @Value("${uuid.confirmation.expiration.days:7}")
    private int durationInDays;

    private final ConfirmationTokenRepository repository;

    public ConfirmationTokenService(ConfirmationTokenRepository repository) {
        this.repository = repository;
    }

    private ConfirmationToken extractPendingFromOptional(Optional<ConfirmationToken> optionalToken,
                                                         String notFoundMessage){

        var token = optionalToken
                .orElseThrow(() -> {
                    throw new ApiException(HttpStatus.NOT_FOUND, notFoundMessage);
                });

        if (ConfirmationTokenStatus.EXPIRED == token.getStatus()){
            throw new ApiException(HttpStatus.CONFLICT, ApiExceptionMessageConstants.CONFIRMATION_TOKEN_EXPIRED);
        }
        if (ConfirmationTokenStatus.ACTIVATED == token.getStatus()){
            throw new ApiException(HttpStatus.CONFLICT, ApiExceptionMessageConstants.CONFIRMATION_TOKEN_ALREADY_USED);
        }
        return token;
    }

    @Transactional
    public ConfirmationToken useConfirmationToken(UUID tokenId){

        var optionalToken = repository.findById(tokenId);

        var errMsg = String.format(ApiExceptionMessageConstants.ENTITY_NOT_FOUND_TEMPLATE,
                "ConfirmationToken", tokenId);
        var token = extractPendingFromOptional(optionalToken, errMsg);
        token.setStatus(ConfirmationTokenStatus.ACTIVATED);
        return repository.save(token);
    }

    @Transactional
    public ConfirmationToken createTokenForUser(AccountUser user){

        var confirmationToken = new ConfirmationToken();
        confirmationToken.setStatus(ConfirmationTokenStatus.PENDING);
        confirmationToken.setExpirationDate(LocalDate.now().plusDays(durationInDays));
        confirmationToken.setAccountUser(user);
        return repository.save(confirmationToken);
    }

    @Transactional
    public ConfirmationToken replaceConfirmationTokenForUser(AccountUser user){

        var optionalToken = repository.findByAccountUserId(user.getId());
        var errMsg = String.format(ApiExceptionMessageConstants.ENTITY_OF_ENTITY_NOT_FOUND_TEMPLATE,
                "ConfirmationToken", "AccountUser", user.getId());

        var token = extractPendingFromOptional(optionalToken, errMsg);
        repository.delete(token);
        repository.flush();
        return createTokenForUser(user);
    }

}
