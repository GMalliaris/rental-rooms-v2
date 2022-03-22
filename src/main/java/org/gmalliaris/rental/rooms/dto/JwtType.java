package org.gmalliaris.rental.rooms.dto;

public enum JwtType {
    ACCESS("access"),
    REFRESH("refresh");

    private final String value;

    JwtType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
