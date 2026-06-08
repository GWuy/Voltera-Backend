package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.FavoriteList;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteListRepository extends JpaRepository<FavoriteList, Integer> {
    @Query("SELECT COUNT(f) > 0 FROM FavoriteList f WHERE f.userid.id = :userid AND f.postid.id = :postid")
    boolean existsByUseridAndPostid(@Param("userid") Integer userid, @Param("postid") Integer postid);

    @Query("SELECT f FROM FavoriteList f WHERE f.userid.id = :userid AND f.postid.id = :postid")
    FavoriteList findByUseridAndPostid(@Param("userid") Integer userid, @Param("postid") Integer postid);

    @Query("SELECT f FROM FavoriteList f WHERE f.userid.id = :userid")
    List<FavoriteList> findByUserid(@Param("userid") Integer userid);
}