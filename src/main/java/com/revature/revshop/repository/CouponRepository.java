package com.revature.revshop.repository;

import com.revature.revshop.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Coupon c WHERE c.isActive = true AND (c.expiryDate IS NULL OR c.expiryDate > :now)")
    java.util.List<Coupon> findActiveCoupons(
            @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now);
}
