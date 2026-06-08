package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.Complaint;
import com.g_wuy.swp391.voltera.entity.ComplaintImage;
import com.g_wuy.swp391.voltera.exception.BusinessException;
import com.g_wuy.swp391.voltera.repository.ComplaintImageRepository;
import com.g_wuy.swp391.voltera.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ComplaintImageService {

    ComplaintImageRepository complaintImageRepository;

    S3Service s3Service;

    ComplaintRepository complaintRepository;

    public List<String> uploadComplaintImages(Integer complaintId, MultipartFile[] files) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new BusinessException("Complaint not found"));

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = s3Service.uploadFile(file, "complaints/" + complaintId);
            ComplaintImage image = new ComplaintImage();
            image.setComplaintId(complaint);
            image.setImageUrl(url);
            image.setUploadedAt(Instant.now());
            complaintImageRepository.save(image);
            urls.add(url);
        }
        return urls;
    }

}
