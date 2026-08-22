package com.backendlab.coupon.claim.exception;

public class DuplicateClaimException extends RuntimeException {

    public DuplicateClaimException(String message) {
        super(message);
    }
}