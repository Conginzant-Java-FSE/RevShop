package com.revature.revshop.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.revature.revshop.dto.ReviewDTO;
import com.revature.revshop.dto.ReviewResponseDTO;
import com.revature.revshop.model.Product;
import com.revature.revshop.model.Review;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.ProductRepository;
import com.revature.revshop.repository.UserRepository;
import com.revature.revshop.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ReviewController(ReviewService reviewService, UserRepository userRepository,
            ProductRepository productRepository) {
        this.reviewService = reviewService;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> addReview(@RequestBody ReviewDTO request) {
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Review review = new Review(product, user, request.getRating(), request.getReviewText());
        Review savedReview = reviewService.addReview(review);
        return ResponseEntity.ok(convertToDto(savedReview));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByProduct(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        List<ReviewResponseDTO> responses = reviewService.getReviewsByProduct(product).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<ReviewResponseDTO> responses = reviewService.getReviewsByUser(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private ReviewResponseDTO convertToDto(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setReviewId(review.getReviewId());
        if (review.getProduct() != null) {
            dto.setProductId(review.getProduct().getProductId());
        }
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
