package com.workflowfox.eligibility.exception;

import com.workflowfox.eligibility.dto.ErrorResponse;
import com.workflowfox.eligibility.dto.ValidationErrorResponse;
import com.workflowfox.eligibility.dto.ValidationFieldError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Centralized exception handling (spec.md §14). Translates request
 * validation failures into the {@code ValidationErrorResponse} HTTP 422
 * shape and unexpected failures into the safe {@code ErrorResponse} HTTP
 * 500 shape defined by openapi.yaml. Never exposes stack traces, SQL,
 * file paths, or other infrastructure details in a response body.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String VALIDATION_DETAIL = "Validation failed. Provide a member ID and a valid check date.";
    private static final String TECHNICAL_FAILURE_DETAIL = "An unexpected error occurred. Please try again.";

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<ValidationFieldError> errors = ex.getConstraintViolations().stream()
                .map(GlobalExceptionHandler::toFieldError)
                .toList();
        return ResponseEntity.unprocessableEntity().body(new ValidationErrorResponse(VALIDATION_DETAIL, errors));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ValidationErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        String message = "%s is required.".formatted(ex.getParameterName());
        ValidationFieldError error = new ValidationFieldError(ex.getParameterName(), message);
        return ResponseEntity.unprocessableEntity().body(new ValidationErrorResponse(VALIDATION_DETAIL, List.of(error)));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ValidationErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "%s must be a valid ISO date (YYYY-MM-DD).".formatted(ex.getName());
        ValidationFieldError error = new ValidationFieldError(ex.getName(), message);
        return ResponseEntity.unprocessableEntity().body(new ValidationErrorResponse(VALIDATION_DETAIL, List.of(error)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected failure handling eligibility request", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(TECHNICAL_FAILURE_DETAIL));
    }

    private static ValidationFieldError toFieldError(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
        return new ValidationFieldError(field, violation.getMessage());
    }
}
