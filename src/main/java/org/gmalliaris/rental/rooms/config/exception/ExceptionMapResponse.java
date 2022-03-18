package org.gmalliaris.rental.rooms.config.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ExceptionMapResponse {
    private final HttpStatus httpStatus;
    private final Map<String, String> messages;

    public ExceptionMapResponse(HttpStatus httpStatus, Map<String, String> messages) {
        this.httpStatus = httpStatus;
        this.messages = messages;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Map<String, String> getMessages() {
        return messages;
    }
}
