package com.backendlab.coupon.coupon.controller;

import com.backendlab.coupon.coupon.dto.request.CreateCouponRequest;
import com.backendlab.coupon.coupon.dto.response.CreateCouponResponse;
import com.backendlab.coupon.coupon.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public ResponseEntity<CreateCouponResponse> createCoupon(
            @Valid @RequestBody CreateCouponRequest request
    ) {
        CreateCouponResponse response = couponService.createCoupon(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreateCouponResponse> getCouponById(
            @PathVariable Long id
    ) {
        CreateCouponResponse response = couponService.getCouponById(id);

        return ResponseEntity.ok(response);
    }
}