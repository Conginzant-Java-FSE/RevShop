package com.revature.revshop.controller;

import com.revature.revshop.dto.ApiResponse;
import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.model.Coupon;
import com.revature.revshop.service.CouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private static final Logger log = LoggerFactory.getLogger(CouponController.class);

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    /**
     * POST /api/coupons/validate
     * Body: { "code": "SAVE20", "orderAmount": 500.00 }
     * Returns: { "valid": true/false, "discountAmount": X, "message": "..." }
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateCoupon(
            @RequestBody Map<String, Object> body) {

        String code = body.get("code").toString();
        BigDecimal orderAmount = new BigDecimal(body.get("orderAmount").toString());

        log.info("POST /api/coupons/validate - code={} orderAmount={}", code, orderAmount);

        Map<String, Object> result = new HashMap<>();
        try {
            BigDecimal discount = couponService.validateCoupon(code, orderAmount);
            result.put("valid", true);
            result.put("discountAmount", discount);
            result.put("message", "Coupon applied! You save ₹" + discount);
        } catch (InvalidInputException e) {
            result.put("valid", false);
            result.put("discountAmount", 0);
            result.put("message", e.getMessage());
        }

        return ResponseEntity.ok(new ApiResponse<>("Coupon validation complete", result));
    }

    /**
     * POST /api/coupons
     * Create a new coupon (admin use)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(@RequestBody Coupon coupon) {
        log.info("POST /api/coupons - code={}", coupon.getCode());
        Coupon created = couponService.createCoupon(coupon);
        return ResponseEntity.ok(new ApiResponse<>("Coupon created successfully", created));
    }
}
