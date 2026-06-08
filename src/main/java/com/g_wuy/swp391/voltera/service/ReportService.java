package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.Report;
import com.g_wuy.swp391.voltera.mapper.ReportMapper;
import com.g_wuy.swp391.voltera.model.response.ReportResponse;
import com.g_wuy.swp391.voltera.repository.ReportRepository;
import com.g_wuy.swp391.voltera.repository.TransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportService {

    ReportRepository reportRepository;

    TransactionRepository transactionRepository;

    ReportMapper mapper;

    public ReportResponse createOrUpdateReport(int month, int year) {
        ZonedDateTime startOfMonth = YearMonth.of(year, month).atDay(1).atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        BigDecimal revenue = transactionRepository.getTotalRevenueInPeriod(startOfMonth.toInstant(), endOfMonth.toInstant());
        Long totalTransactions = transactionRepository.getTotalTransactionsInPeriod(startOfMonth.toInstant(), endOfMonth.toInstant());

        // Tạo mới nếu chưa có báo cáo, dùng Builder
        Report report = reportRepository.findByMonthAndYear(month, year)
                .orElseGet(() -> Report.builder()
                        .month(month)
                        .year(year)
                        .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
                        .totalTransactions(totalTransactions != null ? totalTransactions : 0L)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()
                );

        report.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);
        report.setTotalTransactions(totalTransactions != null ? totalTransactions : 0L);
        report.setUpdatedAt(Instant.now());

        return mapper.toResponse(reportRepository.save(report));
    }

    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public ReportResponse getByMonthYear(int month, int year) {
        return reportRepository.findByMonthAndYear(month, year)
                .map(mapper::toResponse)
                .orElse(null);
    }


    @Scheduled(cron = "0 0 0 1 * *")
    public void autoGenerateMonthlyReport() {
        LocalDate now = LocalDate.now().minusMonths(1); // báo cáo tháng trước
        createOrUpdateReport(now.getMonthValue(), now.getYear());
    }
}
