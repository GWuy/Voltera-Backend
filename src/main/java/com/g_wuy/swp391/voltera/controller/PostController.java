package com.g_wuy.swp391.voltera.controller;


import com.g_wuy.swp391.voltera.entity.Post;
import com.g_wuy.swp391.voltera.model.response.ModerationResponse;
import com.g_wuy.swp391.voltera.model.response.PostResponse;
import com.g_wuy.swp391.voltera.model.response.RejectResponse;
import jakarta.validation.Valid;

import com.g_wuy.swp391.voltera.model.request.PostRequest;
import com.g_wuy.swp391.voltera.model.request.RejectPostRequest;
import com.g_wuy.swp391.voltera.service.JwtService;
import com.g_wuy.swp391.voltera.service.PostService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {

    PostService postService;
    JwtService jwtService;


    @PostMapping("/create")
    public ResponseEntity<PostResponse> createPost(
            @Valid
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PostRequest dto) {

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        PostResponse response = postService.createPost(dto, username);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/list/pending")
    public ResponseEntity<List<PostResponse>> getPendingPostsWithPaidFee() {
        return ResponseEntity.ok(postService.getPendingPostsWithPaidFee());
    }

    @GetMapping("/list/{status}")
    public List<PostResponse> getAllPost(@PathVariable("status") String status) {
        return postService.getAllPost(status);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{postId}/approve")
    public ModerationResponse approvePost(@PathVariable Integer postId) {
        return postService.approvePost(postId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{postId}/reject")
    public RejectResponse rejectPost(@PathVariable Integer postId,
                                     @RequestBody RejectPostRequest request) {
        return postService.rejectPost(postId, request);
    }


    @GetMapping("/filter/vehicles")
    public ResponseEntity<List<PostResponse>> filterVehicles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) Integer minOdo,
            @RequestParam(required = false) Integer maxOdo,
            @RequestParam(required = false) Integer minRange,
            @RequestParam(required = false) Integer maxRange,
            @RequestParam(required = false) Boolean bodyInsurance,
            @RequestParam(required = false) Boolean vehicleInspection,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minYearManufacture,
            @RequestParam(required = false) Integer maxYearManufacture,
            @RequestParam(required = false) Integer numberOfSeat
    ) {
        List<PostResponse> result = postService.filterVehicles(
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
        return ResponseEntity.ok(result);
    }

    @GetMapping("/filter/batteries")
    public ResponseEntity<List<PostResponse>> filterBatteries(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String batteryType,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) BigDecimal minOriginCapacity,
            @RequestParam(required = false) BigDecimal maxOriginCapacity,
            @RequestParam(required = false) BigDecimal minRemainingCapacity,
            @RequestParam(required = false) BigDecimal maxRemainingCapacity,
            @RequestParam(required = false) Integer minMileageCovered,
            @RequestParam(required = false) Integer maxMileageCovered,
            @RequestParam(required = false) BigDecimal minVoltage,
            @RequestParam(required = false) BigDecimal maxVoltage,
            @RequestParam(required = false) Integer minCycleCount,
            @RequestParam(required = false) Integer maxCycleCount,
            @RequestParam(required = false) String warranty,
            @RequestParam(required = false) BigDecimal minWeight,
            @RequestParam(required = false) BigDecimal maxWeight,
            @RequestParam(required = false) String lifeCycle,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        List<PostResponse> result = postService.filterBatteries(
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

        return ResponseEntity.ok(result);
    }

    @GetMapping("/public/vehicles")
    public ResponseEntity<List<PostResponse>> getAllVehiclePosts() {
        List<PostResponse> responses = postService.getAllVehiclePosts();
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/public/batteries")
    public ResponseEntity<List<PostResponse>> getAllBatteryPosts() {
        List<PostResponse> responses = postService.getAllBatteryPosts();
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/detail/{postId}")
    public ResponseEntity<PostResponse> getPostDetail(@PathVariable Integer postId) {
        PostResponse response = postService.getPostDetail(postId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponse>> getUserPosts(@PathVariable Integer userId) {
        List<PostResponse> responses = postService.getPostsByUserId(userId);
        return ResponseEntity.ok(responses);
    }
}
