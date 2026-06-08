package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g_wuy.swp391.voltera.entity.Vehicle;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    Optional<Vehicle> findByPost(Post post);

    @Query("SELECT COUNT(v) > 0 FROM Vehicle v WHERE v.licensePlate = :licensePlate")
    boolean isLicensePlateExist(@Param("licensePlate") String licensePlate);
}
