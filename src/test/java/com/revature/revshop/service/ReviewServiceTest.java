package com.revature.revshop.service;

import com.revature.revshop.model.Product;
import com.revature.revshop.model.Review;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Review review;
    private Product product;
    private User user;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setProductId(1L);

        user = new User();
        user.setUserId(1L);

        review = new Review(product, user, 4, "Great product!");
        review.setReviewId(1L);
    }

    @Test
    void testAddReview() {
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review savedReview = reviewService.addReview(review);

        assertNotNull(savedReview);
        assertEquals(4, savedReview.getRating());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testGetReviewsByProduct() {
        when(reviewRepository.findByProduct(product)).thenReturn(Arrays.asList(review));

        List<Review> reviews = reviewService.getReviewsByProduct(product);

        assertFalse(reviews.isEmpty());
        assertEquals(1, reviews.size());
        assertEquals(review.getReviewId(), reviews.get(0).getReviewId());
    }

    @Test
    void testGetReviewsByUser() {
        when(reviewRepository.findByUser(user)).thenReturn(Arrays.asList(review));

        List<Review> reviews = reviewService.getReviewsByUser(user);

        assertFalse(reviews.isEmpty());
        assertEquals(1, reviews.size());
    }

    @Test
    void testGetReviewById_Exists() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        Optional<Review> result = reviewService.getReviewById(1L);

        assertTrue(result.isPresent());
        assertEquals(review.getReviewId(), result.get().getReviewId());
    }

    @Test
    void testGetReviewById_NotExists() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Review> result = reviewService.getReviewById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetReviewsByRating() {
        when(reviewRepository.findByRating(4)).thenReturn(Arrays.asList(review));

        List<Review> reviews = reviewService.getReviewsByRating(4);

        assertFalse(reviews.isEmpty());
        assertEquals(4, reviews.get(0).getRating());
    }

    @Test
    void testDeleteReview() {
        doNothing().when(reviewRepository).deleteById(1L);

        reviewService.deleteReview(1L);

        verify(reviewRepository, times(1)).deleteById(1L);
    }

    @Test
    void testUpdateReview_Exists() {
        Review updatedInfo = new Review();
        updatedInfo.setRating(5);
        updatedInfo.setReviewText("Excellent!");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review); // Save returns the modified existing review

        Review result = reviewService.updateReview(1L, updatedInfo);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Excellent!", result.getReviewText());
        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void testUpdateReview_NotExists() {
        Review updatedInfo = new Review();
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        Review result = reviewService.updateReview(1L, updatedInfo);

        assertNull(result);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testGetAverageRatingByProduct_WithReviews() {
        Review r1 = new Review();
        r1.setRating(4);
        Review r2 = new Review();
        r2.setRating(5);

        when(reviewRepository.findByProduct(product)).thenReturn(Arrays.asList(r1, r2));

        Double average = reviewService.getAverageRatingByProduct(product);

        assertEquals(4.5, average);
    }

    @Test
    void testGetAverageRatingByProduct_EmptyReviews() {
        when(reviewRepository.findByProduct(product)).thenReturn(new ArrayList<>());

        Double average = reviewService.getAverageRatingByProduct(product);

        assertEquals(0.0, average);
    }

    @Test
    void testGetAverageRatingByProduct_NullReviews() {
        when(reviewRepository.findByProduct(product)).thenReturn(null);

        Double average = reviewService.getAverageRatingByProduct(product);

        assertEquals(0.0, average);
    }
}
