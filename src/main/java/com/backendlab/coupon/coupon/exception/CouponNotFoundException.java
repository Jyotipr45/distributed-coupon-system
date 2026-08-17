package com.backendlab.coupon.coupon.exception;

public class CouponNotFoundException extends RuntimeException {

    public CouponNotFoundException(String message) {
        super(message);
    }
}
