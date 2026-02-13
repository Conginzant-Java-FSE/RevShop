package com.revature.revshop.repository;

import com.revature.revshop.model.Favorite;
import com.revature.revshop.model.Product;
import com.revature.revshop.model.Buyer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {


    List<Favorite> findByBuyer(Buyer buyer);

    Optional<Favorite> findByBuyerAndProduct(Buyer buyer, Product product);

    void deleteByBuyerAndProduct(Buyer buyer, Product product);

}
