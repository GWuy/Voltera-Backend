package com.g_wuy.swp391.voltera.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OtpService {

    private static final long OTP_TTL_MILLIS = TimeUnit.MINUTES.toMillis(5);

    RedisTemplate<String, String> redisTemplate;

    EmailService emailService;

    ConcurrentMap<String, LocalOtp> localOtpStore = new ConcurrentHashMap<>();

    public void generateOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(899999) + 100000); // OTP 6 số
        String key = "OTP:" + email;

        try {
            redisTemplate.opsForValue().set(key, otp, 5, TimeUnit.MINUTES);
            localOtpStore.remove(key);
        } catch (RuntimeException ex) {
            localOtpStore.put(key, new LocalOtp(otp, Instant.now().toEpochMilli() + OTP_TTL_MILLIS));
        }


        emailService.sendOtpEmail(email, otp);
    }


    public boolean verifyOtp(String email, String otp) {
        String key = "OTP:" + email;
        try {
            String value = redisTemplate.opsForValue().get(key);

            if (value != null && value.equals(otp)) {
                redisTemplate.delete(key);
                localOtpStore.remove(key);
                return true;
            }
        } catch (RuntimeException ex) {
            LocalOtp fallbackOtp = localOtpStore.get(key);
            if (fallbackOtp != null && fallbackOtp.isValid() && fallbackOtp.otp().equals(otp)) {
                localOtpStore.remove(key);
                return true;
            }
        }

        LocalOtp fallbackOtp = localOtpStore.get(key);
        if (fallbackOtp != null && fallbackOtp.isValid() && fallbackOtp.otp().equals(otp)) {
            localOtpStore.remove(key);
            return true;
        }

        return false;
    }

    public void resendOtp(String email) {
        generateOtp(email);
    }

    private record LocalOtp(String otp, long expiresAtMillis) {
        boolean isValid() {
            return Instant.now().toEpochMilli() <= expiresAtMillis;
        }
    }
}
