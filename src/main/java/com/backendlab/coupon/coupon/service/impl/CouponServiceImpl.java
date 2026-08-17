package com.backendlab.coupon.coupon.service.impl;

import com.backendlab.coupon.coupon.dto.request.CreateCouponRequest;
import com.backendlab.coupon.coupon.dto.response.CreateCouponResponse;
import com.backendlab.coupon.coupon.entity.Coupon;
import com.backendlab.coupon.coupon.mapper.CouponMapper;
import com.backendlab.coupon.coupon.repository.CouponRepository;
import com.backendlab.coupon.coupon.service.CouponService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    public CouponServiceImpl(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    @Transactional
    public CreateCouponResponse createCoupon(CreateCouponRequest request) {

        Coupon coupon = CouponMapper.toEntity(request);

        Coupon savedCoupon = couponRepository.save(coupon);

        return CouponMapper.toCreateResponse(savedCoupon);
    }

    @Override
    @Transactional(readOnly = true)
    public CreateCouponResponse getCouponById(Long id) {

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Coupon not found with id: " + id)
                );

        return CouponMapper.toCreateResponse(coupon);
    }
}