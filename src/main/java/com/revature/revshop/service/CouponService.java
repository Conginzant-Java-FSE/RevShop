package com.revature.revshop.service;

import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.model.Coupon;
import com.revature.revshop.repository.CouponRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class CouponService {

    private static final Logger log = LoggerFactory.getLogger(CouponService.class);

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    /**
     * Validates a coupon code against the given order amount.
     * 
     * @return the calculated discount amount
     * @throws InvalidInputException if the coupon is invalid for any reason
     */
    public BigDecimal validateCoupon(String code, BigDecimal orderAmount) {
        log.info("Validating coupon code={} for orderAmount={}", code, orderAmount);

        Coupon coupon = couponRepository.findByCode(code.toUpperCase().trim())
                .orElseThrow(() -> new InvalidInputException("Coupon code not found"));

        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            throw new InvalidInputException("This coupon is no longer active");
        }
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidInputException("This coupon has expired");
        }
        if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new InvalidInputException("This coupon has reached its usage limit");
        }
        if (coupon.getMinOrderAmount() != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new InvalidInputException(
                    "Minimum order amount of ₹" + coupon.getMinOrderAmount() + " required for this coupon");
        }

        BigDecimal discount;
        if (coupon.getDiscountType() == Coupon.DiscountType.FIXED) {
            discount = coupon.getDiscountValue();
        } else {
            // PERCENT
            discount = orderAmount.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        // Discount cannot exceed order amount
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }

    /**
     * Increments usage count once a coupon has been applied to an order.
     */
    public void applyCoupon(String code) {
        log.info("Applying coupon code={}", code);
        couponRepository.findByCode(code.toUpperCase().trim()).ifPresent(coupon -> {
            coupon.setUsageCount(coupon.getUsageCount() + 1);
            couponRepository.save(coupon);
        });
    }

    public Coupon createCoupon(Coupon coupon) {
        coupon.setCode(coupon.getCode().toUpperCase().trim());
        log.info("Creating coupon code={}", coupon.getCode());
        return couponRepository.save(coupon);
    }

    public java.util.List<Coupon> getAllActiveCoupons() {
        log.info("Fetching all active coupons");
        return couponRepository.findAllByIsActiveTrue();
    }
}
