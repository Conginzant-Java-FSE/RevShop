package com.revature.revshop.repository;

import com.revature.revshop.model.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, Long> {
    Optional<Buyer> findByUserEmail(String userEmail);

    boolean existsByUserEmail(String email);
}
