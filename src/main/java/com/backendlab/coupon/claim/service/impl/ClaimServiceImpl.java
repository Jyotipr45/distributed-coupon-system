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
import org.springframework.dao.DataIntegrityViolationException;
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

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() ->
                        new CouponNotFoundException(
                                "Coupon not found with id: " + couponId
                        )
                );

        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new CouponInactiveException(
                    "Coupon is not active"
            );
        }

        if (coupon.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new CouponExpiredException(
                    "Coupon has expired"
            );
        }

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

        int updatedRows =
                couponRepository.decrementInventoryIfAvailable(
                        couponId
                );

        if (updatedRows == 0) {
            throw new CouponUnavailableException(
                    "Coupon is sold out"
            );
        }

        Coupon couponReference =
                entityManager.getReference(
                        Coupon.class,
                        couponId
                );

        CouponClaim claim = CouponClaim.builder()
                .coupon(couponReference)
                .userId(request.userId())
                .status(CouponClaimStatus.SUCCESS)
                .claimedAt(LocalDateTime.now())
                .build();

        try {

            CouponClaim savedClaim =
                    couponClaimRepository.saveAndFlush(claim);

            return CouponClaimMapper.toCreateResponse(savedClaim);

        } catch (DataIntegrityViolationException exception) {

            if (isDuplicateClaimViolation(exception)) {
                throw new DuplicateClaimException(
                        "User has already claimed this coupon"
                );
            }

            throw exception;
        }
    }

    private boolean isDuplicateClaimViolation(
            DataIntegrityViolationException exception
    ) {

        Throwable current = exception;

        while (current != null) {

            if (current.getMessage() != null
                    && current.getMessage()
                    .contains("uk_coupon_claim_coupon_user")) {

                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}