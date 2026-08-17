package com.backendlab.coupon.claim.exception;

public class CouponUnavailableException extends RuntimeException {

    public CouponUnavailableException(String message) {
        super(message);
    }
}