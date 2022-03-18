package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.config.exception.ApiException;
import org.gmalliaris.rental.rooms.config.exception.ApiExceptionMessageConstants;
import org.gmalliaris.rental.rooms.entity.UserRole;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.gmalliaris.rental.rooms.repository.UserRoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    public UserRoleService(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    public UserRole findUserRoleByName(UserRoleName name){
        return userRoleRepository.findByName(name)
                .orElseThrow( () -> {
                    var errMsg = String.format(ApiExceptionMessageConstants.ENTITY_NOT_FOUND_TEMPLATE,
                            UserRole.class.getSimpleName(), name);
                    throw new ApiException(HttpStatus.NOT_FOUND, errMsg);
                });
    }
}
