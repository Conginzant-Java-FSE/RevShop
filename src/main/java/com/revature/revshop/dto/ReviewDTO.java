package com.revature.revshop.dto;

public class ReviewDTO {
    private Long productId;
    private Long userId;
    private Integer rating;
    private String reviewText;

    public ReviewDTO() {
    }

    public ReviewDTO(Long productId, Integer rating, String reviewText) {
        this.productId = productId;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
