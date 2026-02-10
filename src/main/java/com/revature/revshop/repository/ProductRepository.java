package com.revature.revshop.repository;

import com.revature.revshop.model.Product;
import com.revature.revshop.model.Category;
import com.revature.revshop.model.Seller;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByIsActiveTrue();

    List<Product> findByCategory(Category category);

    List<Product> findBySeller(Seller seller);

    List<Product> findByNameContainingIgnoreCase(String keyword);

    List<Product> findByStockQuantityLessThanEqual(Integer thresholdQuantity);

}
