package com.revature.revshop.service;

import com.revature.revshop.model.Buyer;
import com.revature.revshop.model.Favorite;
import com.revature.revshop.model.Product;
import com.revature.revshop.repository.FavoriteRepository;
import com.revature.revshop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           ProductRepository productRepository) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
    }

    public void addToFavorite(Buyer buyer, Integer productId) {

        if (buyer == null) {
            throw new RuntimeException("Buyer cannot be null");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (favoriteRepository.findByBuyerAndProduct(buyer, product).isPresent()) {
            throw new RuntimeException("Product already in favorites");
        }

        Favorite favorite = new Favorite();
        favorite.setBuyer(buyer);
        favorite.setProduct(product);

        favoriteRepository.save(favorite);
    }

    public void removeFromFavorite(Buyer buyer, Integer productId) {

        if (buyer == null) {
            throw new RuntimeException("Buyer cannot be null");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        favoriteRepository.deleteByBuyerAndProduct(buyer, product);
    }

    public List<Product> getBuyerFavorites(Buyer buyer) {

        if (buyer == null) {
            throw new RuntimeException("Buyer cannot be null");
        }

        return favoriteRepository.findByBuyer(buyer)
                .stream()
                .map(Favorite::getProduct)
                .collect(Collectors.toList());
    }
}
