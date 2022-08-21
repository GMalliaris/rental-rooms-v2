package org.gmalliaris.rental.rooms.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gmalliaris.rental.rooms.config.exception.ApiException;
import org.gmalliaris.rental.rooms.config.exception.ApiExceptionMessageConstants;
import org.gmalliaris.rental.rooms.config.exception.ExceptionMapResponse;
import org.gmalliaris.rental.rooms.config.exception.ExceptionResponse;
import org.gmalliaris.rental.rooms.dto.CurrentAccountUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.TreeMap;

@RestControllerAdvice
public class ExceptionControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionControllerAdvice.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500",
                    description = "Internal error",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)) })
    })
    public ExceptionResponse exception(Exception exception) {
        logException(exception);
        return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500",
                    description = "Internal error",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)) })
    })
    public ExceptionResponse illegalStateException(IllegalStateException illegalStateException) {
        logException(illegalStateException);
        return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, illegalStateException.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ExceptionResponse accessDeniedException(AccessDeniedException exception) {
        logException(exception);
        return new ExceptionResponse(HttpStatus.FORBIDDEN, ApiExceptionMessageConstants.FORBIDDEN_MESSAGE);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ExceptionResponse> apiException(ApiException apiException){
        logException(apiException);
        return ResponseEntity.status(apiException.getStatus())
                .body(new ExceptionResponse(apiException.getStatus(), apiException.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionMapResponse> methodArgumentNotValidException(MethodArgumentNotValidException methodArgumentNotValidException){
        logException(methodArgumentNotValidException);
        var fieldErrorMap = new TreeMap<String, String>();
        methodArgumentNotValidException.getBindingResult()
                .getFieldErrors()
                .forEach(error -> fieldErrorMap.put(error.getField(),
                            error.getDefaultMessage()));

        return ResponseEntity.status(400)
                .body(new ExceptionMapResponse(HttpStatus.BAD_REQUEST,
                        fieldErrorMap));
    }

    private void logException(Throwable exception){
        logger.info("Handled exception:", exception);
    }
}

