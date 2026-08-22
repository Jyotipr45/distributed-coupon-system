package com.backendlab.coupon.claim.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateClaimRequest(

        @NotBlank(message = "User ID is required")
        @Size(max = 100, message = "User ID must not exceed 100 characters")
        String userId
) {
}