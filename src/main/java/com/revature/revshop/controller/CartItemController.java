package com.revature.revshop.controller;

import com.revature.revshop.dto.ApiResponse;
import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.model.CartItem;
import com.revature.revshop.service.CartItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart-items")
public class CartItemController {

    private static final Logger log = LoggerFactory.getLogger(CartItemController.class);

    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItem>> updateItemQuantity(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Integer> request) {

        log.info("PUT /api/cart-items/{}", cartItemId);
        Integer quantity = request.get("quantity");

        if (quantity == null || quantity <= 0) {
            throw new InvalidInputException("Quantity must be greater than 0");
        }

        CartItem item = cartItemService.updateItemQuantity(cartItemId, quantity);

        if (item == null) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        return ResponseEntity.ok(
                new ApiResponse<>("Cart item updated successfully", item));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeItemFromCart(
            @PathVariable Long cartItemId) {

        log.info("DELETE /api/cart-items/{}", cartItemId);
        CartItem item = cartItemService.updateItemQuantity(cartItemId, 0);

        if (item == null) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        cartItemService.removeItemFromCart(cartItemId);

        return ResponseEntity.ok(
                new ApiResponse<>("Cart item removed successfully", null));
    }
}