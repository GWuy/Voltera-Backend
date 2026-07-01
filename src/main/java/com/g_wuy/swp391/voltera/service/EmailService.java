package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.Contract;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EmailService {

    JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP Code");
        message.setText(
                "Dear user,\n\n" +
                        "Your OTP code is: " + otp + "\n" +
                        "It is valid for 5 minutes.\n\n" +
                        "If you did not request this code, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "Voltera Team"
        );
        message.setFrom("buigiahuy1506@gmail.com");
        mailSender.send(message);
        System.out.println("Sent OTP to " + to + ": " + otp);
    }

    public void sendContractEmailWithAttachment(Contract contract, MultipartFile file) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(new String[]{
                    contract.getBuyerid().getEmail(),
                    contract.getSellerid().getEmail()
            });
            helper.setSubject("Contract Signed - #" + contract.getId());
            helper.setText("""
                        Dear users,
                    
                        Your contract has been signed by both parties.
                        Please find the attached PDF copy for your reference.
                    
                        Best regards,
                        Voltera Team
                    """);

            helper.addAttachment("Contract_" + contract.getId() + ".pdf",
                    new ByteArrayResource(file.getBytes()));

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send contract email", e);
        }
    }
}
