package com.revature.revshop.service;

import com.revature.revshop.model.Product;
import com.revature.revshop.model.Review;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review addReview(Review review) {
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
        reviewRepository.deleteById(reviewId);
    }

    public Review updateReview(Long reviewId, Review updatedReview) {
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
