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

    public void sendApprovalNotification(String toEmail, String companyName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Đăng ký nhà xe được duyệt - CK DatVeXe");
            helper.setText(buildApprovalNotificationContent(companyName), true);

            mailSender.send(message);
            log.info("Approval notification email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send approval notification email to: {}", toEmail, e);
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

    private String buildRegistrationConfirmationContent(String companyName) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                                .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                                .highlight { background: #e3f2fd; padding: 15px; border-left: 4px solid #2196f3; margin: 20px 0; }
                                .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                                .logo { font-size: 24px; font-weight: bold; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <div class="logo">🚌 CK DatVeXe</div>
                                    <h2>Xác nhận đăng ký nhà xe</h2>
                                </div>
                                <div class="content">
                                    <h3>Kính chào %s,</h3>
                                    <p>Cảm ơn bạn đã đăng ký nhà xe với <strong>CK DatVeXe</strong>!</p>

                                    <div class="highlight">
                                        <h4>📋 Thông tin đăng ký của bạn:</h4>
                                        <p><strong>Tên nhà xe:</strong> %s</p>
                                        <p><strong>Trạng thái:</strong> Đang chờ xem xét</p>
                                        <p><strong>Thời gian đăng ký:</strong> %s</p>
                                    </div>

                                    <p>Chúng tôi đã nhận được đơn đăng ký của bạn và hiện đang trong quá trình xem xét.</p>
                                    <p>Đội ngũ của chúng tôi sẽ kiểm tra thông tin và thông báo kết quả xét duyệt qua email này trong thời gian sớm nhất.</p>

                                    <p><strong>Lưu ý:</strong> Vui lòng đảm bảo thông tin liên hệ luôn chính xác để nhận được thông báo kịp thời.</p>
                                </div>
                                <div class="footer">
                                    <p>Trân trọng,<br><strong>Đội ngũ CK DatVeXe</strong></p>
                                    <p>📧 Email: support@ckdatveexe.com | 📞 Hotline: 1900-xxxx</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                companyName, companyName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    private String buildApprovalNotificationContent(String companyName) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background: linear-gradient(135deg, #4caf50 0%%, #45a049 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                                .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                                .success-box { background: #e8f5e8; padding: 20px; border-left: 4px solid #4caf50; margin: 20px 0; border-radius: 5px; }
                                .next-steps { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; border-radius: 5px; }
                                .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                                .logo { font-size: 24px; font-weight: bold; }
                                .celebration { font-size: 48px; text-align: center; margin: 20px 0; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <div class="logo">🚌 CK DatVeXe</div>
                                    <h2>Chúc mừng! Đăng ký được duyệt</h2>
                                </div>
                                <div class="content">
                                    <div class="celebration">🎉</div>
                                    <h3>Kính chào %s,</h3>

                                    <div class="success-box">
                                        <h4>✅ Đăng ký thành công!</h4>
                                        <p>Đơn đăng ký nhà xe của bạn đã được <strong>duyệt thành công</strong>!</p>
                                        <p><strong>Tên nhà xe:</strong> %s</p>
                                        <p><strong>Thời gian duyệt:</strong> %s</p>
                                    </div>

                                    <p>Chúc mừng bạn đã trở thành đối tác của <strong>CK DatVeXe</strong>!</p>

                                    <div class="next-steps">
                                        <h4>📋 Các bước tiếp theo:</h4>
                                        <ul>
                                            <li>Đăng nhập vào hệ thống quản lý</li>
                                            <li>Cập nhật thông tin chi tiết nhà xe</li>
                                            <li>Thêm thông tin tuyến đường và lịch trình</li>
                                            <li>Bắt đầu bán vé trực tuyến</li>
                                        </ul>
                                    </div>

                                    <p>Để biết thêm thông tin chi tiết về cách sử dụng hệ thống, vui lòng liên hệ với đội ngũ hỗ trợ của chúng tôi.</p>
                                </div>
                                <div class="footer">
                                    <p>Trân trọng,<br><strong>Đội ngũ CK DatVeXe</strong></p>
                                    <p>📧 Email: support@ckdatveexe.com | 📞 Hotline: 1900-xxxx</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                companyName, companyName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    private String buildApprovalNotificationWithAccountContent(String companyName, String username,
            String temporaryPassword) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background: linear-gradient(135deg, #4caf50 0%%, #45a049 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                                .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                                .success-box { background: #e8f5e8; padding: 20px; border-left: 4px solid #4caf50; margin: 20px 0; border-radius: 5px; }
                                .account-box { background: #fff3cd; padding: 20px; border-left: 4px solid #ffc107; margin: 20px 0; border-radius: 5px; }
                                .security-box { background: #ffebee; padding: 15px; border-left: 4px solid #f44336; margin: 20px 0; border-radius: 5px; }
                                .next-steps { background: #e3f2fd; padding: 15px; border-left: 4px solid #2196f3; margin: 20px 0; border-radius: 5px; }
                                .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                                .logo { font-size: 24px; font-weight: bold; }
                                .celebration { font-size: 48px; text-align: center; margin: 20px 0; }
                                .credential { background: #f5f5f5; padding: 10px; border-radius: 5px; font-family: monospace; font-size: 16px; margin: 10px 0; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <div class="logo">🚌 CK DatVeXe</div>
                                    <h2>Chúc mừng! Tài khoản đã được tạo</h2>
                                </div>
                                <div class="content">
                                    <div class="celebration">🎉</div>
                                    <h3>Kính chào %s,</h3>

                                    <div class="success-box">
                                        <h4>✅ Đăng ký thành công!</h4>
                                        <p>Đơn đăng ký nhà xe của bạn đã được <strong>duyệt thành công</strong>!</p>
                                        <p><strong>Tên nhà xe:</strong> %s</p>
                                        <p><strong>Thời gian duyệt:</strong> %s</p>
                                    </div>

                                    <div class="account-box">
                                        <h4>🔑 Thông tin tài khoản đăng nhập</h4>
                                        <p>Chúng tôi đã tạo tài khoản quản lý nhà xe cho bạn:</p>
                                        <p><strong>Tên đăng nhập (Email):</strong></p>
                                        <div class="credential">%s</div>
                                        <p><strong>Mật khẩu tạm thời:</strong></p>
                                        <div class="credential">%s</div>
                                    </div>

                                    <div class="security-box">
                                        <h4>🔒 Bảo mật tài khoản</h4>
                                        <p><strong>Quan trọng:</strong> Vui lòng đổi mật khẩu ngay sau lần đăng nhập đầu tiên để đảm bảo bảo mật tài khoản.</p>
                                    </div>

                                    <div class="next-steps">
                                        <h4>📋 Các bước tiếp theo:</h4>
                                        <ol>
                                            <li><strong>Đăng nhập</strong> vào hệ thống với thông tin trên</li>
                                            <li><strong>Đổi mật khẩu</strong> ngay lập tức</li>
                                            <li><strong>Cập nhật thông tin</strong> chi tiết nhà xe</li>
                                            <li><strong>Thêm xe</strong> và thiết lập sơ đồ ghế</li>
                                            <li><strong>Tạo tuyến đường</strong> và lịch trình</li>
                                            <li><strong>Bắt đầu</strong> bán vé trực tuyến</li>
                                        </ol>
                                    </div>

                                    <p>Chào mừng bạn đến với gia đình <strong>CK DatVeXe</strong>! Chúng tôi sẵn sàng hỗ trợ bạn trong hành trình phát triển kinh doanh.</p>
                                </div>
                                <div class="footer">
                                    <p>Trân trọng,<br><strong>Đội ngũ CK DatVeXe</strong></p>
                                    <p>📧 Email: support@ckdatveexe.com | 📞 Hotline: 1900-xxxx</p>
                                    <p><small>⚠️ Vui lòng không chia sẻ thông tin đăng nhập với bất kỳ ai</small></p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                companyName, companyName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                username, temporaryPassword);
    }

    public void sendPasswordChangeNotification(String toEmail, String companyName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Đổi mật khẩu thành công - CK DatVeXe");
            helper.setText(buildPasswordChangeNotificationContent(companyName), true);

            mailSender.send(message);
            log.info("Password change notification email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password change notification email to: {}", toEmail, e);
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

    private String buildRejectionNotificationContent(String companyName, String reason) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background: linear-gradient(135deg, #f44336 0%%, #d32f2f 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                                .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                                .rejection-box { background: #ffebee; padding: 20px; border-left: 4px solid #f44336; margin: 20px 0; border-radius: 5px; }
                                .reason-box { background: #fff3e0; padding: 15px; border-left: 4px solid #ff9800; margin: 20px 0; border-radius: 5px; }
                                .retry-box { background: #e3f2fd; padding: 15px; border-left: 4px solid #2196f3; margin: 20px 0; border-radius: 5px; }
                                .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                                .logo { font-size: 24px; font-weight: bold; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <div class="logo">🚌 CK DatVeXe</div>
                                    <h2>Thông báo về đăng ký nhà xe</h2>
                                </div>
                                <div class="content">
                                    <h3>Kính chào %s,</h3>

                                    <div class="rejection-box">
                                        <h4>❌ Đăng ký chưa được duyệt</h4>
                                        <p>Rất tiếc, đơn đăng ký nhà xe của bạn chưa đáp ứng được các yêu cầu hiện tại.</p>
                                        <p><strong>Tên nhà xe:</strong> %s</p>
                                        <p><strong>Thời gian xem xét:</strong> %s</p>
                                    </div>

                                    <div class="reason-box">
                                        <h4>📝 Lý do cụ thể:</h4>
                                        <p><em>%s</em></p>
                                    </div>

                                    <div class="retry-box">
                                        <h4>🔄 Bạn có thể làm gì tiếp theo?</h4>
                                        <ul>
                                            <li>Xem xét và khắc phục các vấn đề được nêu ra</li>
                                            <li>Chuẩn bị đầy đủ giấy tờ pháp lý</li>
                                            <li>Đăng ký lại với thông tin chính xác và đầy đủ</li>
                                            <li>Liên hệ với chúng tôi nếu cần hỗ trợ thêm</li>
                                        </ul>
                                    </div>

                                    <p>Chúng tôi luôn sẵn sàng hỗ trợ bạn trong quá trình đăng ký. Đừng ngần ngại liên hệ với đội ngũ hỗ trợ nếu bạn có bất kỳ thắc mắc nào.</p>
                                </div>
                                <div class="footer">
                                    <p>Trân trọng,<br><strong>Đội ngũ CK DatVeXe</strong></p>
                                    <p>📧 Email: support@ckdatveexe.com | 📞 Hotline: 1900-xxxx</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                companyName, companyName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                reason != null ? reason : "Không đáp ứng yêu cầu");
    }

    private String buildPasswordResetNotificationContent(String companyName, String newPassword) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background: linear-gradient(135deg, #ff9800 0%%, #f57c00 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                                .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                                .password-box { background: #fff3cd; padding: 20px; border-left: 4px solid #ffc107; margin: 20px 0; border-radius: 5px; }
                                .security-box { background: #ffebee; padding: 15px; border-left: 4px solid #f44336; margin: 20px 0; border-radius: 5px; }
                                .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                                .logo { font-size: 24px; font-weight: bold; }
                                .credential { background: #f5f5f5; padding: 10px; border-radius: 5px; font-family: monospace; font-size: 16px; margin: 10px 0; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <div class="logo">🚌 CK DatVeXe</div>
                                    <h2>🔑 Mật khẩu mới</h2>
                                </div>
                                <div class="content">
                                    <h3>Kính chào %s,</h3>
                                    <p>Chúng tôi đã tạo mật khẩu mới cho tài khoản nhà xe của bạn theo yêu cầu.</p>

                                    <div class="password-box">
                                        <h4>🔑 Mật khẩu mới của bạn</h4>
                                        <p><strong>Mật khẩu:</strong></p>
                                        <div class="credential">%s</div>
                                        <p><strong>Thời gian tạo:</strong> %s</p>
                                    </div>

                                    <div class="security-box">
                                        <h4>🔒 Lưu ý bảo mật quan trọng</h4>
                                        <ul>
                                            <li><strong>Đăng nhập ngay</strong> và đổi mật khẩu mới</li>
                                            <li><strong>Không chia sẻ</strong> mật khẩu này với bất kỳ ai</li>
                                            <li><strong>Sử dụng mật khẩu mạnh</strong> khi thay đổi</li>
                                            <li><strong>Đăng xuất</strong> sau khi sử dụng xong</li>
                                        </ul>
                                    </div>

                                    <p>Nếu bạn không yêu cầu reset mật khẩu, vui lòng liên hệ với chúng tôi ngay lập tức.</p>
                                </div>
                                <div class="footer">
                                    <p>Trân trọng,<br><strong>Đội ngũ CK DatVeXe</strong></p>
                                    <p>📧 Email: support@ckdatveexe.com | 📞 Hotline: 1900-xxxx</p>
                                    <p><small>⚠️ Email này chứa thông tin bảo mật, vui lòng xóa sau khi đã đổi mật khẩu</small></p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                companyName, newPassword,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    private String buildPasswordChangeNotificationContent(String companyName) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background: linear-gradient(135deg, #4caf50 0%%, #45a049 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                                .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                                .success-box { background: #e8f5e8; padding: 20px; border-left: 4px solid #4caf50; margin: 20px 0; border-radius: 5px; }
                                .security-box { background: #e3f2fd; padding: 15px; border-left: 4px solid #2196f3; margin: 20px 0; border-radius: 5px; }
                                .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                                .logo { font-size: 24px; font-weight: bold; }
                                .celebration { font-size: 48px; text-align: center; margin: 20px 0; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <div class="logo">🚌 CK DatVeXe</div>
                                    <h2>🔐 Đổi mật khẩu thành công</h2>
                                </div>
                                <div class="content">
                                    <div class="celebration">✅</div>
                                    <h3>Kính chào %s,</h3>

                                    <div class="success-box">
                                        <h4>🎉 Đổi mật khẩu thành công!</h4>
                                        <p>Mật khẩu tài khoản nhà xe của bạn đã được thay đổi thành công.</p>
                                        <p><strong>Thời gian thay đổi:</strong> %s</p>
                                        <p><strong>Địa chỉ IP:</strong> [Được ẩn vì lý do bảo mật]</p>
                                    </div>

                                    <div class="security-box">
                                        <h4>🔒 Lưu ý bảo mật</h4>
                                        <ul>
                                            <li><strong>Nếu bạn không thực hiện thay đổi này</strong>, vui lòng liên hệ với chúng tôi ngay lập tức</li>
                                            <li><strong>Đăng xuất</strong> khỏi tất cả thiết bị và đăng nhập lại</li>
                                            <li><strong>Không chia sẻ</strong> mật khẩu mới với bất kỳ ai</li>
                                            <li><strong>Sử dụng mật khẩu mạnh</strong> và thay đổi định kỳ</li>
                                        </ul>
                                    </div>

                                    <p>Cảm ơn bạn đã sử dụng dịch vụ của <strong>CK DatVeXe</strong>. Chúng tôi luôn đặt bảo mật tài khoản của bạn lên hàng đầu.</p>
                                </div>
                                <div class="footer">
                                    <p>Trân trọng,<br><strong>Đội ngũ CK DatVeXe</strong></p>
                                    <p>📧 Email: support@ckdatveexe.com | 📞 Hotline: 1900-xxxx</p>
                                    <p><small>🔐 Email này được gửi tự động để thông báo về thay đổi bảo mật</small></p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                companyName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    // New methods for bus company admin notifications

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

    private String buildCompanyUpdateNotificationContent(String companyName, String updateMessage) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background: linear-gradient(135deg, #2196f3 0%%, #1976d2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                                .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                                .update-box { background: #e3f2fd; padding: 20px; border-left: 4px solid #2196f3; margin: 20px 0; border-radius: 5px; }
                                .info-box { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; border-radius: 5px; }
                                .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                                .logo { font-size: 24px; font-weight: bold; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <div class="logo">🚌 CK DatVeXe</div>
                                    <h2>📝 Thông báo cập nhật</h2>
                                </div>
                                <div class="content">
                                    <h3>Kính chào %s,</h3>

                                    <div class="update-box">
                                        <h4>📋 Thông tin cập nhật</h4>
                                        <p><strong>Tên nhà xe:</strong> %s</p>
                                        <p><strong>Thời gian cập nhật:</strong> %s</p>
                                        <p><strong>Nội dung:</strong> %s</p>
                                    </div>

                                    <div class="info-box">
                                        <h4>ℹ️ Lưu ý</h4>
                                        <p>Thông tin nhà xe của bạn đã được cập nhật trong hệ thống. Vui lòng đăng nhập để xem chi tiết các thay đổi.</p>
                                    </div>

                                    <p>Nếu bạn có bất kỳ thắc mắc nào về việc cập nhật này, vui lòng liên hệ với đội ngũ hỗ trợ của chúng tôi.</p>
                                </div>
                                <div class="footer">
                                    <p>Trân trọng,<br><strong>Đội ngũ CK DatVeXe</strong></p>
                                    <p>📧 Email: support@ckdatveexe.com | 📞 Hotline: 1900-xxxx</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                companyName, companyName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                updateMessage);
    }

    private String buildAccountRestorationNotificationContent(String companyName) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background: linear-gradient(135deg, #4caf50 0%%, #45a049 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                                .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                                .success-box { background: #e8f5e8; padding: 20px; border-left: 4px solid #4caf50; margin: 20px 0; border-radius: 5px; }
                                .next-steps { background: #e3f2fd; padding: 15px; border-left: 4px solid #2196f3; margin: 20px 0; border-radius: 5px; }
                                .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                                .logo { font-size: 24px; font-weight: bold; }
                                .celebration { font-size: 48px; text-align: center; margin: 20px 0; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <div class="logo">🚌 CK DatVeXe</div>
                                    <h2>🎉 Tài khoản được khôi phục</h2>
                                </div>
                                <div class="content">
                                    <div class="celebration">✅</div>
                                    <h3>Kính chào %s,</h3>

                                    <div class="success-box">
                                        <h4>🎉 Tài khoản đã được khôi phục!</h4>
                                        <p>Tài khoản nhà xe của bạn đã được khôi phục thành công.</p>
                                        <p><strong>Tên nhà xe:</strong> %s</p>
                                        <p><strong>Thời gian khôi phục:</strong> %s</p>
                                        <p><strong>Trạng thái:</strong> Hoạt động</p>
                                    </div>

                                    <div class="next-steps">
                                        <h4>📋 Bạn có thể làm gì tiếp theo?</h4>
                                        <ul>
                                            <li><strong>Đăng nhập</strong> vào hệ thống quản lý</li>
                                            <li><strong>Kiểm tra</strong> thông tin nhà xe</li>
                                            <li><strong>Cập nhật</strong> thông tin nếu cần thiết</li>
                                            <li><strong>Tiếp tục</strong> hoạt động kinh doanh</li>
                                        </ul>
                                    </div>

                                    <p>Chào mừng bạn trở lại với <strong>CK DatVeXe</strong>! Chúng tôi rất vui khi được phục vụ bạn tiếp tục.</p>
                                </div>
                                <div class="footer">
                                    <p>Trân trọng,<br><strong>Đội ngũ CK DatVeXe</strong></p>
                                    <p>📧 Email: support@ckdatveexe.com | 📞 Hotline: 1900-xxxx</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                companyName, companyName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
}