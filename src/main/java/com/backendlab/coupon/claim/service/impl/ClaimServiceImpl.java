package com.backendlab.coupon.claim.service.impl;

import com.backendlab.coupon.claim.dto.request.CreateClaimRequest;
import com.backendlab.coupon.claim.dto.response.CreateClaimResponse;
import com.backendlab.coupon.claim.entity.CouponClaim;
import com.backendlab.coupon.claim.enums.CouponClaimStatus;
import com.backendlab.coupon.claim.exception.CouponUnavailableException;
import com.backendlab.coupon.claim.exception.DuplicateClaimException;
import com.backendlab.coupon.claim.mapper.CouponClaimMapper;
import com.backendlab.coupon.claim.repository.CouponClaimRepository;
import com.backendlab.coupon.claim.service.ClaimService;
import com.backendlab.coupon.coupon.entity.Coupon;
import com.backendlab.coupon.coupon.enums.CouponStatus;
import com.backendlab.coupon.coupon.exception.CouponExpiredException;
import com.backendlab.coupon.coupon.exception.CouponInactiveException;
import com.backendlab.coupon.coupon.exception.CouponNotFoundException;
import com.backendlab.coupon.coupon.repository.CouponRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ClaimServiceImpl implements ClaimService {

    private final CouponRepository couponRepository;
    private final CouponClaimRepository couponClaimRepository;
    private final EntityManager entityManager;

    public ClaimServiceImpl(
            CouponRepository couponRepository,
            CouponClaimRepository couponClaimRepository,
            EntityManager entityManager
    ) {
        this.couponRepository = couponRepository;
        this.couponClaimRepository = couponClaimRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public CreateClaimResponse claimCoupon(
            Long couponId,
            CreateClaimRequest request
    ) {

        /*
         * ================================================================
         * STEP 1
         * Verify that the coupon exists.
         *
         * IMPORTANT:
         * We intentionally do NOT use findByIdForUpdate().
         *
         * The inventory update will be handled atomically by PostgreSQL.
         * ================================================================
         */
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() ->
                        new CouponNotFoundException(
                                "Coupon not found with id: " + couponId
                        )
                );

        /*
         * ================================================================
         * STEP 2
         * Validate coupon status.
         * ================================================================
         */
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new CouponInactiveException(
                    "Coupon is not active"
            );
        }

        /*
         * ================================================================
         * STEP 3
         * Validate coupon expiry.
         *
         * This is an early application-level validation.
         *
         * The atomic UPDATE also checks expiry at database level,
         * which protects us from concurrent/stale state.
         * ================================================================
         */
        if (coupon.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new CouponExpiredException(
                    "Coupon has expired"
            );
        }

        /*
         * ================================================================
         * STEP 4
         * Fast duplicate-claim check.
         *
         * This avoids unnecessary work when the user has already claimed
         * the coupon.
         *
         * IMPORTANT:
         * This check alone is NOT sufficient for concurrency.
         *
         * The database UNIQUE constraint:
         *
         * UNIQUE (coupon_id, user_id)
         *
         * remains the final protection against concurrent duplicate claims.
         * ================================================================
         */
        boolean alreadyClaimed =
                couponClaimRepository.existsByCouponIdAndUserId(
                        couponId,
                        request.userId()
                );

        if (alreadyClaimed) {
            throw new DuplicateClaimException(
                    "User has already claimed this coupon"
            );
        }

        /*
         * ================================================================
         * STEP 5
         * Atomically reserve one coupon.
         *
         * The repository executes an UPDATE similar to:
         *
         * UPDATE coupon
         * SET remaining_quantity = remaining_quantity - 1
         * WHERE id = ?
         *   AND status = ACTIVE
         *   AND expiry_time > CURRENT_TIMESTAMP
         *   AND remaining_quantity > 0
         *
         * PostgreSQL guarantees that this UPDATE is atomic.
         *
         * Result:
         *
         * 1 row updated -> inventory successfully reserved
         * 0 rows updated -> coupon unavailable
         * ================================================================
         */
        int updatedRows =
                couponRepository.decrementInventoryIfAvailable(
                        couponId
                );

        /*
         * ================================================================
         * STEP 6
         * No inventory was reserved.
         * ================================================================
         */
        if (updatedRows == 0) {
            throw new CouponUnavailableException(
                    "Coupon is sold out"
            );
        }

        /*
         * ================================================================
         * STEP 7
         * Obtain a JPA reference to the coupon.
         *
         * We do NOT use the previously loaded managed Coupon entity
         * for the relationship after the bulk UPDATE.
         *
         * The atomic UPDATE has already changed remaining_quantity
         * directly in the database.
         *
         * getReference() gives us a lightweight entity reference that
         * can be used by CouponClaim without loading the coupon again.
         * ================================================================
         */
        Coupon couponReference =
                entityManager.getReference(
                        Coupon.class,
                        couponId
                );

        /*
         * ================================================================
         * STEP 8
         * Create the claim.
         * ================================================================
         */
        CouponClaim claim = CouponClaim.builder()
                .coupon(couponReference)
                .userId(request.userId())
                .status(CouponClaimStatus.SUCCESS)
                .claimedAt(LocalDateTime.now())
                .build();

        /*
         * ================================================================
         * STEP 9
         * Save the claim.
         *
         * If this INSERT fails, the entire transaction rolls back.
         *
         * Therefore:
         *
         * inventory decrement
         *        +
         * claim creation
         *
         * behave as one atomic business transaction.
         * ================================================================
         */
        CouponClaim savedClaim =
                couponClaimRepository.save(claim);

        /*
         * ================================================================
         * STEP 10
         * Return successful claim.
         * ================================================================
         */
        return CouponClaimMapper.toCreateResponse(savedClaim);
    }
}