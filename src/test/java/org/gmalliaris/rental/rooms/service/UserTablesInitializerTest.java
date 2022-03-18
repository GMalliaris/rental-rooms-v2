package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.config.exception.ApiException;
import org.gmalliaris.rental.rooms.config.exception.ApiExceptionMessageConstants;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.entity.UserRole;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.gmalliaris.rental.rooms.repository.AccountUserRepository;
import org.gmalliaris.rental.rooms.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTablesInitializerTest {

    @InjectMocks
    private UserTablesInitializer userTablesInitializer;

    @Mock
    private AccountUserRepository accountUserRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    void onApplicationEvent_rolesExist_adminExists(){
        when(userRoleRepository.findByName(any(UserRoleName.class)))
                .thenReturn(Optional.of(mock(UserRole.class)));
        when(accountUserRepository.countAdminAccountUsers())
                .thenReturn(1L);

        userTablesInitializer.onApplicationEvent(mock(ApplicationReadyEvent.class));

        verify(userRoleRepository, never()).save(any());
        verify(accountUserRepository, never()).save(any());
    }

    @Test
    void onApplicationEvent_initError(){
        var mockUserRole = mock(UserRole.class);
        when(mockUserRole.getName())
                .thenReturn(UserRoleName.ROLE_HOST);

        when(userRoleRepository.findByName(any(UserRoleName.class)))
                .thenReturn(Optional.of(mockUserRole));
        when(accountUserRepository.countAdminAccountUsers())
                .thenReturn(0L);

        var exception = assertThrows(ApiException.class,
                () -> userTablesInitializer.onApplicationEvent(mock(ApplicationReadyEvent.class)));
        assertEquals(ApiExceptionMessageConstants.USER_TABLES_INIT_ERROR, exception.getMessage());
    }

    @Test
    void onApplicationEvent_rolesNotExist_adminNotExist(){
        when(userRoleRepository.findByName(any(UserRoleName.class)))
                .thenReturn(Optional.empty());
        when(accountUserRepository.countAdminAccountUsers())
                .thenReturn(0L);
        var encoded = "encoded";
        when(bCryptPasswordEncoder.encode(anyString()))
                .thenReturn(encoded);

        var adminEmail = "admin@example.eg";
        var adminPassword = "123456789";
        ReflectionTestUtils.setField(userTablesInitializer, "adminEmail", adminEmail);
        ReflectionTestUtils.setField(userTablesInitializer, "adminPassword", adminPassword);

        userTablesInitializer.onApplicationEvent(mock(ApplicationReadyEvent.class));

        var roleNames = Arrays.stream(UserRoleName.values())
                .collect(Collectors.toSet());
        var rolesArgCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository, times(roleNames.size()))
                .save(rolesArgCaptor.capture());
        var userRoles = rolesArgCaptor.getAllValues();
        assertEquals(roleNames.size(), userRoles.size());
        assertTrue(roleNames.containsAll(userRoles.stream().map(UserRole::getName)
                .collect(Collectors.toList())));

        verify(bCryptPasswordEncoder).encode(adminPassword);

        var adminCaptor = ArgumentCaptor.forClass(AccountUser.class);
        verify(accountUserRepository).save(adminCaptor.capture());
        var admin = adminCaptor.getValue();
        assertEquals(adminEmail, admin.getEmail());
        assertEquals(encoded, admin.getPassword());
    }
}