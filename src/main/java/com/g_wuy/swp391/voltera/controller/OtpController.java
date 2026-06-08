package com.g_wuy.swp391.voltera.controller;

import com.g_wuy.swp391.voltera.exception.BusinessException;
import com.g_wuy.swp391.voltera.model.request.OtpRequest;
import com.g_wuy.swp391.voltera.model.request.PasswordResetRequest;
import com.g_wuy.swp391.voltera.service.OtpService;
import com.g_wuy.swp391.voltera.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpController {

    @PostMapping("/forgot/reset")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        userService.updatePassword(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successfully");
    }

    OtpService otpService;
    UserService userService;

    @PostMapping("/request")
    public ResponseEntity<String> requestOtp(@RequestParam String email) {
        // Kiểm tra email đã tồn tại chưa cho registration
        try {
            userService.checkEmailExists(email);
            return ResponseEntity.badRequest().body("This email is already registered");
        } catch (BusinessException e) {
            // Email chưa tồn tại, tiếp tục gửi OTP
        }
        otpService.generateOtp(email);
        return ResponseEntity.ok("OTP has been sent to " + email);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest request) {
        try {
            userService.verifyRegisterOtp(request.getEmail(), request.getOtp());
            return ResponseEntity.ok("Email verified successfully");
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify/register")
    public ResponseEntity<?> verifyRegisterOtp(@RequestBody OtpRequest request) {
        try {
            // Chỉ verify OTP, không tạo account
            if (!otpService.verifyOtp(request.getEmail(), request.getOtp())) {
                return ResponseEntity.badRequest().body("OTP invalid or expired");
            }
            return ResponseEntity.ok("OTP verified successfully");
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<String> resendOtp(@RequestParam String email) {
        otpService.resendOtp(email);
        return ResponseEntity.ok("OTP resent to " + email);
    }


    @PostMapping("/forgot/request")
    public ResponseEntity<String> requestPasswordOtp(@RequestParam String email) {
        userService.checkEmailExists(email);
        otpService.generateOtp(email);
        return ResponseEntity.ok("OTP for password reset sent to " + email);
    }

    @PostMapping("/forgot/verify")
    public ResponseEntity<?> verifyPasswordOtp(@RequestBody PasswordResetRequest request) {
        boolean valid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (valid) {
            userService.updatePassword(request.getEmail(), request.getNewPassword());
            return ResponseEntity.ok("Password reset successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
    }
}
