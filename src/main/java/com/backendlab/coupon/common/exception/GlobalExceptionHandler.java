package com.backendlab.coupon.common.exception;

import com.backendlab.coupon.claim.exception.CouponUnavailableException;
import com.backendlab.coupon.claim.exception.DuplicateClaimException;
import com.backendlab.coupon.coupon.exception.CouponExpiredException;
import com.backendlab.coupon.coupon.exception.CouponInactiveException;
import com.backendlab.coupon.coupon.exception.CouponNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============================================================
    // COUPON NOT FOUND
    // ============================================================

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCouponNotFound(
            CouponNotFoundException exception
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "COUPON_NOT_FOUND",
                exception.getMessage()
        );
    }

    // ============================================================
    // COUPON INACTIVE
    // ============================================================

    @ExceptionHandler(CouponInactiveException.class)
    public ResponseEntity<ErrorResponse> handleCouponInactive(
            CouponInactiveException exception
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "COUPON_INACTIVE",
                exception.getMessage()
        );
    }

    // ============================================================
    // COUPON EXPIRED
    // ============================================================

    @ExceptionHandler(CouponExpiredException.class)
    public ResponseEntity<ErrorResponse> handleCouponExpired(
            CouponExpiredException exception
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "COUPON_EXPIRED",
                exception.getMessage()
        );
    }

    // ============================================================
    // DUPLICATE CLAIM
    // ============================================================

    @ExceptionHandler(DuplicateClaimException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateClaim(
            DuplicateClaimException exception
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "DUPLICATE_CLAIM",
                exception.getMessage()
        );
    }

    // ============================================================
    // COUPON SOLD OUT
    // ============================================================

    @ExceptionHandler(CouponUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleCouponUnavailable(
            CouponUnavailableException exception
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "COUPON_SOLD_OUT",
                exception.getMessage()
        );
    }

    // ============================================================
    // REQUEST VALIDATION ERROR
    // ============================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {

            fieldErrors.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ValidationErrorResponse response = new ValidationErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Request validation failed",
                fieldErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // ============================================================
    // COMMON ERROR RESPONSE BUILDER
    // ============================================================

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String code,
            String message
    ) {

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                status.value(),
                code,
                message
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // ============================================================
    // STANDARD BUSINESS ERROR RESPONSE
    // ============================================================

    public record ErrorResponse(
            Instant timestamp,
            int status,
            String code,
            String message
    ) {
    }

    // ============================================================
    // VALIDATION ERROR RESPONSE
    // ============================================================

    public record ValidationErrorResponse(
            Instant timestamp,
            int status,
            String code,
            String message,
            Map<String, String> fieldErrors
    ) {
    }
}