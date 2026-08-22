package com.backendlab.coupon.claim.repository;

import com.backendlab.coupon.claim.entity.CouponClaim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponClaimRepository extends JpaRepository<CouponClaim, Long> {

    boolean existsByCouponIdAndUserId(Long couponId, String userId);
}