package com.revature.revshop.controller;

import com.revature.revshop.dto.*;
import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.model.Product;
import com.revature.revshop.model.Review;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.ProductRepository;
import com.revature.revshop.repository.UserRepository;
import com.revature.revshop.service.ReviewService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ReviewController(ReviewService reviewService,
                            UserRepository userRepository,
                            ProductRepository productRepository) {
        this.reviewService = reviewService;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }


    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> addReview(
            @Valid @RequestBody ReviewDTO request) {

        if (request.getUserId() == null || request.getProductId() == null) {
            throw new InvalidInputException("UserId and ProductId are required");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Review review = new Review(
                product,
                user,
                request.getRating(),
                request.getReviewText()
        );

        Review savedReview = reviewService.addReview(review);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Review added successfully",
                        convertToDto(savedReview)
                ));
    }


    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsByProduct(
            @PathVariable Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        List<ReviewResponseDTO> responses =
                reviewService.getReviewsByProduct(product)
                        .stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>("Reviews fetched successfully", responses)
        );
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsByUser(
            @PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<ReviewResponseDTO> responses =
                reviewService.getReviewsByUser(user)
                        .stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>("Reviews fetched successfully", responses)
        );
    }


    private ReviewResponseDTO convertToDto(Review review) {

        ReviewResponseDTO dto = new ReviewResponseDTO();

        dto.setReviewId(review.getReviewId());

        if (review.getProduct() != null)
            dto.setProductId(review.getProduct().getProductId());

        if (review.getUser() != null) {
            dto.setUserId(review.getUser().getUserId());
            dto.setUserName(review.getUser().getName());
        }

        dto.setRating(review.getRating());
        dto.setReviewText(review.getReviewText());
        dto.setCreatedAt(review.getCreatedAt());

        return dto;
    }
}