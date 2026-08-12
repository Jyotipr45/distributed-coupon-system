package com.backendlab.coupon.coupon.repository;

import com.backendlab.coupon.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}