package com.revature.revshop.dto;


import jakarta.validation.constraints.NotNull;

public class CancelOrderRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;

    public CancelOrderRequestDTO() {
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
