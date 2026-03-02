package com.revature.revshop.repository;

import com.revature.revshop.model.Product;
import com.revature.revshop.model.Category;
import com.revature.revshop.model.Seller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByIsActiveTrue();

    List<Product> findByCategory(Category category);

    List<Product> findBySeller(Seller seller);

    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    List<Product> findByStockQuantityLessThanEqual(Integer thresholdQuantity);

}
