package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g_wuy.swp391.voltera.entity.Battery;

import java.util.Optional;

@Repository
public interface BatteryRepository extends JpaRepository<Battery, Integer> {

    Optional<Battery> findByPost(Post post);

    @Query("SELECT COUNT(b) > 0 FROM Battery b WHERE b.serialNumber = :serialNumber")
    boolean isSerialNumberExist(@Param("serialNumber") String serialNumber);
}
