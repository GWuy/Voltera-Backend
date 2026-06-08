package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.Fee;
import com.g_wuy.swp391.voltera.entity.Post;
import com.g_wuy.swp391.voltera.entity.User;
import com.g_wuy.swp391.voltera.repository.FeeRepository;
import com.g_wuy.swp391.voltera.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FeeScheduler {

    FeeRepository feeRepository;

    PostRepository postRepository;

    @Scheduled(cron = "0 37 7 * * ?")
    @Transactional
    public void scheduled() {
        LocalDateTime fifteenDaysAgo = LocalDateTime.now().minusDays(15);

        List<Fee> expiredFees = feeRepository.findByCreatedAtBeforeAndFeeStatus(fifteenDaysAgo, "PAID");

        for (Fee fee : expiredFees) {
            fee.setFeeStatus("PENDING");
            feeRepository.save(fee);

            Post post = fee.getPost();
            if (post != null) {
                post.setStatus("PENDING");
                postRepository.save(post);
            }
        }
    }
}