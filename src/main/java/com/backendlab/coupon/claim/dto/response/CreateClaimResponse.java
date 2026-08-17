package com.backendlab.coupon.claim.dto.response;

import com.backendlab.coupon.claim.enums.CouponClaimStatus;

import java.time.LocalDateTime;

public record CreateClaimResponse(
        Long id,
        Long couponId,
        String userId,
        CouponClaimStatus status,
        LocalDateTime claimedAt
) {
}