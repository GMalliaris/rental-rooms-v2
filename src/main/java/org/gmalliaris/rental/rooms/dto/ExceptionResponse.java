package org.gmalliaris.rental.rooms.dto;

import org.springframework.http.HttpStatus;

public class ExceptionResponse{
    private final HttpStatus httpStatus;
    private final String message;

    public ExceptionResponse(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}