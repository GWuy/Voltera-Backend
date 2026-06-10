package com.g_wuy.swp391.voltera.controller;

import java.util.List;

import com.g_wuy.swp391.voltera.model.response.FavListResponse;

import com.g_wuy.swp391.voltera.entity.FavoriteList;
import com.g_wuy.swp391.voltera.entity.User;
import com.g_wuy.swp391.voltera.exception.BusinessException;
import com.g_wuy.swp391.voltera.service.FavoriteService;
import com.g_wuy.swp391.voltera.service.JwtService;
import com.g_wuy.swp391.voltera.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FavoriteListController {

    FavoriteService favoriteService;

    JwtService jwtService;

    UserService userService;

    @PostMapping("/add/{postID}")
    public ResponseEntity<Void> addToList(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("postID") Integer postId) {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User userAdd = null;
        if (username != null) {
            userAdd = userService.findUserByUsername(username);
        }
        FavoriteList fl = favoriteService.addToFavoriteList(userAdd, postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{postID}")
    public ResponseEntity<String> removeFromList(
            @PathVariable Integer postID,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userService.findUserByUsername(username);
        if (user == null) {
            throw new BusinessException("User Not found");
        }
        favoriteService.removeFromFavList(user.getId(), postID);
        return ResponseEntity.ok("Removed from favorite list successfully!");
    }

    @GetMapping
    public ResponseEntity<List<FavListResponse>> getFavorites(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userService.findUserByUsername(username);
        if (user == null) {
            throw new BusinessException("User Not found");
        }
        List<FavListResponse> favoriteLists = favoriteService.getFavoriteListsByUserId(user.getId());
        return ResponseEntity.ok(favoriteLists);
    }
}
