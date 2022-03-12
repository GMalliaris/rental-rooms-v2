package org.gmalliaris.rental.rooms.dto;

import org.springframework.http.HttpStatus;

public class DevExceptionResponse extends ExceptionResponse{
    private final Class<?> clazz;
    private final String occurrence;

    public DevExceptionResponse(HttpStatus httpStatus, String message, Class<?> clazz, String occurrence) {
        super(httpStatus, message);
        this.occurrence = occurrence;
        this.clazz = clazz;
    }

    public String getOccurrence() {
        return occurrence;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}