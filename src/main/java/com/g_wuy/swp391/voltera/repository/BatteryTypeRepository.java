package com.g_wuy.swp391.voltera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.g_wuy.swp391.voltera.entity.BatteryType;

@Repository
public interface BatteryTypeRepository extends JpaRepository<BatteryType, Integer> {
}