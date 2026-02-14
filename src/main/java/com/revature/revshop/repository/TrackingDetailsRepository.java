package com.revature.revshop.repository;

import com.revature.revshop.model.TrackingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingDetailsRepository extends JpaRepository<TrackingDetails, Integer> {

    List<TrackingDetails> findByOrder_OrderId(Long orderId);

    List<TrackingDetails> findByStatus(String status);
}
