package com.revature.revshop.controller;

import com.revature.revshop.model.CartItem;
import com.revature.revshop.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart-items")
public class CartItemController {

    private final CartItemService cartItemService;

    @Autowired
    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItem> updateItemQuantity(@PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        CartItem item = cartItemService.updateItemQuantity(cartItemId, quantity);
        if (item != null) {
            return ResponseEntity.ok(item);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeItemFromCart(@PathVariable Long cartItemId) {
        cartItemService.removeItemFromCart(cartItemId);
        return ResponseEntity.ok().build();
    }
}
