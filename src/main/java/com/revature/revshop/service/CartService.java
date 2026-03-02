package com.revature.revshop.service;

import com.revature.revshop.model.Cart;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;

    @Autowired
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Optional<Cart> getCartByUser(User user) {
        return cartRepository.findByUser(user);
    }

    public Cart createCart(User user) {
        log.info("Creating cart for userId={}", user.getUserId());
        Cart cart = new Cart(user);
        return cartRepository.save(cart);
    }

    public Cart saveCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public Cart findOrCreateCart(User user) {
        log.info("Finding or creating cart for userId={}", user.getUserId());
        return cartRepository.findByUser(user)
                .orElseGet(() -> createCart(user));
    }

    public BigDecimal calculateCartTotal(Cart cart) {
        if (cart == null || cart.getCartItems() == null) {
            return BigDecimal.ZERO;
        }
        return cart.getCartItems().stream()
                .map(item -> item.getProduct().getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
