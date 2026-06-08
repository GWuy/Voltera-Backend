package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.Battery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.g_wuy.swp391.voltera.entity.BatteryImage;

import java.util.List;

@Repository
public interface BatteryImageRepository extends JpaRepository<BatteryImage, Integer> {
    List<BatteryImage> findByBattery(Battery battery);
}
