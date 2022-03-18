package org.gmalliaris.rental.rooms.config.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException{

    private final HttpStatus status;
    private final String message;

    public ApiException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
