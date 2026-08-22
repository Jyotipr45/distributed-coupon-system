package com.backendlab.coupon.claim.entity;

import com.backendlab.coupon.claim.enums.CouponClaimStatus;
import com.backendlab.coupon.common.entity.BaseEntity;
import com.backendlab.coupon.coupon.entity.Coupon;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "coupon_claim",
        indexes = {
                @Index(
                        name = "idx_coupon_claim_coupon",
                        columnList = "coupon_id"
                ),
                @Index(
                        name = "idx_coupon_claim_user",
                        columnList = "user_id"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Access(AccessType.FIELD)
public class CouponClaim extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "coupon_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_coupon_claim_coupon")
    )
    private Coupon coupon;

    @Column(
            name = "user_id",
            nullable = false,
            length = 100
    )
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponClaimStatus status;

    @Column(
            name = "claimed_at",
            nullable = false
    )
    private LocalDateTime claimedAt;

}