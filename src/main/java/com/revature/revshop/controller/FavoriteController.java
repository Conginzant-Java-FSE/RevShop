package com.revature.revshop.controller;

import com.revature.revshop.dto.FavoriteDTO;
import com.revature.revshop.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{buyerId}/{productId}")
    public ResponseEntity<Void> add(@PathVariable Long buyerId,
                                    @PathVariable Long productId) {
        favoriteService.addToFavorite(buyerId, productId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{buyerId}/{productId}")
    public ResponseEntity<Void> remove(@PathVariable Long buyerId,
                                       @PathVariable Long productId) {
        favoriteService.removeFromFavorite(buyerId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{buyerId}")
    public ResponseEntity<List<FavoriteDTO>> getFavorites(@PathVariable Long buyerId) {
        return ResponseEntity.ok(favoriteService.getBuyerFavorites(buyerId));
    }
}