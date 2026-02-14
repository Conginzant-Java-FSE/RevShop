package com.revature.revshop.dto;

public class ReviewDTO {
    private Integer productId;
    private Integer rating;
    private String reviewText;

    public ReviewDTO() {
    }

    public ReviewDTO(Integer productId, Integer rating, String reviewText) {
        this.productId = productId;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
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
}
