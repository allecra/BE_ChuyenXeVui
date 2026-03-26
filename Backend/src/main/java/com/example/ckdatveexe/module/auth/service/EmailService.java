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

    // Phương thức cho Bus Company Registration
    public void sendRegistrationConfirmationEmail(String toEmail, String companyName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đăng ký nhà xe - " + companyName);
            helper.setText(buildRegistrationConfirmationEmailContent(companyName), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email xác nhận đăng ký", e);
        }
    }

    public void sendApprovalEmail(String toEmail, String companyName, boolean approved, String adminNotes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);

            if (approved) {
                helper.setSubject("Đơn đăng ký nhà xe được duyệt - " + companyName);
                helper.setText(buildApprovalEmailContent(companyName, adminNotes), true);
            } else {
                helper.setSubject("Đơn đăng ký nhà xe bị từ chối - " + companyName);
                helper.setText(buildRejectionEmailContent(companyName, adminNotes), true);
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email thông báo duyệt", e);
        }
    }

    public void sendCompanyAccountCreatedEmail(String toEmail, String companyName, String temporaryPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Tài khoản nhà xe đã được tạo - " + companyName);
            helper.setText(buildCompanyAccountCreatedEmailContent(companyName, temporaryPassword), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email tạo tài khoản", e);
        }
    }

    private String buildRegistrationConfirmationEmailContent(String companyName) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #2c5aa0;">Xác nhận đăng ký nhà xe</h2>
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Chúng tôi đã nhận được đơn đăng ký nhà xe của bạn.</p>
                        <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p><strong>Thông tin đăng ký:</strong></p>
                            <ul>
                                <li>Tên nhà xe: %s</li>
                                <li>Trạng thái: Đang chờ xét duyệt</li>
                                <li>Thời gian xử lý: 1-3 ngày làm việc</li>
                            </ul>
                        </div>
                        <p>Chúng tôi sẽ xem xét đơn đăng ký và gửi thông báo kết quả qua email này.</p>
                        <p>Cảm ơn bạn đã tin tưởng dịch vụ của chúng tôi!</p>
                        <hr style="margin: 30px 0;">
                        <p style="font-size: 12px; color: #666;">
                            Email này được gửi tự động, vui lòng không trả lời.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(companyName, companyName);
    }

    private String buildApprovalEmailContent(String companyName, String adminNotes) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #28a745;">Đơn đăng ký được duyệt!</h2>
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Chúc mừng! Đơn đăng ký nhà xe của bạn đã được duyệt.</p>
                        <div style="background-color: #d4edda; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #28a745;">
                            <p><strong>Trạng thái:</strong> Đã duyệt ✅</p>
                            %s
                        </div>
                        <p>Tài khoản quản lý nhà xe đã được tạo và thông tin đăng nhập sẽ được gửi trong email riêng.</p>
                        <p>Bạn có thể bắt đầu sử dụng hệ thống để quản lý nhà xe của mình.</p>
                        <p>Cảm ơn bạn đã tham gia cùng chúng tôi!</p>
                        <hr style="margin: 30px 0;">
                        <p style="font-size: 12px; color: #666;">
                            Email này được gửi tự động, vui lòng không trả lời.
                        </p>
                    </div>
                </body>
                </html>
                """
                .formatted(companyName,
                        adminNotes != null && !adminNotes.trim().isEmpty()
                                ? "<p><strong>Ghi chú từ admin:</strong> " + adminNotes + "</p>"
                                : "");
    }

    private String buildRejectionEmailContent(String companyName, String adminNotes) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #dc3545;">Đơn đăng ký bị từ chối</h2>
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Rất tiếc, đơn đăng ký nhà xe của bạn không được duyệt.</p>
                        <div style="background-color: #f8d7da; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #dc3545;">
                            <p><strong>Trạng thái:</strong> Bị từ chối ❌</p>
                            %s
                        </div>
                        <p>Bạn có thể chỉnh sửa thông tin và đăng ký lại sau khi khắc phục các vấn đề được nêu.</p>
                        <p>Nếu có thắc mắc, vui lòng liên hệ với chúng tôi để được hỗ trợ.</p>
                        <hr style="margin: 30px 0;">
                        <p style="font-size: 12px; color: #666;">
                            Email này được gửi tự động, vui lòng không trả lời.
                        </p>
                    </div>
                </body>
                </html>
                """
                .formatted(companyName,
                        adminNotes != null && !adminNotes.trim().isEmpty()
                                ? "<p><strong>Lý do từ chối:</strong> " + adminNotes + "</p>"
                                : "");
    }

    private String buildCompanyAccountCreatedEmailContent(String companyName, String temporaryPassword) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #2c5aa0;">Tài khoản nhà xe đã được tạo</h2>
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Tài khoản quản lý nhà xe của bạn đã được tạo thành công!</p>
                        <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #ffc107;">
                            <p><strong>Thông tin đăng nhập:</strong></p>
                            <ul>
                                <li><strong>Email:</strong> (email này)</li>
                                <li><strong>Mật khẩu tạm thời:</strong> <code style="background-color: #f8f9fa; padding: 2px 4px; border-radius: 3px;">%s</code></li>
                            </ul>
                            <p style="color: #856404; margin-top: 10px;">
                                ⚠️ <strong>Quan trọng:</strong> Vui lòng đổi mật khẩu ngay sau khi đăng nhập lần đầu để bảo mật tài khoản.
                            </p>
                        </div>
                        <p>Bạn có thể đăng nhập vào hệ thống để bắt đầu quản lý nhà xe của mình.</p>
                        <p>Chúc bạn kinh doanh thành công!</p>
                        <hr style="margin: 30px 0;">
                        <p style="font-size: 12px; color: #666;">
                            Email này được gửi tự động, vui lòng không trả lời.
                        </p>
                    </div>
                </body>
                </html>
                """
                .formatted(companyName, temporaryPassword);
    }
}