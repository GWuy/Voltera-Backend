package com.g_wuy.swp391.voltera.mapper;

import com.g_wuy.swp391.voltera.entity.Report;
import com.g_wuy.swp391.voltera.model.response.ReportResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    ReportResponse toResponse(Report report);
}
