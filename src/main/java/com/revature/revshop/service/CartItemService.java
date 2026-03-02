package com.revature.revshop.service;

import com.revature.revshop.model.Cart;
import com.revature.revshop.model.CartItem;
import com.revature.revshop.model.Product;
import com.revature.revshop.repository.CartItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CartItemService {

    private static final Logger log = LoggerFactory.getLogger(CartItemService.class);

    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartItemService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public CartItem addItemToCart(Cart cart, Product product, Integer quantity) {
        log.info("Adding item to cart productId={} qty={}", product.getProductId(), quantity);
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem(cart, product, quantity);
            return cartItemRepository.save(newItem);
        }
    }

    public CartItem updateItemQuantity(Long cartItemId, Integer quantity) {
        log.info("Updating cart item id={} qty={}", cartItemId, quantity);
        return cartItemRepository.findById(cartItemId)
                .map(item -> {
                    item.setQuantity(quantity);
                    return cartItemRepository.save(item);
                }).orElse(null);
    }

    public void removeItemFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void removeItemFromCart(Cart cart, Product product) {
        cartItemRepository.deleteByCartAndProduct(cart, product);
    }

    public List<CartItem> getCartItemsByCart(Cart cart) {
        return cartItemRepository.findByCart(cart);
    }

    @Transactional
    public void clearCart(Cart cart) {
        log.info("Clearing cart id={}", cart.getCartId());
        cartItemRepository.deleteByCart(cart);
    }

    public Optional<CartItem> getCartItemByCartAndProduct(Cart cart, Product product) {
        return cartItemRepository.findByCartAndProduct(cart, product);
    }
}
