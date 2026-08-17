package com.backendlab.coupon.coupon.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateCouponRequest(

        @NotBlank(message = "Coupon code is required")
        @Size(max = 50, message = "Coupon code must not exceed 50 characters")
        String couponCode,

        @NotNull(message = "Total quantity is required")
        @Min(value = 1, message = "Total quantity must be greater than 0")
        Integer totalQuantity,

        @NotNull(message = "Expiry time is required")
        @Future(message = "Expiry time must be in the future")
        LocalDateTime expiryTime
) {
}