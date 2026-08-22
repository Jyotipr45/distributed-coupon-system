package com.backendlab.coupon.concurrency;

import com.backendlab.coupon.claim.dto.request.CreateClaimRequest;
import com.backendlab.coupon.claim.exception.DuplicateClaimException;
import com.backendlab.coupon.claim.service.ClaimService;
import com.backendlab.coupon.coupon.entity.Coupon;
import com.backendlab.coupon.coupon.enums.CouponStatus;
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
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponDuplicateClaimConcurrencyTest {

    @Autowired
    private ClaimService claimService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void shouldAllowOnlyOneClaimForSameUserUnderConcurrentRequests()
            throws Exception {

        // ------------------------------------------------------------
        // ARRANGE
        // ------------------------------------------------------------

        int concurrentRequests = 30;

        String userId = "duplicate-user";

        Coupon coupon = Coupon.builder()
                .couponCode(
                        "DUPLICATE_CLAIM_TEST_" + System.nanoTime()
                )
                .totalQuantity(10)
                .remainingQuantity(10)
                .status(CouponStatus.ACTIVE)
                .expiryTime(LocalDateTime.now().plusMinutes(10))
                .build();

        Coupon savedCoupon =
                couponRepository.save(coupon);

        Long couponId = savedCoupon.getId();

        ExecutorService executor =
                Executors.newFixedThreadPool(concurrentRequests);

        CountDownLatch ready =
                new CountDownLatch(concurrentRequests);

        CountDownLatch start =
                new CountDownLatch(1);

        List<Future<Boolean>> results =
                new ArrayList<>();

        try {

            // --------------------------------------------------------
            // ACT
            // --------------------------------------------------------

            for (int i = 0; i < concurrentRequests; i++) {

                results.add(
                        executor.submit(() -> {

                            ready.countDown();

                            start.await();

                            try {

                                claimService.claimCoupon(
                                        couponId,
                                        new CreateClaimRequest(userId)
                                );

                                return true;

                            } catch (DuplicateClaimException exception) {

                                return false;
                            }
                        })
                );
            }

            // Make sure all tasks reached the starting point.
            assertThat(
                    ready.await(10, TimeUnit.SECONDS)
            ).isTrue();

            // Release all workers.
            start.countDown();

            int successfulClaims = 0;

            for (Future<Boolean> result : results) {

                if (result.get(30, TimeUnit.SECONDS)) {
                    successfulClaims++;
                }
            }

            // --------------------------------------------------------
            // ASSERT
            // --------------------------------------------------------

            Coupon finalCoupon =
                    couponRepository.findById(couponId)
                            .orElseThrow();

            assertThat(successfulClaims)
                    .isEqualTo(1);

            assertThat(finalCoupon.getRemainingQuantity())
                    .isEqualTo(9);

        } finally {

            executor.shutdownNow();

            assertThat(
                    executor.awaitTermination(
                            10,
                            TimeUnit.SECONDS
                    )
            ).isTrue();
        }
    }
}