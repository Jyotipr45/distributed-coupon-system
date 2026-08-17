package com.backendlab.coupon.claim.service;

import com.backendlab.coupon.claim.dto.request.CreateClaimRequest;
import com.backendlab.coupon.claim.dto.response.CreateClaimResponse;

public interface ClaimService {

    CreateClaimResponse claimCoupon(
            Long couponId,
            CreateClaimRequest request
    );
}