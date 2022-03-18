package org.gmalliaris.rental.rooms.entity;

public enum UserRoleName {
    ROLE_ADMIN("ADMIN"),
    ROLE_HOST("HOST"),
    ROLE_GUEST("GUEST");

    private final String value;

    UserRoleName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
