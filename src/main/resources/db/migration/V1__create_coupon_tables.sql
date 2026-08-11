/*
===============================================================================
 Migration  : V1__create_coupon_tables.sql
 Author     : Backend Lab
 Purpose    : Create initial schema for Coupon Distribution System

 This migration creates:
    1. coupon
    2. coupon_claim

 Notes:
    - No optimistic locking yet.
    - No idempotency support yet.
    - No audit table yet.
    - Those will be introduced in later migrations.
===============================================================================
*/

-- ============================================================================
-- TABLE : coupon
-- Purpose:
-- Stores coupon campaign information.
-- Example:
--      WELCOME100
--      Quantity : 100
--      Remaining : 100
-- ============================================================================

CREATE TABLE coupon (

                        id BIGSERIAL PRIMARY KEY,

                        coupon_code VARCHAR(50) NOT NULL,

                        total_quantity INTEGER NOT NULL,

                        remaining_quantity INTEGER NOT NULL,

                        status VARCHAR(20) NOT NULL,

                        expiry_time TIMESTAMP NOT NULL,

                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT uk_coupon_code UNIQUE (coupon_code),

                        CONSTRAINT chk_total_quantity
                            CHECK (total_quantity >= 0),

                        CONSTRAINT chk_remaining_quantity
                            CHECK (remaining_quantity >= 0),

                        CONSTRAINT chk_remaining_less_than_total
                            CHECK (remaining_quantity <= total_quantity)

);

-- ============================================================================
-- INDEX
-- Frequently searched using coupon code.
-- ============================================================================

CREATE INDEX idx_coupon_code
    ON coupon(coupon_code);





-- ============================================================================
-- TABLE : coupon_claim
--
-- One record represents one user's coupon claim.
--
-- Example
--
-- User 1001
-- Claimed WELCOME100
-- SUCCESS
-- ============================================================================
CREATE TABLE coupon_claim (

                              id BIGSERIAL PRIMARY KEY,

                              coupon_id BIGINT NOT NULL,

                              user_id VARCHAR(100) NOT NULL,

                              status VARCHAR(20) NOT NULL,

                              claimed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_coupon_claim_coupon
                                  FOREIGN KEY (coupon_id)
                                      REFERENCES coupon(id)

);

-- ============================================================================
-- INDEX
-- Frequently queried by coupon.
-- ============================================================================

CREATE INDEX idx_coupon_claim_coupon
    ON coupon_claim(coupon_id);

-- ============================================================================
-- INDEX
-- Frequently queried by user.
-- ============================================================================

CREATE INDEX idx_coupon_claim_user
    ON coupon_claim(user_id);