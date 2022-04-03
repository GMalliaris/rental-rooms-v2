package org.gmalliaris.rental.rooms.config.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ExceptionControllerAdvice.class);

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ExceptionResponse exception(Exception exception) {
        logException(exception);
        return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IllegalStateException.class)
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

