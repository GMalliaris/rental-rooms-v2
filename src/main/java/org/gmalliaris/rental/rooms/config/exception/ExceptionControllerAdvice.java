package org.gmalliaris.rental.rooms.config.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.TreeMap;

@ControllerAdvice
@ResponseBody
public class ExceptionControllerAdvice {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ExceptionResponse exception(Exception exception) {
        exception.printStackTrace();
        return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AccessDeniedException.class)
    public ExceptionResponse accessDeniedException(AccessDeniedException exception) {
        exception.printStackTrace();
        return new ExceptionResponse(HttpStatus.UNAUTHORIZED, "Access is denied.");
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ExceptionResponse> apiException(ApiException apiException){
        apiException.printStackTrace();
        return ResponseEntity.status(apiException.getStatus())
                .body(new ExceptionResponse(apiException.getStatus(), apiException.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionMapResponse> methodArgumentNotValidException(MethodArgumentNotValidException methodArgumentNotValidException){
        methodArgumentNotValidException.printStackTrace();

        var fieldErrorMap = new TreeMap<String, String>();
        methodArgumentNotValidException.getBindingResult()
                .getFieldErrors()
                .forEach(error -> fieldErrorMap.put(error.getField(),
                            error.getDefaultMessage()));

        return ResponseEntity.status(400)
                .body(new ExceptionMapResponse(HttpStatus.BAD_REQUEST,
                        fieldErrorMap));
    }
}

