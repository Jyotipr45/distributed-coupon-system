package com.backendlab.coupon.claim.mapper;

import com.backendlab.coupon.claim.dto.response.CreateClaimResponse;
import com.backendlab.coupon.claim.entity.CouponClaim;

public final class CouponClaimMapper {

    private CouponClaimMapper() {
        // Utility class
    }

    public static CreateClaimResponse toCreateResponse(CouponClaim claim) {

        return new CreateClaimResponse(
                claim.getId(),
                claim.getCoupon().getId(),
                claim.getUserId(),
                claim.getStatus(),
                claim.getClaimedAt()
        );
    }
}