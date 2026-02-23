package com.revature.revshop.service;

import com.revature.revshop.dto.FavoriteDTO;
import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.exception.ProductNotFoundException;
import com.revature.revshop.exception.UserNotFoundException;
import com.revature.revshop.model.Buyer;
import com.revature.revshop.model.Favorite;
import com.revature.revshop.model.Product;
import com.revature.revshop.repository.BuyerRepository;
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
    private final BuyerRepository buyerRepository;
    private final NotificationService notificationService;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           ProductRepository productRepository,
                           BuyerRepository buyerRepository,
                           NotificationService notificationService) {

        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
        this.buyerRepository = buyerRepository;
        this.notificationService = notificationService;
    }

    public void addToFavorite(Long buyerId, Long productId) {

        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new UserNotFoundException("Buyer not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (favoriteRepository.findByBuyerAndProduct(buyer, product).isPresent()) {
            throw new InvalidInputException("Product already in favorites");
        }

        Favorite favorite = new Favorite();
        favorite.setBuyer(buyer);
        favorite.setProduct(product);

        favoriteRepository.save(favorite);

        notificationService.createNotification(
                buyer.getUser().getUserId(),
                "Added to Favorites",
                product.getName() + " has been added to your favorites."
        );
    }

    public void removeFromFavorite(Long buyerId, Long productId) {

        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new UserNotFoundException("Buyer not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        favoriteRepository.deleteByBuyerAndProduct(buyer, product);
    }

    public List<FavoriteDTO> getBuyerFavorites(Long buyerId) {

        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new UserNotFoundException("Buyer not found"));

        return favoriteRepository.findByBuyer(buyer)
                .stream()
                .map(fav -> new FavoriteDTO(
                        fav.getProduct().getProductId(),
                        fav.getProduct().getName()
                ))
                .collect(Collectors.toList());
    }
}