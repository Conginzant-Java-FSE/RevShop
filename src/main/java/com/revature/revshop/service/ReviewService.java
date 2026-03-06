package com.revature.revshop.service;

import com.revature.revshop.model.Product;
import com.revature.revshop.model.Review;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final com.revature.revshop.repository.OrdersRepository ordersRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
            com.revature.revshop.repository.OrdersRepository ordersRepository) {
        this.reviewRepository = reviewRepository;
        this.ordersRepository = ordersRepository;
    }

    public boolean isProductPurchasedByUser(Long userId, Long productId) {
        return ordersRepository.existsByPurchasedProduct(userId, productId);
    }

    public Review addReview(Review review) {
        log.info("Adding review for productId={}", review.getProduct().getProductId());
        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByProduct(Product product) {
        return reviewRepository.findByProduct(product);
    }

    public List<Review> getReviewsByUser(User user) {
        return reviewRepository.findByUser(user);
    }

    public Optional<Review> getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }

    public List<Review> getReviewsByRating(Integer rating) {
        return reviewRepository.findByRating(rating);
    }

    public void deleteReview(Long reviewId) {
        log.info("Deleting review id={}", reviewId);
        reviewRepository.deleteById(reviewId);
    }

    public Review updateReview(Long reviewId, Review updatedReview) {
        log.info("Updating review id={}", reviewId);
        return reviewRepository.findById(reviewId)
                .map(existingReview -> {
                    existingReview.setRating(updatedReview.getRating());
                    existingReview.setReviewText(updatedReview.getReviewText());
                    return reviewRepository.save(existingReview);
                })
                .orElse(null);
    }

    public Double getAverageRatingByProduct(Product product) {
        List<Review> reviews = reviewRepository.findByProduct(product);
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}
