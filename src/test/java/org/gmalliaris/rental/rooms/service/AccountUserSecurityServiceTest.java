package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.repository.AccountUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountUserSecurityServiceTest {

    @InjectMocks
    private AccountUserSecurityService accountUserSecurityService;

    @Mock
    private AccountUserRepository accountUserRepository;

    @Test
    void loadUserByUsername_noUserFound() {
        when(accountUserRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        var email = "random@example.eg";
        var result = accountUserSecurityService.loadUserByUsername(email);
        assertNull(result);
    }

    @Test
    void loadUserByUsername_userFound() {
        var email = "random@example.eg";
        var user = mock(AccountUser.class);
        when(user.getEmail()).thenReturn(email);

        when(accountUserRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        var result = accountUserSecurityService.loadUserByUsername(email);
        assertNotNull(result);
        assertEquals(email, result.getUsername());
    }

    @Test
    void loadUserById_noUserFound() {
        when(accountUserRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        var userId = UUID.randomUUID();
        var result = accountUserSecurityService.loadUserById(userId);
        assertNull(result);
    }

    @Test
    void loadUserById_userFound() {
        var email = "random@example.eg";
        var userId = UUID.randomUUID();
        var user = mock(AccountUser.class);
        when(user.getEmail()).thenReturn(email);

        when(accountUserRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(user));

        var result = accountUserSecurityService.loadUserById(userId);
        assertNotNull(result);
        assertEquals(email, result.getUsername());
    }
}