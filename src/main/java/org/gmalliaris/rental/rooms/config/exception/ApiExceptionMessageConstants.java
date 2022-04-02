package org.gmalliaris.rental.rooms.config.exception;

public final class ApiExceptionMessageConstants {

    public static final String USER_TABLES_INIT_ERROR = "Failed to initialize user tables on startup.";
    public static final String INVALID_USER_ROLES_REGISTRATION = "Cannot register as admin user.";
    public static final String USED_EMAIL = "Provided email is already in use.";
    public static final String USED_PHONE_NUMBER = "Provided phone number is already in use.";
    public static final String INVALID_CREDENTIALS = "Invalid username and/or password.";
    public static final String ENTITY_NOT_FOUND_TEMPLATE = "%s entity '%s' not found.";
    public static final String ENTITY_OF_ENTITY_NOT_FOUND_TEMPLATE = "%s entity of %s entity '%s' not found.";
    public static final String FAILED_EMAIL = "Failed to send email for %s.";
    public static final String CONFIRMATION_TOKEN_EXPIRED = "Confirmation token has expired.";
    public static final String CONFIRMATION_TOKEN_ALREADY_USED = "Confirmation token has already used to confirm registration.";
    public static final String USER_ALREADY_CONFIRMED = "User registration has already been confirmed.";

    private ApiExceptionMessageConstants(){
        // hide implicit constructor
    }
}
