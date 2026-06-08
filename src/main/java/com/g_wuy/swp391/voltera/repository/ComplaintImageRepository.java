package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.ComplaintImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintImageRepository extends JpaRepository<ComplaintImage, Integer> {
}
