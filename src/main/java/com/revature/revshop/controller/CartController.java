package com.revature.revshop.controller;

import com.revature.revshop.dto.*;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.model.*;
import com.revature.revshop.repository.*;
import com.revature.revshop.service.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/carts")
public class CartController {

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
        public ResponseEntity<ApiResponse<CartItem>> addItemToCart(
                @PathVariable Long userId,
                @Valid @RequestBody CartItemDTO request) {

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Product product = productRepository.findById(request.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

                Cart cart = cartService.findOrCreateCart(user);
                CartItem item = cartItemService.addItemToCart(cart, product, request.getQuantity());

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponse<>("Item added to cart successfully", item));
        }


        @GetMapping("/user/{userId}")
        public ResponseEntity<ApiResponse<CartDTO>> getCart(@PathVariable Long userId) {

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Cart cart = cartService.getCartByUser(user)
                        .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

                CartDTO dto = convertToDto(cart);

                return ResponseEntity.ok(
                        new ApiResponse<>("Cart fetched successfully", dto)
                );
        }



        @DeleteMapping("/user/{userId}/clear")
        public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable Long userId) {

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Cart cart = cartService.getCartByUser(user)
                        .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

                cartItemService.clearCart(cart);

                return ResponseEntity.ok(
                        new ApiResponse<>("Cart cleared successfully", null)
                );
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
                                item.getProduct().getSellingPrice()))
                        .collect(Collectors.toList());

                dto.setItems(itemDtos);

                BigDecimal total = itemDtos.stream()
                        .map(item -> item.getPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                dto.setTotalPrice(total);

                return dto;
        }
}