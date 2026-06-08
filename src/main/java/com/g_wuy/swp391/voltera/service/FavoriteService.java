package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.FavoriteList;
import com.g_wuy.swp391.voltera.entity.Post;
import com.g_wuy.swp391.voltera.entity.User;
import com.g_wuy.swp391.voltera.exception.BusinessException;
import com.g_wuy.swp391.voltera.mapper.FavListMapper;
import com.g_wuy.swp391.voltera.model.response.FavListResponse;
import com.g_wuy.swp391.voltera.repository.FavoriteListRepository;
import com.g_wuy.swp391.voltera.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FavoriteService {

    FavoriteListRepository favoriteListRepository;

    PostRepository postRepository;

    FavListMapper favListMapper;

    public FavoriteList addToFavoriteList(User currentUser, Integer postID) {
        if (currentUser == null) {
            throw new BusinessException("User not found");
        }

        if (favoriteListRepository.existsByUseridAndPostid(currentUser.getId(), postID)) {
            throw new BusinessException("This post is already in the favorite list");
        }

        Post favPost = postRepository.findPostById(postID);
        if (favPost == null) {
            throw new BusinessException("Post not found");
        }

        FavoriteList favList = FavoriteList.builder()
                .userid(currentUser)
                .postid(favPost)
                .build();

        return favoriteListRepository.save(favList);
    }

    public void removeFromFavList(Integer userID, Integer postID) {
        FavoriteList favoriteList = favoriteListRepository.findByUseridAndPostid(userID, postID);
        if (favoriteList == null) {
            throw new BusinessException("Post not found in favorite list");
        }
        favoriteListRepository.delete(favoriteList);
    }

    public List<FavListResponse> getFavoriteListsByUserId(Integer userId) {
        if (userId == null) {
            throw new BusinessException("User not found");
        }

        List<FavoriteList> favorites = favoriteListRepository.findByUserid(userId);
        List<FavListResponse> responseList = favListMapper.toDtoList(favorites);

        responseList.forEach(fav -> {
            Integer postId = fav.getPostId();
            if (postId != null) {
                String thumb = postRepository.getThumbnailUrlByPostId(postId);
                fav.setThumbnailUrl(thumb);
            } else {
                fav.setThumbnailUrl(null);
            }
        });

        return responseList;
    }
}