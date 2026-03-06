package com.revature.revshop.service;

import com.revature.revshop.dto.FavoriteDTO;
import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.exception.ProductNotFoundException;
import com.revature.revshop.exception.UserNotFoundException;
import com.revature.revshop.model.Buyer;
import com.revature.revshop.model.Favorite;
import com.revature.revshop.model.Product;
import com.revature.revshop.model.Seller;
import com.revature.revshop.repository.BuyerRepository;
import com.revature.revshop.repository.FavoriteRepository;
import com.revature.revshop.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FavoriteService {

        private static final Logger log = LoggerFactory.getLogger(FavoriteService.class);
        private static final String BUYER_NOT_FOUND = "Buyer not found";
        private static final String PRODUCT_NOT_FOUND = "Product not found";

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
                log.info("Adding favorite buyerId={} productId={}", buyerId, productId);

                Buyer buyer = buyerRepository.findById(buyerId)
                                .orElseThrow(() -> new UserNotFoundException(BUYER_NOT_FOUND));

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ProductNotFoundException(PRODUCT_NOT_FOUND));

                if (favoriteRepository.findByBuyerAndProduct(buyer, product).isPresent()) {
                        throw new InvalidInputException("Product already in favorites");
                }

                Favorite favorite = new Favorite();
                favorite.setBuyer(buyer);
                favorite.setProduct(product);

                favoriteRepository.save(favorite);

                Seller seller = product.getSeller();

                notificationService.createNotification(
                                seller.getUser().getUserId(),
                                "Product Favorited",
                                buyer.getUser().getName() +
                                                " added your product '" +
                                                product.getName() +
                                                "' to favorites.");
        }

        public void removeFromFavorite(Long buyerId, Long productId) {
                log.info("Removing favorite buyerId={} productId={}", buyerId, productId);

                Buyer buyer = buyerRepository.findById(buyerId)
                                .orElseThrow(() -> new UserNotFoundException(BUYER_NOT_FOUND));

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ProductNotFoundException(PRODUCT_NOT_FOUND));

                favoriteRepository.deleteByBuyerAndProduct(buyer, product);
        }

        public List<FavoriteDTO> getBuyerFavorites(Long buyerId) {

                Buyer buyer = buyerRepository.findById(buyerId)
                                .orElseThrow(() -> new UserNotFoundException(BUYER_NOT_FOUND));

                return favoriteRepository.findByBuyer(buyer)
                                .stream()
                                .map(fav -> new FavoriteDTO(
                                                fav.getProduct().getProductId(),
                                                fav.getProduct().getName()))
                                .toList();
        }
}
