package com.revature.revshop;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.revature.revshop.controller.ReviewController;
import com.revature.revshop.dto.ReviewDTO;
import com.revature.revshop.model.Product;
import com.revature.revshop.model.Review;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.ProductRepository;
import com.revature.revshop.repository.UserRepository;
import com.revature.revshop.service.ReviewService;

public class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ReviewController reviewController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
    }

    @Test
    public void testAddReview() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setName("John Doe");

        Product product = new Product();
        product.setProductId(1L);

        Review review = new Review(product, user, 5, "Great product!");
        review.setReviewId(1L);
        review.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewService.addReview(any(Review.class))).thenReturn(review);

        String jsonRequest = "{\"productId\": 1, \"userId\": 1, \"rating\": 5, \"reviewText\": \"Great product!\"}";

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.userName").value("John Doe"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.reviewText").value("Great product!"));
    }

    @Test
    public void testGetReviewsByUser() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setName("John Doe");

        Review review = new Review();
        review.setReviewId(1L);
        review.setUser(user);
        review.setRating(4);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewService.getReviewsByUser(user)).thenReturn(Collections.singletonList(review));

        mockMvc.perform(get("/api/reviews/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].rating").value(4));
    }
}
