package com.revature.revshop.repository;

import com.revature.revshop.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    List<Seller> getSellersByBusinessNameIgnoreCase(String businessName);

    List<Seller> findSellerByBusinessName(String businessName);
}
