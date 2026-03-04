package com.revature.revshop.repository;

import com.revature.revshop.model.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipperRepository extends JpaRepository<Shipper, Long> {
    List<Shipper> findByIsAvailable(Boolean isAvailable);
}
