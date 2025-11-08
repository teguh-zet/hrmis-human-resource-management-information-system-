package com.jasamedika.hrmis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex) {
        // Error dari API (business logic) - return 501 dengan pesan manusiawi
        // Error seperti BAD_REQUEST, UNAUTHORIZED, FORBIDDEN adalah error dari API
        if (ex.getStatusCode() == HttpStatus.BAD_REQUEST || 
            ex.getStatusCode() == HttpStatus.UNAUTHORIZED ||
            ex.getStatusCode() == HttpStatus.FORBIDDEN ||
            ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED) // 501
                    .body(ex.getReason() != null ? ex.getReason() : "Terjadi kesalahan pada sistem");
        }
        // Error lainnya tetap sesuai status code aslinya
        return ResponseEntity.status(ex.getStatusCode())
                .body(ex.getReason());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal Server Error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
    }
}

