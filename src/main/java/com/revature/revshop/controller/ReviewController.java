package com.revature.revshop.controller;

import com.revature.revshop.dto.*;
import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.model.Product;
import com.revature.revshop.model.Review;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.ProductRepository;
import com.revature.revshop.repository.ReviewRepository;
import com.revature.revshop.repository.UserRepository;
import com.revature.revshop.service.ReviewService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

        private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

        private final ReviewService reviewService;
        private final UserRepository userRepository;
        private final ProductRepository productRepository;
        private final ReviewRepository reviewRepository;

        private static final String USER_NOT_FOUND = "User not found";
        private static final String PRODUCT_NOT_FOUND = "Product not found";

        public ReviewController(ReviewService reviewService,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        ReviewRepository reviewRepository) {
                this.reviewService = reviewService;
                this.userRepository = userRepository;
                this.productRepository = productRepository;
                this.reviewRepository = reviewRepository;
        }

        @PostMapping
        public ResponseEntity<ApiResponse<ReviewResponseDTO>> addReview(
                        @Valid @RequestBody ReviewDTO request) {

                log.info("POST /api/reviews - userId={} productId={}", request.getUserId(), request.getProductId());
                if (request.getUserId() == null || request.getProductId() == null) {
                        throw new InvalidInputException("UserId and ProductId are required");
                }

                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

                Product product = productRepository.findById(request.getProductId())
                                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));

                Review review = new Review(
                                product,
                                user,
                                request.getRating(),
                                request.getReviewText());

                Review savedReview = reviewService.addReview(review);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new ApiResponse<>(
                                                "Review added successfully",
                                                convertToDto(savedReview)));
        }

        @GetMapping("/product/{productId}")
        public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsByProduct(
                        @PathVariable Long productId) {

                log.info("GET /api/reviews/product/{}", productId);
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));

                List<ReviewResponseDTO> responses = reviewService.getReviewsByProduct(product)
                                .stream()
                                .map(this::convertToDto)
                                .toList();

                return ResponseEntity.ok(
                                new ApiResponse<>("Reviews fetched successfully", responses));
        }

        @GetMapping("/product/{productId}/average-rating")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getAverageRating(
                        @PathVariable Long productId) {

                log.info("GET /api/reviews/product/{}/average-rating", productId);
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));
                Double avg = reviewService.getAverageRatingByProduct(product);
                int count = reviewService.getReviewsByProduct(product).size();

                Map<String, Object> result = new HashMap<>();
                result.put("averageRating", Math.round(avg * 10.0) / 10.0);
                result.put("reviewCount", count);

                return ResponseEntity.ok(new ApiResponse<>("Average rating fetched", result));
        }

        @GetMapping("/check")
        public ResponseEntity<ApiResponse<Boolean>> checkUserReviewed(
                        @RequestParam Long userId,
                        @RequestParam Long productId) {

                log.info("GET /api/reviews/check - userId={} productId={}", userId, productId);
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));

                boolean hasReviewed = reviewRepository.findByProductAndUser(product, user).isPresent();
                return ResponseEntity.ok(new ApiResponse<>("Check complete", hasReviewed));
        }

        @GetMapping("/user/{userId}")
        public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsByUser(
                        @PathVariable Long userId) {

                log.info("GET /api/reviews/user/{}", userId);
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

                List<ReviewResponseDTO> responses = reviewService.getReviewsByUser(user)
                                .stream()
                                .map(this::convertToDto)
                                .toList();

                return ResponseEntity.ok(
                                new ApiResponse<>("Reviews fetched successfully", responses));
        }

        @DeleteMapping("/{reviewId}")
        public ResponseEntity<ApiResponse<Void>> deleteReview(
                        @PathVariable Long reviewId) {

                log.info("DELETE /api/reviews/{}", reviewId);
                // Validate the review exists before deleting
                Review review = reviewService.getReviewById(reviewId)
                                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
                log.debug("Deleting review with id: {}", review.getReviewId());
                reviewService.deleteReview(reviewId);

                return ResponseEntity.ok(
                                new ApiResponse<>("Review deleted successfully", null));
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