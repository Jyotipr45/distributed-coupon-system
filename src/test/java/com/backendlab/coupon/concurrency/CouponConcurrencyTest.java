package com.backendlab.coupon.concurrency;

import com.backendlab.coupon.claim.dto.request.CreateClaimRequest;
import com.backendlab.coupon.claim.exception.CouponUnavailableException;
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
class CouponConcurrencyTest {

    @Autowired
    private ClaimService claimService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void shouldNotOversellCouponUnderConcurrentClaims()
            throws Exception {

        // ------------------------------------------------------------
        // ARRANGE
        // ------------------------------------------------------------

        int couponQuantity = 10;
        int concurrentUsers = 100;

        Coupon coupon = Coupon.builder()
                .couponCode(
                        "CONCURRENCY_TEST_" + System.nanoTime()
                )
                .totalQuantity(couponQuantity)
                .remainingQuantity(couponQuantity)
                .status(CouponStatus.ACTIVE)
                .expiryTime(LocalDateTime.now().plusMinutes(10))
                .build();

        Coupon savedCoupon =
                couponRepository.save(coupon);

        Long couponId =
                savedCoupon.getId();

        /*
         * We intentionally use one worker per concurrent user.
         *
         * Why?
         *
         * The test uses a barrier/latch to make all users ready
         * before releasing them simultaneously.
         *
         * If we used only 20 threads for 100 users, the first
         * 20 threads would block waiting for "start", while the
         * remaining 80 tasks could never execute.
         */
        ExecutorService executor =
                Executors.newFixedThreadPool(concurrentUsers);

        CountDownLatch ready =
                new CountDownLatch(concurrentUsers);

        CountDownLatch start =
                new CountDownLatch(1);

        List<Future<Boolean>> results =
                new ArrayList<>(concurrentUsers);

        try {

            // --------------------------------------------------------
            // ACT
            // --------------------------------------------------------

            for (int i = 0; i < concurrentUsers; i++) {

                final int userNumber = i;

                results.add(
                        executor.submit(() -> {

                            /*
                             * Tell the test that this worker
                             * has reached the starting point.
                             */
                            ready.countDown();

                            /*
                             * Wait until every worker is ready.
                             */
                            if (!start.await(30, TimeUnit.SECONDS)) {
                                throw new IllegalStateException(
                                        "Worker timed out waiting for test start"
                                );
                            }

                            String userId =
                                    "concurrent-user-" + userNumber;

                            try {

                                claimService.claimCoupon(
                                        couponId,
                                        new CreateClaimRequest(userId)
                                );

                                return true;

                            } catch (CouponUnavailableException exception) {

                                return false;
                            }
                        })
                );
            }

            /*
             * Make sure all 100 workers actually reached the
             * starting point.
             *
             * This prevents the test from silently starting
             * with only a subset of workers.
             */
            boolean allWorkersReady =
                    ready.await(30, TimeUnit.SECONDS);

            assertThat(allWorkersReady)
                    .as("All concurrent workers should become ready")
                    .isTrue();

            /*
             * Release all workers.
             */
            start.countDown();

            // --------------------------------------------------------
            // COLLECT RESULTS
            // --------------------------------------------------------

            int successfulClaims = 0;

            for (Future<Boolean> result : results) {

                boolean successful =
                        result.get(30, TimeUnit.SECONDS);

                if (successful) {
                    successfulClaims++;
                }
            }

            // --------------------------------------------------------
            // ASSERT
            // --------------------------------------------------------

            Coupon finalCoupon =
                    couponRepository.findById(couponId)
                            .orElseThrow();

            /*
             * Only 10 users should successfully claim
             * a coupon with quantity 10.
             */
            assertThat(successfulClaims)
                    .isEqualTo(couponQuantity);

            /*
             * Inventory must reach exactly zero.
             */
            assertThat(finalCoupon.getRemainingQuantity())
                    .isEqualTo(0);

            /*
             * Inventory must never become negative.
             */
            assertThat(finalCoupon.getRemainingQuantity())
                    .isGreaterThanOrEqualTo(0);

        } finally {

            /*
             * Always shut down the executor.
             *
             * Important for Gradle/JUnit so worker threads
             * don't keep the test process alive.
             */
            executor.shutdownNow();

            if (!executor.awaitTermination(
                    10,
                    TimeUnit.SECONDS
            )) {

                throw new IllegalStateException(
                        "Executor did not terminate cleanly"
                );
            }
        }
    }
}