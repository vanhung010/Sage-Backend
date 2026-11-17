package com.sagares.saga_res_api.common.exception;

import com.sagares.saga_res_api.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req
    ) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                req.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ResourceSavedException.class)
    public ResponseEntity<ErrorResponse> handleSaveFailed(
            ResourceSavedException ex, HttpServletRequest req
    ) {
        ErrorResponse body = new ErrorResponse(
          HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(UpdateException.class)
    public ResponseEntity<ErrorResponse> handleUpdateFailed(
            UpdateException ex, HttpServletRequest req
    ) {
        ErrorResponse body = new ErrorResponse(
          HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBuisinessFailed(BusinessException exception, HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse(
                exception.getStatus().value(),
                exception.getError(),
                exception.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(exception.getStatus()).body(errorResponse);
    }
}
