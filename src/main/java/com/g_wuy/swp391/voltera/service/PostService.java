package com.g_wuy.swp391.voltera.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.g_wuy.swp391.voltera.entity.*;
import com.g_wuy.swp391.voltera.exception.BusinessException;
import com.g_wuy.swp391.voltera.mapper.PostMapper;
import com.g_wuy.swp391.voltera.mapper.BatteryMapper;
import com.g_wuy.swp391.voltera.mapper.VehicleMapper;
import com.g_wuy.swp391.voltera.model.dto.BatteryDTO;
import com.g_wuy.swp391.voltera.model.dto.VehicleDTO;
import com.g_wuy.swp391.voltera.model.request.PostRequest;
import com.g_wuy.swp391.voltera.model.request.RejectPostRequest;
import com.g_wuy.swp391.voltera.model.response.ModerationResponse;
import com.g_wuy.swp391.voltera.model.response.PostResponse;
import com.g_wuy.swp391.voltera.model.response.RejectResponse;
import com.g_wuy.swp391.voltera.repository.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PostService {

    PostRepository postRepository;

    BatteryRepository batteryRepository;

    BatteryTypeRepository batteryTypeRepository;

    AccountRepository accountRepository;

    VehicleRepository vehicleRepository;

    PostMapper postMapper;

    BatteryMapper batteryMapper;

    VehicleMapper vehicleMapper;

    VehicleImageRepository vehicleImageRepository;

    BatteryImageRepository batteryImageRepository;

    TransactionRepository transactionRepository;

    NotificationService notificationService;

    FeeRepository feeRepository;

    @Transactional
    public PostResponse createPost(PostRequest dto, String username) {

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Account not found"));

        if (!"SELLER".equalsIgnoreCase(account.getRole())) {
            throw new SecurityException("Only sellers can create posts");
        }

        User seller = Optional.ofNullable(account.getUser())
                .orElseThrow(() -> new SecurityException("Seller information not found"));


        Post post = Post.builder()
                .sellerId(seller)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .status("PENDING")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        postRepository.save(post);
        postRepository.flush();

        // Kiểm tra chỉ được chọn 1 trong 2: vehicle hoặc battery
        if (dto.getVehicle() != null && dto.getBattery() != null) {
            throw new IllegalArgumentException("Choose either a vehicle or a battery, not both.");
        }
        if (dto.getVehicle() == null && dto.getBattery() == null) {
            throw new IllegalArgumentException("Provide either vehicle or battery details.");
        }

        Battery savedBattery = null;
        Vehicle savedVehicle = null;
        List<String> allImages = new ArrayList<>();

        // ================= VEHICLE =================
        if (dto.getVehicle() != null) {
            if (vehicleRepository.isLicensePlateExist(dto.getVehicle().getLicensePlate())) {
                throw new BusinessException("This License Plate already exists");
            }

            savedVehicle = vehicleRepository.save(Vehicle.builder()
                    .post(post)
                    .brand(dto.getVehicle().getBrand())
                    .model(dto.getVehicle().getModel())
                    .version(dto.getVehicle().getVersion())
                    .odo(dto.getVehicle().getOdo())
                    .batteryCapacity(dto.getVehicle().getBatteryCapacity())
                    .range(dto.getVehicle().getRange())
                    .chargingTime(dto.getVehicle().getChargingTime())
                    .color(dto.getVehicle().getColor())
                    .numberOfSeat(dto.getVehicle().getNumberOfSeat())
                    .style(dto.getVehicle().getStyle())
                    .bodyInsurance(Boolean.TRUE.equals(dto.getVehicle().getBodyInsurance()))
                    .vehicleInspection(Boolean.TRUE.equals(dto.getVehicle().getVehicleInspection()))
                    .licensePlate(dto.getVehicle().getLicensePlate())
                    .origin(dto.getVehicle().getOrigin())
                    .status("AVAILABLE")
                    .yearManufacture(dto.getVehicle().getYearManufacture())
                    .build());

            //Lưu ảnh xe (nếu có)
            if (dto.getVehicleImages() != null && !dto.getVehicleImages().isEmpty()) {
                for (String url : dto.getVehicleImages()) {
                    vehicleImageRepository.save(VehicleImage.builder()
                            .vehicle(savedVehicle)
                            .imageUrl(url)
                            .uploadedAt(Instant.now())
                            .build());
                }
                allImages.addAll(dto.getVehicleImages());
            }
        }

        // ================= BATTERY =================
        else if (dto.getBattery() != null) {
            Integer typeId = dto.getBattery().getBatteryTypeId().getId();
            if (typeId == null) throw new IllegalArgumentException("Battery type ID is required");

            BatteryType type = batteryTypeRepository.findById(typeId)
                    .orElseThrow(() -> new IllegalArgumentException("Battery type not found: " + typeId));

            if (batteryRepository.isSerialNumberExist(dto.getBattery().getSerialNumber())) {
                throw new BusinessException("This serial number already exists");
            }

            savedBattery = batteryRepository.save(Battery.builder()
                    .post(post)
                    .batteryTypeId(type)
                    .serialNumber(dto.getBattery().getSerialNumber())
                    .originCapacity(dto.getBattery().getOriginCapacity())
                    .remainingCapacity(dto.getBattery().getRemainingCapacity())
                    .mileageCovered(dto.getBattery().getMileageCovered())
                    .voltage(dto.getBattery().getVoltage())
                    .cycleCount(dto.getBattery().getCycleCount())
                    .warranty(dto.getBattery().getWarranty())
                    .weight(dto.getBattery().getWeight())
                    .lifecycle(dto.getBattery().getLifecycle())
                    .status("AVAILABLE")
                    .build());


            if (dto.getBatteryImages() != null && !dto.getBatteryImages().isEmpty()) {
                for (String url : dto.getBatteryImages()) {
                    batteryImageRepository.save(BatteryImage.builder()
                            .battery(savedBattery)
                            .imageUrl(url)
                            .uploadedAt(Instant.now())
                            .build());
                }
                allImages.addAll(dto.getBatteryImages());
            }
        }


        BigDecimal price = BigDecimal.valueOf(0.0);
        if (dto.getBattery() != null) {
            price = BigDecimal.valueOf(200000);
        } else if (dto.getVehicle() != null) {
            price = BigDecimal.valueOf(500000);
        }

        Transaction transaction = Transaction.builder()
                .post(post)
                .createAt(Instant.now())
                .updateAt(Instant.now())
                .price(price)
                .contractid(null)
                .reportid(null)
                .transactionStatus("PENDING")
                .buyerid(seller)
                .build();
        transactionRepository.save(transaction);

        // Automatically create Fee record for immediate tracking in Fee Management
        Fee fee = Fee.builder()
                .post(post)
                .transaction(transaction)
                .amount(price)
                .description("Posting fee for: " + post.getTitle())
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(15)) // 15 days to pay
                .feeStatus("PENDING")
                .build();
        feeRepository.save(fee);


        PostResponse response = postMapper.toPostResponse(post, savedBattery, savedVehicle, allImages);
        response.setLocation(seller.getAddress());
        return response;
    }

    public ModerationResponse approvePost(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setStatus("APPROVE");
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);
        notificationService.sendForEvent(post);

        return new ModerationResponse(post.getId(), post.getStatus(), null);
    }

    public RejectResponse rejectPost(Integer postId, RejectPostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setStatus("REJECT");
        post.setUpdatedAt(Instant.now());


        List<Fee> fees = feeRepository.findFeesByPostIdOrderByCreatedAtDesc(postId);
        for (Fee fee : fees) {
            fee.setFeeStatus("CANCELLED");
            feeRepository.save(fee);
        }

        postRepository.save(post);
        notificationService.sendForEvent(post);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminUsername = auth != null ? auth.getName() : "Unknown";

        return postMapper.toRejectResponse(post, adminUsername, request.getReason());
    }

    public List<PostResponse> filterVehicles(
            String keyword,
            String address,
            String brand,
            String version,
            String color,
            String origin,
            String style,
            Integer minOdo,
            Integer maxOdo,
            Integer minRange,
            Integer maxRange,
            Boolean bodyInsurance,
            Boolean vehicleInspection,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minYearManufacture,
            Integer maxYearManufacture,
            Integer numberOfSeat) {

        List<Post> posts = postRepository.filterVehicles(
                keyword,
                address,
                brand,
                version,
                color,
                origin,
                style,
                minOdo,
                maxOdo,
                minRange,
                maxRange,
                bodyInsurance,
                vehicleInspection,
                minPrice,
                maxPrice,
                minYearManufacture,
                maxYearManufacture,
                numberOfSeat
        );

        return posts.stream().map(post -> {
            Battery battery = batteryRepository.findByPost(post).orElse(null);
            Vehicle vehicle = vehicleRepository.findByPost(post).orElse(null);

            List<String> allImages = new ArrayList<>();
            if (vehicle != null) {
                List<String> vImages = vehicleImageRepository.findByVehicle(vehicle)
                        .stream().map(VehicleImage::getImageUrl).collect(Collectors.toList());
                allImages.addAll(vImages);
            }

            PostResponse response = postMapper.toPostResponse(post, battery, vehicle, allImages);
            response.setLocation(post.getSellerId().getAddress());
            return response;
        }).collect(Collectors.toList());
    }


    public List<PostResponse> filterBatteries(
            String keyword,
            String address,
            String batteryType,
            String serialNumber,
            BigDecimal minOriginCapacity,
            BigDecimal maxOriginCapacity,
            BigDecimal minRemainingCapacity,
            BigDecimal maxRemainingCapacity,
            Integer minMileageCovered,
            Integer maxMileageCovered,
            BigDecimal minVoltage,
            BigDecimal maxVoltage,
            Integer minCycleCount,
            Integer maxCycleCount,
            String warranty,
            BigDecimal minWeight,
            BigDecimal maxWeight,
            String lifeCycle,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        List<Post> posts = postRepository.filterBatteries(
                keyword,
                address,
                batteryType,
                serialNumber,
                minOriginCapacity,
                maxOriginCapacity,
                minRemainingCapacity,
                maxRemainingCapacity,
                minMileageCovered,
                maxMileageCovered,
                minVoltage,
                maxVoltage,
                minCycleCount,
                maxCycleCount,
                warranty,
                minWeight,
                maxWeight,
                lifeCycle,
                minPrice,
                maxPrice
        );

        return posts.stream().map(post -> {
            Battery battery = batteryRepository.findByPost(post).orElse(null);
            Vehicle vehicle = vehicleRepository.findByPost(post).orElse(null);

            List<String> allImages = new ArrayList<>();
            if (battery != null) {
                List<String> bImages = batteryImageRepository.findByBattery(battery)
                        .stream().map(BatteryImage::getImageUrl).collect(Collectors.toList());
                allImages.addAll(bImages);
            }

            PostResponse response = postMapper.toPostResponse(post, battery, vehicle, allImages);
            response.setLocation(post.getSellerId().getAddress());
            return response;
        }).collect(Collectors.toList());
    }

    public List<PostResponse> getAllPost(String status) {
        List<Post> posts = postRepository.getAllPostByStatus(status);
        List<PostResponse> responses = new ArrayList<>();

        for (Post post : posts) {
            List<String> allImages = new ArrayList<>();
            Vehicle vehicle = vehicleRepository.findByPost(post).orElse(null);
            Battery battery = batteryRepository.findByPost(post).orElse(null);

            if (vehicle != null) {
                List<String> vImages = vehicleImageRepository.findByVehicle(vehicle)
                        .stream().map(VehicleImage::getImageUrl).toList();
                allImages.addAll(vImages);
            }

            if (battery != null) {
                List<String> bImages = batteryImageRepository.findByBattery(battery)
                        .stream().map(BatteryImage::getImageUrl).toList();
                allImages.addAll(bImages);
            }

            PostResponse response = postMapper.toPostResponse(post, battery, vehicle, allImages);
            // Fix lazy loading issue by safely getting address
            try {
                if (post.getSellerId() != null) {
                    response.setLocation(post.getSellerId().getAddress());
                }
            } catch (Exception e) {
                response.setLocation("Unknown location");
            }

            responses.add(response);
        }

        return responses;
    }

    public List<PostResponse> getPendingPostsWithPaidFee() {

        List<Post> posts = postRepository.getAllPostByStatus("PENDING");

        // Filter to only posts with paid fees
        posts = posts.stream()
                .filter(post -> post.getFees() != null &&
                        !post.getFees().isEmpty() &&
                        post.getFees().stream().anyMatch(fee -> "PAID".equals(fee.getFeeStatus())))
                .collect(Collectors.toList());

        return posts.stream().map(post -> {
            // Convert entities to DTOs to avoid lazy loading issues
            BatteryDTO batteryDTO = null;
            VehicleDTO vehicleDTO = null;

            if (post.getBattery() != null) {
                batteryDTO = batteryMapper.toBatteryDTO(post.getBattery());
            }

            if (post.getVehicle() != null) {
                vehicleDTO = vehicleMapper.toVehicleDTO(post.getVehicle());
            }

            List<String> imageUrls = List.of();

            PostResponse response = postMapper.toPostResponse(post, post.getBattery(), post.getVehicle(), imageUrls);

            // Set DTO instead of entity to avoid serialization issues
            response.setBattery(batteryDTO);
            response.setVehicle(vehicleDTO);

            // Add fee status to response
            if (post.getFees() != null && !post.getFees().isEmpty()) {
                // Get the most recent fee
                Fee mostRecentFee = post.getFees().stream()
                        .sorted((f1, f2) -> f2.getCreatedAt().compareTo(f1.getCreatedAt()))
                        .findFirst()
                        .orElse(null);

                if (mostRecentFee != null) {
                    response.setFeeStatus(mostRecentFee.getFeeStatus());
                } else {
                    response.setFeeStatus("NO_FEE");
                }
            } else {
                response.setFeeStatus("NO_FEE");
            }

            return response;

        }).toList();
    }

    public List<PostResponse> getAllVehiclePosts() {
        List<Post> posts = postRepository.findAllVehiclePosts();
        List<PostResponse> responses = new ArrayList<>();

        for (Post post : posts) {
            Vehicle vehicle = vehicleRepository.findByPost(post).orElse(null);
            List<String> imageUrls = new ArrayList<>();

            if (vehicle != null) {
                imageUrls = vehicleImageRepository.findByVehicle(vehicle)
                        .stream().map(VehicleImage::getImageUrl).toList();
            }

            PostResponse response = postMapper.toPostResponse(post, null, vehicle, imageUrls);
            response.setLocation(post.getSellerId().getAddress());
            responses.add(response);
        }
        return responses;
    }


    public List<PostResponse> getAllBatteryPosts() {
        List<Post> posts = postRepository.findAllBatteryPosts();
        List<PostResponse> responses = new ArrayList<>();

        for (Post post : posts) {
            Battery battery = batteryRepository.findByPost(post).orElse(null);
            List<String> imageUrls = new ArrayList<>();

            if (battery != null) {
                imageUrls = batteryImageRepository.findByBattery(battery)
                        .stream().map(BatteryImage::getImageUrl).toList();
            }

            PostResponse response = postMapper.toPostResponse(post, battery, null, imageUrls);
            response.setLocation(post.getSellerId().getAddress());
            responses.add(response);
        }
        return responses;
    }

    public PostResponse getPostDetail(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Battery battery = batteryRepository.findByPost(post).orElse(null);
        Vehicle vehicle = vehicleRepository.findByPost(post).orElse(null);

        List<String> imageUrls = new ArrayList<>();

        if (vehicle != null) {
            imageUrls = vehicleImageRepository.findByVehicle(vehicle)
                    .stream().map(VehicleImage::getImageUrl).toList();
        } else if (battery != null) {
            imageUrls = batteryImageRepository.findByBattery(battery)
                    .stream().map(BatteryImage::getImageUrl).toList();
        }

        PostResponse response = postMapper.toPostResponse(post, battery, vehicle, imageUrls);
        response.setLocation(post.getSellerId().getAddress());

        return response;
    }

    public List<PostResponse> getPostsByUserId(Integer userId) {
        List<Post> posts = postRepository.findBySellerIdIdOrderByCreatedAtDesc(userId);
        List<PostResponse> responses = new ArrayList<>();

        for (Post post : posts) {
            Vehicle vehicle = vehicleRepository.findByPost(post).orElse(null);
            Battery battery = batteryRepository.findByPost(post).orElse(null);
            List<String> imageUrls = new ArrayList<>();

            if (vehicle != null) {
                imageUrls = vehicleImageRepository.findByVehicle(vehicle)
                        .stream().map(VehicleImage::getImageUrl).toList();
            } else if (battery != null) {
                imageUrls = batteryImageRepository.findByBattery(battery)
                        .stream().map(BatteryImage::getImageUrl).toList();
            }

            PostResponse response = postMapper.toPostResponse(post, battery, vehicle, imageUrls);
            response.setLocation(post.getSellerId().getAddress());
            responses.add(response);
        }
        return responses;
    }


}