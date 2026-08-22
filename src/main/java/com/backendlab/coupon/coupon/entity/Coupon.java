package com.backendlab.coupon.coupon.entity;

import com.backendlab.coupon.common.entity.BaseEntity;
import com.backendlab.coupon.coupon.enums.CouponStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "coupon",
        indexes = {
                @Index(
                        name = "idx_coupon_code",
                        columnList = "coupon_code"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Coupon extends BaseEntity {

    @Column(
            name = "coupon_code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String couponCode;

    @Column(
            name = "total_quantity",
            nullable = false
    )
    private Integer totalQuantity;

    @Column(
            name = "remaining_quantity",
            nullable = false
    )
    private Integer remainingQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status;

    @Column(
            name = "expiry_time",
            nullable = false
    )
    private LocalDateTime expiryTime;
}