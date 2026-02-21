package com.revature.revshop.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.revature.revshop.exception.ProductNotFoundException;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.revature.revshop.dto.CartDTO;
import com.revature.revshop.dto.CartItemDTO;
import com.revature.revshop.model.Cart;
import com.revature.revshop.model.CartItem;
import com.revature.revshop.model.Product;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.ProductRepository;
import com.revature.revshop.repository.UserRepository;
import com.revature.revshop.service.CartItemService;
import com.revature.revshop.service.CartService;

@RestController
@RequestMapping("/api/carts")
public class CartController {

        private final CartService cartService;
        private final CartItemService cartItemService;
        private final UserRepository userRepository;
        private final ProductRepository productRepository;

        @Autowired
        public CartController(CartService cartService, CartItemService cartItemService, UserRepository userRepository,
                        ProductRepository productRepository) {
                this.cartService = cartService;
                this.cartItemService = cartItemService;
                this.userRepository = userRepository;
                this.productRepository = productRepository;
        }

        @PostMapping("/add")
        public ResponseEntity<CartItem> addItemToCart(@RequestParam Long userId, @RequestBody CartItemDTO request) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));
                Product product = productRepository.findById(request.getProductId())
                                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
                Cart cart = cartService.findOrCreateCart(user);
                CartItem item = cartItemService.addItemToCart(cart, product, request.getQuantity());
                return ResponseEntity.ok(item);
        }

        @GetMapping
        public ResponseEntity<CartDTO> getCart(@RequestParam Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));
                return cartService.getCartByUser(user)
                                .map(this::convertToDto)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        @DeleteMapping("/clear")
        public ResponseEntity<Void> clearCart(@RequestParam Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));
                Cart cart = cartService.getCartByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
                cartItemService.clearCart(cart);
                return ResponseEntity.ok().build();
        }

        private CartDTO convertToDto(Cart cart) {
                CartDTO dto = new CartDTO();
                dto.setCartId(cart.getCartId());

                List<CartItemDTO> itemDtos = cart.getCartItems().stream()
                                .map(item -> new CartItemDTO(
                                                item.getProduct().getProductId(),
                                                item.getQuantity(),
                                                item.getProduct().getName(),
                                                item.getProduct().getSellingPrice()))
                                .collect(Collectors.toList());
                dto.setItems(itemDtos);

                BigDecimal total = itemDtos.stream()
                                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                dto.setTotalPrice(total);

                return dto;
        }
}
