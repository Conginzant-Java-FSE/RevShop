

package com.revature.revshop.dto;

import java.time.LocalDateTime;

public class TrackingDetailsDTO {
    private Integer trackingId;
    private Integer orderId;
    private String status;
    private String description;
    private LocalDateTime updatedAt;

    public TrackingDetailsDTO() {
    }

    public TrackingDetailsDTO(Integer trackingId, Integer orderId, String status, String description,
                              LocalDateTime updatedAt) {
        this.trackingId = trackingId;
        this.orderId = orderId;
        this.status = status;
        this.description = description;
        this.updatedAt = updatedAt;
    }

    public Integer getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(Integer trackingId) {
        this.trackingId = trackingId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
