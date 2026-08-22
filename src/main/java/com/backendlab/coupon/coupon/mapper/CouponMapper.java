package com.backendlab.coupon.coupon.mapper;

import com.backendlab.coupon.coupon.dto.request.CreateCouponRequest;
import com.backendlab.coupon.coupon.dto.response.CreateCouponResponse;
import com.backendlab.coupon.coupon.entity.Coupon;
import com.backendlab.coupon.coupon.enums.CouponStatus;

public final class CouponMapper {

    private CouponMapper() {
        // Utility class
    }

    public static Coupon toEntity(CreateCouponRequest request) {

        return Coupon.builder()
                .couponCode(request.couponCode())
                .totalQuantity(request.totalQuantity())
                .remainingQuantity(request.totalQuantity())
                .status(CouponStatus.ACTIVE)
                .expiryTime(request.expiryTime())
                .build();
    }

    public static CreateCouponResponse toCreateResponse(Coupon coupon) {

        return new CreateCouponResponse(
                coupon.getId(),
                coupon.getCouponCode(),
                coupon.getTotalQuantity(),
                coupon.getRemainingQuantity(),
                coupon.getStatus(),
                coupon.getExpiryTime(),
                coupon.getCreatedAt()
        );
    }
}