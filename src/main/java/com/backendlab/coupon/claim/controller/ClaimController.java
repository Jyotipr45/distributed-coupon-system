package com.backendlab.coupon.claim.controller;

import com.backendlab.coupon.claim.dto.request.CreateClaimRequest;
import com.backendlab.coupon.claim.dto.response.CreateClaimResponse;
import com.backendlab.coupon.claim.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons/{couponId}/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    public ResponseEntity<CreateClaimResponse> claimCoupon(
            @PathVariable Long couponId,
            @Valid @RequestBody CreateClaimRequest request
    ) {
        CreateClaimResponse response =
                claimService.claimCoupon(couponId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}