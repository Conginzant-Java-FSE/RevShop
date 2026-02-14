package com.revature.revshop.controller;

import com.revature.revshop.dto.FavoriteDTO;
import com.revature.revshop.service.FavoriteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{buyerId}/{productId}")
    public void add(@PathVariable Long buyerId,
                    @PathVariable Long productId) {
        favoriteService.addToFavorite(buyerId, productId);
    }

    @DeleteMapping("/{buyerId}/{productId}")
    public void remove(@PathVariable Long buyerId,
                       @PathVariable Long productId) {
        favoriteService.removeFromFavorite(buyerId, productId);
    }

    @GetMapping("/{buyerId}")
    public List<FavoriteDTO> getFavorites(@PathVariable Long buyerId) {
        return favoriteService.getBuyerFavorites(buyerId);
    }
}
