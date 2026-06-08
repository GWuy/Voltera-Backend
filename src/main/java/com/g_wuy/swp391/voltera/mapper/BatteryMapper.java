package com.g_wuy.swp391.voltera.mapper;

import com.g_wuy.swp391.voltera.entity.Battery;
import com.g_wuy.swp391.voltera.entity.BatteryType;
import com.g_wuy.swp391.voltera.model.dto.BatteryDTO;
import com.g_wuy.swp391.voltera.model.dto.BatteryTypeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BatteryMapper {

    @Mapping(source = "batteryTypeId", target = "batteryTypeId")
    BatteryDTO toBatteryDTO(Battery battery);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "typename", target = "typeName")
    @Mapping(source = "technical", target = "technical")
    @Mapping(source = "description", target = "description")
    BatteryTypeDTO toBatteryTypeDTO(BatteryType batteryType);
}