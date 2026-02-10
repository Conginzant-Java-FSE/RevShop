package com.revature.revshop.repository;

import com.revature.revshop.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import com.revature.revshop.model.User;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUser(User user);
}
