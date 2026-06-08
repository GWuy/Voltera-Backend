package com.g_wuy.swp391.voltera.mapper;

import com.g_wuy.swp391.voltera.entity.Vehicle;
import com.g_wuy.swp391.voltera.model.dto.VehicleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VehicleMapper {

    VehicleDTO toVehicleDTO(Vehicle vehicle);
}