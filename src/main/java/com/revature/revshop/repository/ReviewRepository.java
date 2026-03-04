package com.revature.revshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.revature.revshop.model.Review;
import com.revature.revshop.model.Product;
import com.revature.revshop.model.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);

    List<Review> findByUser(User user);

    List<Review> findByRating(Integer rating);

    Optional<Review> findByProductAndUser(Product product, User user);
}
