package com.backendlab.coupon.coupon.service;

import com.backendlab.coupon.coupon.dto.request.CreateCouponRequest;
import com.backendlab.coupon.coupon.dto.response.CreateCouponResponse;

public interface CouponService {

    CreateCouponResponse createCoupon(CreateCouponRequest request);

    CreateCouponResponse getCouponById(Long id);
}