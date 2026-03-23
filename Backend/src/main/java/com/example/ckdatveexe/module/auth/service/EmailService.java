package com.example.ckdatveexe.module.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - CK DatVeXe");
            message.setText(buildPasswordResetEmailContent(otp));

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendPasswordChangeNotification(String toEmail, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Thông báo thay đổi mật khẩu - CK DatVeXe");
            message.setText(buildPasswordChangeNotificationContent(fullName));

            mailSender.send(message);
            log.info("📧 Password change notification sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("💥 Failed to send password change notification to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildPasswordResetEmailContent(String otp) {
        return String.format(
                "Dear User,\n\n" +
                        "You have requested to reset your password for CK DatVeXe.\n\n" +
                        "Your OTP code is: %s\n\n" +
                        "This OTP will expire in 15 minutes.\n\n" +
                        "If you did not request this password reset, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "CK DatVeXe Team",
                otp);
    }

    private String buildPasswordChangeNotificationContent(String fullName) {
        return String.format(
                "Xin chào %s,\n\n" +
                        "Chúng tôi xác nhận rằng mật khẩu tài khoản CK DatVeXe của bạn đã được thay đổi thành công.\n\n"
                        +
                        "Thời gian thay đổi: %s\n\n" +
                        "Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ với chúng tôi ngay lập tức để bảo mật tài khoản.\n\n"
                        +
                        "Trân trọng,\n" +
                        "Đội ngũ CK DatVeXe\n\n" +
                        "---\n" +
                        "Email này được gửi tự động, vui lòng không trả lời.",
                fullName,
                java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }

    // ===== BUS COMPANY EMAIL METHODS =====

    public void sendRegistrationConfirmation(String toEmail, String companyName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Đăng ký nhà xe thành công - CK DatVeXe");
            helper.setText(buildRegistrationConfirmationContent(companyName), true);

            mailSender.send(message);
            log.info("Registration confirmation email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send registration confirmation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendApprovalNotificationWithAccount(String toEmail, String companyName, String username,
            String temporaryPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Đăng ký nhà xe được duyệt - Thông tin tài khoản - CK DatVeXe");
            helper.setText(buildApprovalNotificationWithAccountContent(companyName, username, temporaryPassword), true);

            mailSender.send(message);
            log.info("Approval notification with account email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send approval notification with account email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendRejectionNotification(String toEmail, String companyName, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Đăng ký nhà xe bị từ chối - CK DatVeXe");
            helper.setText(buildRejectionNotificationContent(companyName, reason), true);

            mailSender.send(message);
            log.info("Rejection notification email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send rejection notification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendPasswordResetNotification(String toEmail, String companyName, String newPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Mật khẩu mới cho tài khoản nhà xe - CK DatVeXe");
            helper.setText(buildPasswordResetNotificationContent(companyName, newPassword), true);

            mailSender.send(message);
            log.info("Password reset notification email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset notification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendCompanyUpdateNotification(String toEmail, String companyName, String updateMessage) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Thông tin nhà xe được cập nhật - CK DatVeXe");
            helper.setText(buildCompanyUpdateNotificationContent(companyName, updateMessage), true);

            mailSender.send(message);
            log.info("Company update notification email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send company update notification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendAccountRestorationNotification(String toEmail, String companyName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Tài khoản nhà xe được khôi phục - CK DatVeXe");
            helper.setText(buildAccountRestorationNotificationContent(companyName), true);

            mailSender.send(message);
            log.info("Account restoration notification email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send account restoration notification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendAccountBlockNotification(String toEmail, String companyName, String blockMessage) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Tài khoản nhà xe bị tạm khóa - CK DatVeXe");
            helper.setText(buildAccountBlockNotificationContent(companyName, blockMessage), true);

            mailSender.send(message);
            log.info("Account block notification email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send account block notification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    // ===== PRIVATE EMAIL CONTENT BUILDERS =====

    private String buildRegistrationConfirmationContent(String companyName) {
        return String.format(
                "Xin chào %s,\n\n" +
                        "Cảm ơn bạn đã đăng ký nhà xe với CK DatVeXe!\n\n" +
                        "Chúng tôi đã nhận được đơn đăng ký của bạn và hiện đang trong quá trình xem xét.\n" +
                        "Đội ngũ của chúng tôi sẽ kiểm tra thông tin và thông báo kết quả xét duyệt qua email này trong thời gian sớm nhất.\n\n"
                        +
                        "Thời gian đăng ký: %s\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ CK DatVeXe",
                companyName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    private String buildApprovalNotificationWithAccountContent(String companyName, String username,
            String temporaryPassword) {
        return String.format(
                "Xin chào %s,\n\n" +
                        "Chúc mừng! Đơn đăng ký nhà xe của bạn đã được duyệt thành công!\n\n" +
                        "Thông tin tài khoản đăng nhập:\n" +
                        "- Email đăng nhập: %s\n" +
                        "- Mật khẩu tạm thời: %s\n\n" +
                        "Vui lòng đăng nhập và đổi mật khẩu ngay sau lần đăng nhập đầu tiên để đảm bảo bảo mật tài khoản.\n\n"
                        +
                        "Chào mừng bạn đến với gia đình CK DatVeXe!\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ CK DatVeXe",
                companyName, username, temporaryPassword);
    }

    private String buildRejectionNotificationContent(String companyName, String reason) {
        return String.format(
                "Xin chào %s,\n\n" +
                        "Rất tiếc, đơn đăng ký nhà xe của bạn chưa đáp ứng được các yêu cầu hiện tại.\n\n" +
                        "Lý do: %s\n\n" +
                        "Bạn có thể xem xét và khắc phục các vấn đề được nêu ra, sau đó đăng ký lại với thông tin chính xác và đầy đủ.\n\n"
                        +
                        "Nếu có thắc mắc, vui lòng liên hệ với chúng tôi.\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ CK DatVeXe",
                companyName, reason != null ? reason : "Không đáp ứng yêu cầu");
    }

    private String buildPasswordResetNotificationContent(String companyName, String newPassword) {
        return String.format(
                "Xin chào %s,\n\n" +
                        "Chúng tôi đã tạo mật khẩu mới cho tài khoản nhà xe của bạn theo yêu cầu.\n\n" +
                        "Mật khẩu mới: %s\n" +
                        "Thời gian tạo: %s\n\n" +
                        "Vui lòng đăng nhập ngay và đổi mật khẩu mới để đảm bảo bảo mật.\n" +
                        "Nếu bạn không yêu cầu reset mật khẩu, vui lòng liên hệ với chúng tôi ngay lập tức.\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ CK DatVeXe",
                companyName, newPassword,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    private String buildCompanyUpdateNotificationContent(String companyName, String updateMessage) {
        return String.format(
                "Xin chào %s,\n\n" +
                        "Thông tin nhà xe của bạn đã được cập nhật trong hệ thống.\n\n" +
                        "Nội dung cập nhật: %s\n" +
                        "Thời gian cập nhật: %s\n\n" +
                        "Vui lòng đăng nhập để xem chi tiết các thay đổi.\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ CK DatVeXe",
                companyName, updateMessage,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    private String buildAccountRestorationNotificationContent(String companyName) {
        return String.format(
                "Xin chào %s,\n\n" +
                        "Tài khoản nhà xe của bạn đã được khôi phục thành công.\n\n" +
                        "Thời gian khôi phục: %s\n" +
                        "Trạng thái: Hoạt động\n\n" +
                        "Bạn có thể đăng nhập vào hệ thống và tiếp tục hoạt động kinh doanh.\n\n" +
                        "Chào mừng bạn trở lại với CK DatVeXe!\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ CK DatVeXe",
                companyName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    private String buildAccountBlockNotificationContent(String companyName, String blockMessage) {
        return String.format(
                "Xin chào %s,\n\n" +
                        "Tài khoản nhà xe của bạn đã bị tạm thời khóa bởi quản trị viên hệ thống.\n\n" +
                        "Lý do: %s\n" +
                        "Thời gian khóa: %s\n\n" +
                        "Nếu bạn cho rằng đây là một sự nhầm lẫn, vui lòng liên hệ với đội ngũ hỗ trợ:\n" +
                        "Email: support@ckdatveexe.com\n" +
                        "Hotline: 1900-xxxx\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ CK DatVeXe",
                companyName, blockMessage,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
}