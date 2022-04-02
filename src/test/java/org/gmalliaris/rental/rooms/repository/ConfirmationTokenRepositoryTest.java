package org.gmalliaris.rental.rooms.repository;

import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.entity.ConfirmationToken;
import org.gmalliaris.rental.rooms.entity.ConfirmationTokenStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class ConfirmationTokenRepositoryTest {

    @Autowired
    private AccountUserRepository userRepository;

    @Autowired
    private ConfirmationTokenRepository tokenRepository;

    private AccountUser user;

    @BeforeEach
    public void setup(){
        user = new AccountUser();
        user.setEmail("test@example.eg");
        user.setPassword("12345678");
        user.setFirstName("firstName");

        userRepository.saveAndFlush(user);
        assertNotNull(user.getId());
    }

    @Test
    void saveConfirmationTokenTest_expirationDateNull(){
        var token = new ConfirmationToken();
        token.setStatus(ConfirmationTokenStatus.PENDING);
        token.setAccountUser(user);

        assertThrows(DataIntegrityViolationException.class,
                () -> tokenRepository.saveAndFlush(token));
    }

    @Test
    void saveConfirmationTokenTest_statusNull(){
        var token = new ConfirmationToken();
        token.setExpirationDate(LocalDate.now());
        token.setAccountUser(user);

        assertThrows(DataIntegrityViolationException.class,
                () -> tokenRepository.saveAndFlush(token));
    }

    @Test
    void saveConfirmationTokenTest_userNull(){
        var token = new ConfirmationToken();
        token.setStatus(ConfirmationTokenStatus.PENDING);
        token.setExpirationDate(LocalDate.now());

        assertThrows(DataIntegrityViolationException.class,
                () -> tokenRepository.saveAndFlush(token));
    }

    @Test
    void saveConfirmationTokenTest(){
        var token = new ConfirmationToken();
        token.setStatus(ConfirmationTokenStatus.PENDING);
        token.setExpirationDate(LocalDate.now());
        token.setAccountUser(user);

        tokenRepository.saveAndFlush(token);
        assertNotNull(token.getId());
    }
}