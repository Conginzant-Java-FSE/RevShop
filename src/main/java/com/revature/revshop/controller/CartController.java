package com.revature.revshop.controller;

import com.revature.revshop.dto.*;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.model.*;
import com.revature.revshop.repository.*;
import com.revature.revshop.service.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/carts")
public class CartController {

        private static final Logger log = LoggerFactory.getLogger(CartController.class);
        private static final String USER_NOT_FOUND = "User not found";

        private final CartService cartService;
        private final CartItemService cartItemService;
        private final UserRepository userRepository;
        private final ProductRepository productRepository;

        public CartController(CartService cartService,
                        CartItemService cartItemService,
                        UserRepository userRepository,
                        ProductRepository productRepository) {
                this.cartService = cartService;
                this.cartItemService = cartItemService;
                this.userRepository = userRepository;
                this.productRepository = productRepository;
        }

        @PostMapping("/user/{userId}/add")
        public ResponseEntity<ApiResponse<Void>> addItemToCart(
                        @PathVariable Long userId,
                        @Valid @RequestBody CartItemDTO request) {

                log.info("POST /api/carts/user/{}/add - productId={}", userId, request.getProductId());

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

                if (user.getRole() == Role.SELLER) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(new ApiResponse<>("Sellers are not allowed to add items to cart", null));
                }

                Product product = productRepository.findById(request.getProductId())
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

                Cart cart = cartService.findOrCreateCart(user);
                // We still perform the logic, but we don't send the 'item' back in the response
                cartItemService.addItemToCart(cart, product, request.getQuantity());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new ApiResponse<>("Item added to cart successfully", null));
        }

        @GetMapping("/user/{userId}")
        public ResponseEntity<ApiResponse<CartDTO>> getCart(@PathVariable Long userId) {

                log.info("GET /api/carts/user/{}", userId);

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

                Cart cart = cartService.getCartByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

                CartDTO dto = convertToDto(cart);

                return ResponseEntity.ok(
                                new ApiResponse<>("Cart fetched successfully", dto));
        }

        @DeleteMapping("/user/{userId}/clear")
        public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable Long userId) {

                log.info("DELETE /api/carts/user/{}/clear", userId);

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

                Cart cart = cartService.getCartByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

                cartItemService.clearCart(cart);

                return ResponseEntity.ok(
                                new ApiResponse<>("Cart cleared successfully", null));
        }

        private CartDTO convertToDto(Cart cart) {

                CartDTO dto = new CartDTO();
                dto.setCartId(cart.getCartId());

                List<CartItemDTO> itemDtos = cart.getCartItems().stream()
                                .map(item -> new CartItemDTO(
                                                item.getCartItemId(),
                                                item.getProduct().getProductId(),
                                                item.getQuantity(),
                                                item.getProduct().getName(),
                                                item.getProduct().getSellingPrice(),
                                                item.getProduct().getImageUrl()))
                                .toList();

                dto.setItems(itemDtos);

                BigDecimal total = itemDtos.stream()
                                .map(item -> item.getPrice()
                                                .multiply(BigDecimal.valueOf(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                dto.setTotalPrice(total);

                return dto;
        }
}
