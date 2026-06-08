package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {
    Optional<Report> findByMonthAndYear(Integer month, Integer year);

}
