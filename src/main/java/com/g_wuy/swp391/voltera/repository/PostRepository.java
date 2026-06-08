package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.Post;
import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer>, JpaSpecificationExecutor<Post> {
    List<Post> findByStatus(String status);

    List<Post> findBySellerIdIdOrderByCreatedAtDesc(Integer sellerId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.status = :status, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    int updateStatusById(@Param("id") Integer id, @Param("status") String status);

    @Query("SELECT p FROM Post p WHERE p.status = :status ORDER BY p.createdAt ASC")
    List<Post> getAllPostByStatus(@Param("status") String status);

    @Query("""
                SELECT DISTINCT p FROM Post p
                JOIN FETCH p.fees f
                WHERE p.status = 'PENDING'
                AND f.feeStatus = 'PAID'
            """)
    List<Post> findPendingPostsWithPaidFee();

    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Post findPostById(@Param("id") Integer id);

    @Query(value = """
                SELECT p.* FROM post p
                JOIN vehicle v ON p.postid = v.postid
                JOIN "user" s ON s.userid = p.sellerid
                WHERE
                    (:keyword IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :keyword, '%')))
                AND (:address IS NULL OR LOWER(s.address) LIKE LOWER(CONCAT('%', :address, '%')))
                AND (:brand IS NULL OR LOWER(v.brand) LIKE LOWER(CONCAT('%', :brand, '%')))
                AND (:version IS NULL OR LOWER(v.version) LIKE LOWER(CONCAT('%', :version, '%')))
                AND (:color IS NULL OR LOWER(v.color) LIKE LOWER(CONCAT('%', :color, '%')))
                AND (:origin IS NULL OR LOWER(v.origin) LIKE LOWER(CONCAT('%', :origin, '%')))
                AND (:style IS NULL OR LOWER(v.style) LIKE LOWER(CONCAT('%', :style, '%')))
                AND (:minOdo IS NULL OR v.odo >= :minOdo)
                AND (:maxOdo IS NULL OR v.odo <= :maxOdo)
                AND (:minRange IS NULL OR v.range >= :minRange)
                AND (:maxRange IS NULL OR v.range <= :maxRange)
                AND (:bodyInsurance IS NULL OR v.bodyinsurance = TRUE)
                AND (:vehicleInspection IS NULL OR v.vehicleinspection = TRUE)
                AND (:minPrice IS NULL OR p.price >= :minPrice)
                AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                AND (:minYearManufacture IS NULL OR v.yearmanufacture >= :minYearManufacture)
                AND (:maxYearManufacture IS NULL OR v.yearmanufacture <= :maxYearManufacture)
                AND (:numberOfSeat IS NULL OR v.numberofseat = :numberOfSeat)
                AND LOWER(v.status) = 'available'
            """, nativeQuery = true)
    List<Post> filterVehicles(
            @Param("keyword") String keyword,
            @Param("address") String address,
            @Param("brand") String brand,
            @Param("version") String version,
            @Param("color") String color,
            @Param("origin") String origin,
            @Param("style") String style,
            @Param("minOdo") Integer minOdo,
            @Param("maxOdo") Integer maxOdo,
            @Param("minRange") Integer minRange,
            @Param("maxRange") Integer maxRange,
            @Param("bodyInsurance") Boolean bodyInsurance,
            @Param("vehicleInspection") Boolean vehicleInspection,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minYearManufacture") Integer minYearManufacture,
            @Param("maxYearManufacture") Integer maxYearManufacture,
            @Param("numberOfSeat") Integer numberOfSeat
    );

    @Query(value = """
                SELECT p.* FROM post p
                INNER JOIN battery b ON p.PostID = b.PostID
                INNER JOIN batterytype bt ON b.BatteryTypeID = bt.ID
                INNER JOIN "user" s ON s.UserID = p.SellerID
                WHERE
                    (:keyword IS NULL OR LOWER(bt.typename) LIKE LOWER(CONCAT('%', CAST(:keyword AS TEXT), '%')))
                AND (:address IS NULL OR LOWER(s.address) LIKE LOWER(CONCAT('%', CAST(:address AS TEXT), '%')))
                AND (:batteryType IS NULL OR LOWER(bt.typename) LIKE LOWER(CONCAT('%', CAST(:batteryType AS TEXT), '%')))
                AND (:serialNumber IS NULL OR LOWER(b.serialnumber) LIKE LOWER(CONCAT('%', CAST(:serialNumber AS TEXT), '%')))
                AND (:minOriginCapacity IS NULL OR b.origincapacity >= :minOriginCapacity)
                AND (:maxOriginCapacity IS NULL OR b.origincapacity <= :maxOriginCapacity)
                AND (:minRemainingCapacity IS NULL OR b.remainingcapacity >= :minRemainingCapacity)
                AND (:maxRemainingCapacity IS NULL OR b.remainingcapacity <= :maxRemainingCapacity)
                AND (:minMileageCovered IS NULL OR b.mileagecovered >= :minMileageCovered)
                AND (:maxMileageCovered IS NULL OR b.mileagecovered <= :maxMileageCovered)
                AND (:minVoltage IS NULL OR b.voltage >= :minVoltage)
                AND (:maxVoltage IS NULL OR b.voltage <= :maxVoltage)
                AND (:minCycleCount IS NULL OR b.cyclecount >= :minCycleCount)
                AND (:maxCycleCount IS NULL OR b.cyclecount <= :maxCycleCount)
                AND (:warranty IS NULL OR LOWER(b.warranty) LIKE LOWER(CONCAT('%', CAST(:warranty AS TEXT), '%')))
                AND (:minWeight IS NULL OR b.weight >= :minWeight)
                AND (:maxWeight IS NULL OR b.weight <= :maxWeight)
                AND (:lifeCycle IS NULL OR LOWER(b.lifecycle) LIKE LOWER(CONCAT('%', CAST(:lifeCycle AS TEXT), '%')))
                AND (:minPrice IS NULL OR p.price >= :minPrice)
                AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                AND p.status = 'APPROVE'
            """, nativeQuery = true)
    List<Post> filterBatteries(
            @Param("keyword") String keyword,
            @Param("address") String address,
            @Param("batteryType") String batteryType,
            @Param("serialNumber") String serialNumber,
            @Param("minOriginCapacity") BigDecimal minOriginCapacity,
            @Param("maxOriginCapacity") BigDecimal maxOriginCapacity,
            @Param("minRemainingCapacity") BigDecimal minRemainingCapacity,
            @Param("maxRemainingCapacity") BigDecimal maxRemainingCapacity,
            @Param("minMileageCovered") Integer minMileageCovered,
            @Param("maxMileageCovered") Integer maxMileageCovered,
            @Param("minVoltage") BigDecimal minVoltage,
            @Param("maxVoltage") BigDecimal maxVoltage,
            @Param("minCycleCount") Integer minCycleCount,
            @Param("maxCycleCount") Integer maxCycleCount,
            @Param("warranty") String warranty,
            @Param("minWeight") BigDecimal minWeight,
            @Param("maxWeight") BigDecimal maxWeight,
            @Param("lifeCycle") String lifeCycle,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    @Query("SELECT p FROM Post p WHERE p.vehicle IS NOT NULL AND p.status = 'APPROVE' ORDER BY p.createdAt DESC")
    List<Post> findAllVehiclePosts();


    @Query("SELECT p FROM Post p WHERE p.battery IS NOT NULL AND p.status = 'APPROVE' ORDER BY p.createdAt DESC")
    List<Post> findAllBatteryPosts();

    @Query(value = "SELECT vi.ImageURL FROM Vehicle v " +
            "JOIN Post p ON p.PostID = v.PostID " +
            "JOIN VehicleImage vi ON vi.PostID = v.PostID " +
            "WHERE p.PostID = :postId " +
            "LIMIT 1 ",
            nativeQuery = true)
    String getThumbnailUrlByPostId(@Param("postId") int postId);


}