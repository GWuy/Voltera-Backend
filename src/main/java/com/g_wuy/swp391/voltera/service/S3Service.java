package com.g_wuy.swp391.voltera.service;

import ch.qos.logback.core.spi.ErrorCodes;
import com.g_wuy.swp391.voltera.exception.GlobalException;
import com.g_wuy.swp391.voltera.exception.GlobalExceptionHandler;
import com.g_wuy.swp391.voltera.model.enums.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class S3Service {

    final S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.public-base-url}")
    private String publicBaseUrl;

    public String uploadFile(
            MultipartFile file,
            String folderName
    ) {

        // 2. Validate file
        if (file == null || file.isEmpty()) {
            throw new GlobalException(
                    ErrorCode.INVALID_REQUEST,
                    "File is empty"
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.matches("image/(png|jpeg|webp)")) {
            throw new GlobalException(
                    ErrorCode.INVALID_REQUEST,
                    "Unsupported image type"
            );
        }

        // 4. Build object key
        String fileKey = folderName + "/"
                + UUID.randomUUID() + "-" + file.getOriginalFilename();

        try {
            // 5. Upload to R2
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileKey)
                            .contentType(contentType)
                            .acl(ObjectCannedACL.PUBLIC_READ)
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            return publicBaseUrl + "/" + fileKey;

        } catch (IOException e) {
            throw new GlobalException(
                    ErrorCode.INTERNAL_ERROR,
                    "Upload image failed"
            );
        }
    }
}

