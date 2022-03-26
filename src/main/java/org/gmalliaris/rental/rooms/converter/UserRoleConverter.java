package org.gmalliaris.rental.rooms.converter;

import org.gmalliaris.rental.rooms.entity.UserRole;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.modelmapper.AbstractConverter;

import java.util.Objects;

public class UserRoleConverter extends AbstractConverter<UserRole, UserRoleName> {

    @Override
    protected UserRoleName convert(UserRole userRole) {
        Objects.requireNonNull(userRole,
                "Cannot convert null UserRole");
        Objects.requireNonNull(userRole.getName(),
                "Cannot convert UserRole of null name");
        return userRole.getName();
    }
}
