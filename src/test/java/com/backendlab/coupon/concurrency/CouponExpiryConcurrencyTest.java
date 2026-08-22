package com.backendlab.coupon.concurrency;

import com.backendlab.coupon.claim.dto.request.CreateClaimRequest;
import com.backendlab.coupon.claim.service.ClaimService;
import com.backendlab.coupon.coupon.entity.Coupon;
import com.backendlab.coupon.coupon.enums.CouponStatus;
import com.backendlab.coupon.coupon.exception.CouponExpiredException;
import com.backendlab.coupon.coupon.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponExpiryConcurrencyTest {

    @Autowired
    private ClaimService claimService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void shouldRejectConcurrentClaimsForExpiredCoupon()
            throws Exception {

        int concurrentRequests = 100;

        Coupon coupon = Coupon.builder()
                .couponCode(
                        "EXPIRY_TEST_" + System.nanoTime()
                )
                .totalQuantity(100)
                .remainingQuantity(100)
                .status(CouponStatus.ACTIVE)
                .expiryTime(LocalDateTime.now().minusSeconds(5))
                .build();

        Coupon savedCoupon =
                couponRepository.save(coupon);

        Long couponId = savedCoupon.getId();

        ExecutorService executor =
                Executors.newFixedThreadPool(20);

        CountDownLatch start =
                new CountDownLatch(1);

        List<Future<Boolean>> results =
                new ArrayList<>();

        for (int i = 0; i < concurrentRequests; i++) {

            final int userNumber = i;

            results.add(
                    executor.submit(() -> {

                        start.await();

                        try {

                            claimService.claimCoupon(
                                    couponId,
                                    new CreateClaimRequest(
                                            "expiry-user-" + userNumber
                                    )
                            );

                            return true;

                        } catch (CouponExpiredException exception) {

                            return false;
                        }
                    })
            );
        }

        start.countDown();

        int successfulClaims = 0;

        for (Future<Boolean> result : results) {

            if (result.get()) {
                successfulClaims++;
            }
        }

        executor.shutdown();

        Coupon finalCoupon =
                couponRepository.findById(couponId)
                        .orElseThrow();

        assertThat(successfulClaims)
                .isEqualTo(0);

        assertThat(finalCoupon.getRemainingQuantity())
                .isEqualTo(100);
    }
}