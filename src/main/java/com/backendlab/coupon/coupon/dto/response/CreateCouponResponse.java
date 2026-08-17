package com.backendlab.coupon.coupon.dto.response;

import com.backendlab.coupon.coupon.enums.CouponStatus;

import java.time.LocalDateTime;

public record CreateCouponResponse(
        Long id,
        String couponCode,
        Integer totalQuantity,
        Integer remainingQuantity,
        CouponStatus status,
        LocalDateTime expiryTime,
        LocalDateTime createdAt
) {
}