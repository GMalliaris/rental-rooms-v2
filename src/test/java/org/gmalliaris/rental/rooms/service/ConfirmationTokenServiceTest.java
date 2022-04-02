package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.entity.ConfirmationToken;
import org.gmalliaris.rental.rooms.entity.ConfirmationTokenStatus;
import org.gmalliaris.rental.rooms.repository.ConfirmationTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmationTokenServiceTest {

    @InjectMocks
    private ConfirmationTokenService tokenService;

    @Mock
    private ConfirmationTokenRepository tokenRepository;

    @Test
    void createTokenForUserTest(){
        when(tokenRepository.save(any(ConfirmationToken.class)))
                .then(i -> i.getArgument(0));

        var user = new AccountUser();
        user.setId(UUID.randomUUID());

        var now = LocalDate.now();
        try(var dateUtils = mockStatic(LocalDate.class)){
            dateUtils.when(LocalDate::now)
                    .thenReturn(now);
            var tokenDuration = 14;
            ReflectionTestUtils.setField(tokenService,
                    "durationInDays", tokenDuration);

            var result = tokenService.createTokenForUser(user);
            assertNotNull(result);
            assertEquals(ConfirmationTokenStatus.PENDING, result.getStatus());
            assertEquals(now.plusDays(tokenDuration), result.getExpirationDate());
            assertEquals(user, result.getAccountUser());
        }

    }
}