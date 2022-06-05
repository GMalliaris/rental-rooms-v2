package org.gmalliaris.rental.rooms.repository;

import org.gmalliaris.rental.rooms.PostgresTestContainer;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.entity.ConfirmationToken;
import org.gmalliaris.rental.rooms.entity.ConfirmationTokenStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        PostgresTestContainer.DATA_JPA_TEST_JDBC_URL_PROPERTY
})
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

    @Test
    void findByAccountUserIdTest(){
        var token = new ConfirmationToken();
        token.setStatus(ConfirmationTokenStatus.PENDING);
        token.setExpirationDate(LocalDate.now());
        token.setAccountUser(user);
        tokenRepository.saveAndFlush(token);
        assertNotNull(token.getId());

        var user2 = new AccountUser();
        user2.setEmail("test2@example.eg");
        user2.setPassword("12345678");
        user2.setFirstName("firstName");
        userRepository.saveAndFlush(user2);
        assertNotNull(user2.getId());

        var token2 = new ConfirmationToken();
        token2.setStatus(ConfirmationTokenStatus.PENDING);
        token2.setExpirationDate(LocalDate.now());
        token2.setAccountUser(user2);
        tokenRepository.saveAndFlush(token2);
        assertNotNull(token2.getId());

        var result = tokenRepository.findByAccountUserId(user.getId());
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(token, result.get());

        var result2 = tokenRepository.findByAccountUserId(user2.getId());
        assertNotNull(result2);
        assertTrue(result2.isPresent());
        assertEquals(token2, result2.get());
    }
}