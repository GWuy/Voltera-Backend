package com.g_wuy.swp391.voltera.controller;

import com.g_wuy.swp391.voltera.model.response.ReportResponse;
import com.g_wuy.swp391.voltera.service.ReportService;

import java.time.LocalDate;
import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportController {

    ReportService reportService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ReportResponse generateReportManually() {
        LocalDate now = LocalDate.now();
        return reportService.createOrUpdateReport(now.getMonthValue(), now.getYear());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReportResponse> getAllReports() {
        return reportService.getAllReports();
    }

    @GetMapping("/{month}/{year}")
    @PreAuthorize("hasRole('ADMIN')")
    public ReportResponse getByMonthYear(@PathVariable Integer month, @PathVariable Integer year) {
        return reportService.getByMonthYear(month, year);
    }
}
