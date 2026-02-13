package com.revature.revshop.service;

import com.revature.revshop.dto.FavoriteDTO;
import com.revature.revshop.model.*;
import com.revature.revshop.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final BuyerRepository buyerRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           ProductRepository productRepository,
                           BuyerRepository buyerRepository) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
        this.buyerRepository = buyerRepository;
    }

    public void addToFavorite(Long buyerId, Integer productId) {

        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

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

    public void removeFromFavorite(Long buyerId, Integer productId) {

        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        favoriteRepository.deleteByBuyerAndProduct(buyer, product);
    }

    public List<FavoriteDTO> getBuyerFavorites(Long buyerId) {

        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        return favoriteRepository.findByBuyer(buyer)
                .stream()
                .map(fav -> new FavoriteDTO(
                        fav.getProduct().getProductId(),
                        fav.getProduct().getName()
                ))
                .collect(Collectors.toList());
    }
}
