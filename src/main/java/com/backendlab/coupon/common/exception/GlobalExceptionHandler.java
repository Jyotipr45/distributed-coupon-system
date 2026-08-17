package com.backendlab.coupon.common.exception;

import com.backendlab.coupon.claim.exception.CouponUnavailableException;
import com.backendlab.coupon.claim.exception.DuplicateClaimException;
import com.backendlab.coupon.coupon.exception.CouponExpiredException;
import com.backendlab.coupon.coupon.exception.CouponInactiveException;
import com.backendlab.coupon.coupon.exception.CouponNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    public record ErrorResponse(
            Instant timestamp,
            int status,
            String code,
            String message
    ) {
    }
}