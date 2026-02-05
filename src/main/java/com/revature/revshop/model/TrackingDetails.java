package com.revature.revshop.model;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_details")
public class TrackingDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tracking_id")
    private Integer trackingId;
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;
    @Column(length = 100)
    private String status;
    @Column(columnDefinition = "TEXT")
    private String description;
    @CreationTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    public TrackingDetails() {
    }
    public TrackingDetails(Integer trackingId, Orders order, String status, String description,
            LocalDateTime updatedAt)
    {
        this.trackingId = trackingId;
        this.order = order;
        this.status = status;
        this.description = description;
        this.updatedAt = updatedAt;
    }
    public Integer getTrackingId(){
        return trackingId;
    }
    public void setTrackingId(Integer trackingId){
        this.trackingId = trackingId;
    }
    public Orders getOrder(){
        return order;
    }
    public void setOrder(Orders order){
        this.order = order;
    }
    public String getStatus(){
        return status;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public LocalDateTime getUpdatedAt(){
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
    }
}
