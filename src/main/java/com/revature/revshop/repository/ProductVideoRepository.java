package com.revature.revshop.repository;

import com.revature.revshop.model.Product;
import com.revature.revshop.model.ProductVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVideoRepository extends JpaRepository<ProductVideo, Long> {
    List<ProductVideo> findByProduct(Product product);
    List<ProductVideo> findByProductProductId(Long productId);
}
