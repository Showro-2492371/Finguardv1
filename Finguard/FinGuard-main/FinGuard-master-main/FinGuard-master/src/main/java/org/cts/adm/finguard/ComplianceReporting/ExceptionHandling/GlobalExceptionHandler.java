package org.cts.adm.finguard.ComplianceReporting.ExceptionHandling;

import lombok.extern.slf4j.Slf4j;
import org.cts.adm.finguard.CustomerOnboarding.Exception.DuplicateContactInfoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 🔴 Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {

        log.error("Resource not found: {}", ex.getMessage());

        return new ResponseEntity<>(
                new ErrorResponse("NOT_FOUND", ex.getMessage(), LocalDateTime.now()),
                HttpStatus.NOT_FOUND
        );
    }

    // 🟠 Bad Request
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {

        log.warn("Bad request: {}", ex.getMessage());

        return new ResponseEntity<>(
                new ErrorResponse("BAD_REQUEST", ex.getMessage(), LocalDateTime.now()),
                HttpStatus.BAD_REQUEST
        );
    }

    // 🟠 Conflict - duplicate signup details
    @ExceptionHandler(DuplicateContactInfoException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateContact(DuplicateContactInfoException ex) {

        log.warn("Duplicate customer signup blocked: {}", ex.getMessage());

        return new ResponseEntity<>(
                new ErrorResponse("CONFLICT", ex.getMessage(), LocalDateTime.now()),
                HttpStatus.CONFLICT
        );
    }

    // 🟠 Conflict - fallback for DB unique constraints
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {

        log.warn("Data integrity violation: {}", ex.getMessage());

        return new ResponseEntity<>(
                new ErrorResponse("CONFLICT", "Duplicate value violates a unique constraint.", LocalDateTime.now()),
                HttpStatus.CONFLICT
        );
    }

    // 🔴 Generic Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {

        log.error("Internal error occurred", ex);

        return new ResponseEntity<>(
                new ErrorResponse("INTERNAL_ERROR", ex.getMessage(), LocalDateTime.now()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // 🔴 Response Status Exception
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String reason = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();

        return new ResponseEntity<>(
                new ErrorResponse(status.name(), reason, LocalDateTime.now()),
                status
        );
    }
}