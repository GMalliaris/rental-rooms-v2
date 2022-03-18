package org.gmalliaris.rental.rooms.config.exception;

public final class ApiExceptionMessageConstants {

    public static final String USER_TABLES_INIT_ERROR = "Failed to initialize user tables on startup";
    public static final String USED_EMAIL = "Provided email is already in use.";
    public static final String USED_PHONE_NUMBER = "Provided phone number is already in use.";
    public static final String INVALID_CREDENTIALS = "Invalid username and/or password.";
    public static final String ENTITY_NOT_FOUND_TEMPLATE = "%s entity '%s' not found.";

    private ApiExceptionMessageConstants(){
        // hide implicit constructor
    }
}
