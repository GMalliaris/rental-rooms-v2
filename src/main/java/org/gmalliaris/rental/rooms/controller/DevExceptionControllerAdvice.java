package org.gmalliaris.rental.rooms.controller;

import org.gmalliaris.rental.rooms.dto.DevExceptionResponse;
import org.gmalliaris.rental.rooms.dto.ExceptionResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@ResponseBody
@Profile("dev")
public class DevExceptionControllerAdvice {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ExceptionResponse exception(Exception exception) {
        exception.printStackTrace();
        var firstTrace = exception.getStackTrace()[0];
        var occurrence = String.format("%s#%s:%s",
                firstTrace.getClassName(),
                firstTrace.getMethodName(),
                firstTrace.getLineNumber());
        return new DevExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception.getClass(), occurrence);
    }
}

