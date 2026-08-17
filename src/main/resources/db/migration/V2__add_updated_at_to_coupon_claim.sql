/*
===============================================================================
 Migration  : V2__add_updated_at_to_coupon_claim.sql
 Author     : Backend Lab
 Purpose    : Add updated_at required by BaseEntity
===============================================================================
*/

ALTER TABLE coupon_claim
    ADD COLUMN updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP;