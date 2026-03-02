package com.revature.revshop.controller;

import com.revature.revshop.dto.ApiResponse;
import com.revature.revshop.dto.FavoriteDTO;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

        private static final Logger log = LoggerFactory.getLogger(FavoriteController.class);

        private final FavoriteService favoriteService;

        public FavoriteController(FavoriteService favoriteService) {
                this.favoriteService = favoriteService;
        }

        @PostMapping("/{buyerId}/{productId}")
        public ResponseEntity<ApiResponse<Void>> add(
                        @PathVariable Long buyerId,
                        @PathVariable Long productId) {

                log.info("POST /api/favorites/{}/{}", buyerId, productId);
                favoriteService.addToFavorite(buyerId, productId);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new ApiResponse<>(
                                                "Product added to favorites successfully",
                                                null));
        }

        @DeleteMapping("/{buyerId}/{productId}")
        public ResponseEntity<ApiResponse<Void>> remove(
                        @PathVariable Long buyerId,
                        @PathVariable Long productId) {

                log.info("DELETE /api/favorites/{}/{}", buyerId, productId);
                favoriteService.removeFromFavorite(buyerId, productId);

                return ResponseEntity.ok(
                                new ApiResponse<>(
                                                "Product removed from favorites successfully",
                                                null));
        }

        @GetMapping("/{buyerId}")
        public ResponseEntity<ApiResponse<List<FavoriteDTO>>> getFavorites(
                        @PathVariable Long buyerId) {

                log.info("GET /api/favorites/{}", buyerId);
                List<FavoriteDTO> favorites = favoriteService.getBuyerFavorites(buyerId);

                if (favorites == null) {
                        throw new ResourceNotFoundException("Favorites not found for this buyer");
                }

                return ResponseEntity.ok(
                                new ApiResponse<>(
                                                "Favorites fetched successfully",
                                                favorites));
        }
}