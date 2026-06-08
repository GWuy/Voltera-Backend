package com.g_wuy.swp391.voltera.mapper;

import com.g_wuy.swp391.voltera.entity.Complaint;
import com.g_wuy.swp391.voltera.model.request.ComplaintRequest;
import com.g_wuy.swp391.voltera.model.response.ComplaintResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComplaintMapper {

    @Mapping(target = "problem", source = "problem")
    @Mapping(target = "description", source = "description")
    Complaint toComplaint(ComplaintRequest request);

    @Mapping(source = "id", target = "complaintId")
    @Mapping(source = "senderId.fullname", target = "senderFullName")
    ComplaintResponse toComplaintResponse(Complaint complaint);
}