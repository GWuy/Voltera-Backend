package com.g_wuy.swp391.voltera.mapper;

import com.g_wuy.swp391.voltera.entity.Complaint;
import com.g_wuy.swp391.voltera.entity.ComplaintReply;
import com.g_wuy.swp391.voltera.model.response.ComplaintReplyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComplaintReplyMapper {
    @Mapping(source = "complaint.id", target = "id")
    @Mapping(source = "complaint.problem", target = "problem")
    @Mapping(source = "reply.message", target = "solution")
    @Mapping(source = "reply.resolveAt", target = "resolveAt")
    ComplaintReplyResponse toComplaintReplyResponse(Complaint complaint, ComplaintReply reply);
}
