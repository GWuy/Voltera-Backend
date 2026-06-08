package com.g_wuy.swp391.voltera.mapper;

import java.util.List;

import com.g_wuy.swp391.voltera.model.response.FavListResponse;
import org.mapstruct.Mapper;

import com.g_wuy.swp391.voltera.entity.FavoriteList;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FavListMapper {
    @Mapping(target = "userId", source = "userid.id")          // Lấy id từ User entity
    @Mapping(target = "postId", source = "postid.id")          // Lấy id từ Post entity
    @Mapping(target = "postTitle", source = "postid.title")    // Lấy title từ Post
    @Mapping(target = "price", source = "postid.price")        // Lấy price từ Post
    @Mapping(target = "thumbnailUrl", ignore = true)
        // Sẽ set thủ công sau
    FavListResponse toDto(FavoriteList entity);

    List<FavListResponse> toDtoList(List<FavoriteList> entities);
}