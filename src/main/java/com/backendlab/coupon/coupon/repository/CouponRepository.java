package com.backendlab.coupon.coupon.repository;

import com.backendlab.coupon.coupon.entity.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c
            FROM Coupon c
            WHERE c.id = :id
            """)
    Optional<Coupon> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("""
        UPDATE Coupon c
        SET c.remainingQuantity = c.remainingQuantity - 1
        WHERE c.id = :couponId
          AND c.status = com.backendlab.coupon.coupon.enums.CouponStatus.ACTIVE
          AND c.expiryTime > CURRENT_TIMESTAMP
          AND c.remainingQuantity > 0
        """)
    int decrementInventoryIfAvailable(
            @Param("couponId") Long couponId
    );
}