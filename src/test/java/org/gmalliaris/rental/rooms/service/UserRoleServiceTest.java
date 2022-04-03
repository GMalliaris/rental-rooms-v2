package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.config.exception.ApiException;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.gmalliaris.rental.rooms.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {

    @InjectMocks
    private UserRoleService userRoleService;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Test
    void findUserRoleByNameTest_notFound(){
        when(userRoleRepository.findByName(any(UserRoleName.class)))
                .thenReturn(Optional.empty());

        var exception = assertThrows(ApiException.class,
                () -> userRoleService.findUserRoleByName(UserRoleName.ROLE_GUEST));
        var errMsg = "UserRole entity 'ROLE_GUEST' not found.";
        assertEquals(errMsg, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(userRoleRepository).findByName(UserRoleName.ROLE_GUEST);
    }
}