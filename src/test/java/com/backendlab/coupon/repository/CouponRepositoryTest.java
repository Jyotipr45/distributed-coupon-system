package com.backendlab.coupon.repository;

import com.backendlab.coupon.coupon.entity.Coupon;
import com.backendlab.coupon.coupon.enums.CouponStatus;
import com.backendlab.coupon.coupon.repository.CouponRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponRepositoryTest {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void shouldSaveAndLoadCoupon() {

        Coupon coupon = Coupon.builder()
                .couponCode("REPOSITORY_TEST_WELCOME_002")
                .totalQuantity(100)
                .remainingQuantity(100)
                .status(CouponStatus.ACTIVE)
                .expiryTime(LocalDateTime.now().plusDays(7))
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);

        assertThat(savedCoupon.getId()).isNotNull();

        Coupon foundCoupon = couponRepository
                .findById(savedCoupon.getId())
                .orElseThrow();

        assertThat(foundCoupon.getCouponCode())
                .isEqualTo("REPOSITORY_TEST_WELCOME_002");

        assertThat(foundCoupon.getRemainingQuantity())
                .isEqualTo(100);
    }

    @Test
    @Transactional
    void shouldUpdateManagedEntityUsingDirtyChecking() {

        Coupon coupon = Coupon.builder()
                .couponCode("DIRTY_CHECK_TEST_2")
                .totalQuantity(100)
                .remainingQuantity(100)
                .status(CouponStatus.ACTIVE)
                .expiryTime(LocalDateTime.now().plusDays(7))
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);

        savedCoupon.setRemainingQuantity(50);

        entityManager.flush();

        entityManager.clear();

        Coupon updatedCoupon = couponRepository
                .findById(savedCoupon.getId())
                .orElseThrow();

        assertThat(updatedCoupon.getRemainingQuantity())
                .isEqualTo(50);
    }
}