package com.example.ckdatveexe.module.ticket.service;

import com.example.ckdatveexe.shared.entity.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Async
    public void sendTicketBookingConfirmation(Ticket ticket) {
        if (ticket.getPassengerEmail() == null || ticket.getPassengerEmail().isEmpty()) {
            log.warn("No email provided for ticket: {}", ticket.getTicketCode());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(ticket.getPassengerEmail());
            helper.setSubject("Xác nhận đặt vé - " + ticket.getTicketCode() + " - CK DatVeXe");
            helper.setText(buildBookingConfirmationContent(ticket), true);

            mailSender.send(message);
            log.info("📧 Booking confirmation email sent successfully for ticket: {}", ticket.getTicketCode());
        } catch (MessagingException e) {
            log.error("💥 Failed to send booking confirmation email for ticket: {}", ticket.getTicketCode(), e);
        }
    }

    @Async
    public void sendPaymentSuccessNotification(Ticket ticket) {
        if (ticket.getPassengerEmail() == null || ticket.getPassengerEmail().isEmpty()) {
            log.warn("No email provided for ticket: {}", ticket.getTicketCode());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(ticket.getPassengerEmail());
            helper.setSubject("Thanh toán thành công - " + ticket.getTicketCode() + " - CK DatVeXe");
            helper.setText(buildPaymentSuccessContent(ticket), true);

            mailSender.send(message);
            log.info("📧 Payment success email sent successfully for ticket: {}", ticket.getTicketCode());
        } catch (MessagingException e) {
            log.error("💥 Failed to send payment success email for ticket: {}", ticket.getTicketCode(), e);
        }
    }

    @Async
    public void sendTicketCancellationNotification(Ticket ticket) {
        if (ticket.getPassengerEmail() == null || ticket.getPassengerEmail().isEmpty()) {
            log.warn("No email provided for ticket: {}", ticket.getTicketCode());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(ticket.getPassengerEmail());
            helper.setSubject("Hủy vé thành công - " + ticket.getTicketCode() + " - CK DatVeXe");
            helper.setText(buildCancellationContent(ticket), true);

            mailSender.send(message);
            log.info("📧 Cancellation email sent successfully for ticket: {}", ticket.getTicketCode());
        } catch (MessagingException e) {
            log.error("💥 Failed to send cancellation email for ticket: {}", ticket.getTicketCode(), e);
        }
    }

    @Async
    public void sendTicketExpirationNotification(Ticket ticket) {
        if (ticket.getPassengerEmail() == null || ticket.getPassengerEmail().isEmpty()) {
            log.warn("No email provided for ticket: {}", ticket.getTicketCode());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(ticket.getPassengerEmail());
            helper.setSubject("Vé hết hạn thanh toán - " + ticket.getTicketCode() + " - CK DatVeXe");
            helper.setText(buildExpirationContent(ticket), true);

            mailSender.send(message);
            log.info("📧 Expiration email sent successfully for ticket: {}", ticket.getTicketCode());
        } catch (MessagingException e) {
            log.error("💥 Failed to send expiration email for ticket: {}", ticket.getTicketCode(), e);
        }
    }

    @Async
    public void sendPaymentReminderNotification(Ticket ticket, int minutesRemaining) {
        if (ticket.getPassengerEmail() == null || ticket.getPassengerEmail().isEmpty()) {
            log.warn("No email provided for ticket: {}", ticket.getTicketCode());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(ticket.getPassengerEmail());
            helper.setSubject("Nhắc nhở thanh toán - " + ticket.getTicketCode() + " - CK DatVeXe");
            helper.setText(buildPaymentReminderContent(ticket, minutesRemaining), true);

            mailSender.send(message);
            log.info("📧 Payment reminder email sent successfully for ticket: {} ({} minutes remaining)",
                    ticket.getTicketCode(), minutesRemaining);
        } catch (MessagingException e) {
            log.error("💥 Failed to send payment reminder email for ticket: {}", ticket.getTicketCode(), e);
        }
    }

    private String buildBookingConfirmationContent(Ticket ticket) {
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
                                .ticket-box { background: #e3f2fd; padding: 20px; border-left: 4px solid #2196f3; margin: 20px 0; border-radius: 5px; }
                                .warning-box { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; border-radius: 5px; }
                                .info-row { display: flex; justify-content: space-between; margin: 10px 0; }
                                .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                                .logo { font-size: 24px; font-weight: bold; }
                                .ticket-code { background: #f5f5f5; padding: 10px; border-radius: 5px; font-family: monospace; font-size: 18px; font-weight: bold; text-align: center; margin: 15px 0; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <div class="logo">🚌 CK DatVeXe</div>
                                    <h2>🎫 Xác nhận đặt vé</h2>
                                </div>
                                <div class="content">
                                    <h3>Kính chào %s,</h3>
                                    <p>Cảm ơn bạn đã đặt vé với <strong>CK DatVeXe</strong>! Đây là thông tin chi tiết về vé của bạn:</p>

                                    <div class="ticket-box">
                                        <h4>🎫 Thông tin vé</h4>
                                        <div class="ticket-code">%s</div>
                                        <div class="info-row">
                                            <span><strong>Tuyến:</strong></span>
                                            <span>%s → %s</span>
                                        </div>
                                        <div class="info-row">
                                            <span><strong>Khởi hành:</strong></span>
                                            <span>%s</span>
                                        </div>
                                        <div class="info-row">
                                            <span><strong>Ghế:</strong></span>
                                            <span>%s (%s)</span>
                                        </div>
                                        <div class="info-row">
                                            <span><strong>Giá vé:</strong></span>
                                            <span><strong>%,.0f VNĐ</strong></span>
                                        </div>
                                        <div class="info-row">
                                            <span><strong>Trạng thái:</strong></span>
                                            <span><strong style="color: #ff9800;">Chờ thanh toán</strong></span>
                                        </div>
                                    </div>

                                    <div class="warning-box">
                                        <h4>⏰ Quan trọng - Hạn thanh toán</h4>
                                        <p><strong>Vui lòng thanh toán trước:</strong> %s</p>
                                        <p>Vé sẽ tự động hủy nếu không thanh toán trong thời hạn quy định (5 phút).</p>
                                    </div>

                                    <p><strong>Hướng dẫn thanh toán:</strong></p>
                                    <ol>
                                        <li>Đăng nhập vào tài khoản CK DatVeXe</li>
                                        <li>Vào mục "Vé của tôi"</li>
                                        <li>Chọn vé cần thanh toán</li>
                                        <li>Chọn phương thức thanh toán</li>
                                        <li>Hoàn tất thanh toán</li>
                                    </ol>
                                </div>
                                <div class="footer">
                                    <p>Trân trọng,<br><strong>Đội ngũ CK DatVeXe</strong></p>
                                    <p>📧 Email: support@ckdatveexe.com | 📞 Hotline: 1900-xxxx</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                ticket.getPassengerName() != null ? ticket.getPassengerName() : "Quý khách",
                ticket.getTicketCode(),
                getRouteInfo(ticket, "departure"),
                getRouteInfo(ticket, "arrival"),
                ticket.getDepartureTime().format(DATE_FORMATTER),
                ticket.getSeat().getSeatNumber(),
                ticket.getSeat().getSeatType().name(),
                ticket.getPrice(),
                ticket.getPaymentDeadline() != null ? ticket.getPaymentDeadline().format(DATE_FORMATTER)
                        : "Không xác định");
    }

    private String buildPaymentSuccessContent(Ticket ticket) {
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
                                .ticket-box { background: #e3f2fd; padding: 20px; border-left: 4px solid #2196f3; margin: 20px 0; border-radius: 5px; }
                                .info-row { display: flex; justify-content: space-between; margin: 10px 0; }
                                .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                                .logo { font-size: 24px; font-weight: bold; }
                                .celebration { font-size: 48px; text-align: center; margin: 20px 0; }
                                .ticket-code { background: #f5f5f5; padding: 10px; border-radius: 5px; font-family: monospace; font-size: 18px; font-weight: bold; text-align: center; margin: 15px 0; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <div class="logo">🚌 CK DatVeXe</div>
                                    <h2>💳 Thanh toán thành công</h2>
                                </div>
                                <div class="content">
                                    <div class="celebration">🎉</div>
                                    <h3>Kính chào %s,</h3>

                                    <div class="success-box">
                                        <h4>✅ Thanh toán thành công!</h4>
                                        <p>Vé của bạn đã được xác nhận và sẵn sàng sử dụng.</p>
                                        <p><strong>Thời gian thanh toán:</strong> %s</p>
                                    </div>

                                    <div class="ticket-box">
                                        <h4>🎫 Thông tin vé đã xác nhận</h4>
                                        <div class="ticket-code">%s</div>
                                        <div class="info-row">
                                            <span><strong>Tuyến:</strong></span>
                                            <span>%s → %s</span>
                                        </div>
                                        <div class="info-row">
                                            <span><strong>Khởi hành:</strong></span>
                                            <span>%s</span>
                                        </div>
                                        <div class="info-row">
                                            <span><strong>Ghế:</strong></span>
                                            <span>%s (%s)</span>
                                        </div>
                                        <div class="info-row">
                                            <span><strong>Giá vé:</strong></span>
                                            <span><strong>%,.0f VNĐ</strong></span>
                                        </div>
                                        <div class="info-row">
                                            <span><strong>Trạng thái:</strong></span>
                                            <span><strong style="color: #4caf50;">Đã xác nhận</strong></span>
                                        </div>
                                    </div>

                                    <p><strong>Lưu ý quan trọng:</strong></p>
                                    <ul>
                                        <li>Vui lòng có mặt tại bến xe trước giờ khởi hành 30 phút</li>
                                        <li>Mang theo CMND/CCCD để đối chiếu thông tin</li>
                                        <li>Giữ lại email này làm bằng chứng đặt vé</li>
                                        <li>Liên hệ hotline nếu cần hỗ trợ</li>
                                    </ul>
                                </div>
                                <div class="footer">
                                    <p>Chúc bạn có chuyến đi an toàn và vui vẻ!<br><strong>Đội ngũ CK DatVeXe</strong></p>
                                    <p>📧 Email: support@ckdatveexe.com | 📞 Hotline: 1900-xxxx</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                ticket.getPassengerName() != null ? ticket.getPassengerName() : "Quý khách",
                java.time.LocalDateTime.now().format(DATE_FORMATTER),
                ticket.getTicketCode(),
                getRouteInfo(ticket, "departure"),
                getRouteInfo(ticket, "arrival"),
                ticket.getDepartureTime().format(DATE_FORMATTER),
                ticket.getSeat().getSeatNumber(),
                ticket.getSeat().getSeatType().name(),
                ticket.getPrice());
    }

    private String buildCancellationContent(Ticket ticket) {
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
                        .cancel-box { background: #fff3e0; padding: 20px; border-left: 4px solid #ff9800; margin: 20px 0; border-radius: 5px; }
                        .ticket-box { background: #f5f5f5; padding: 20px; border-left: 4px solid #9e9e9e; margin: 20px 0; border-radius: 5px; }
                        .info-row { display: flex; justify-content: space-between; margin: 10px 0; }
                        .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                        .logo { font-size: 24px; font-weight: bold; }
                        .ticket-code { background: #f5f5f5; padding: 10px; border-radius: 5px; font-family: monospace; font-size: 18px; font-weight: bold; text-align: center; margin: 15px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="logo">🚌 CK DatVeXe</div>
                            <h2>❌ Hủy vé thành công</h2>
                        </div>
                        <div class="content">
                            <h3>Kính chào %s,</h3>

                            <div class="cancel-box">
                                <h4>✅ Hủy vé thành công!</h4>
                                <p>Vé của bạn đã được hủy thành công.</p>
                                <p><strong>Thời gian hủy:</strong> %s</p>
                            </div>

                            <div class="ticket-box">
                                <h4>🎫 Thông tin vé đã hủy</h4>
                                <div class="ticket-code">%s</div>
                                <div class="info-row">
                                    <span><strong>Tuyến:</strong></span>
                                    <span>%s → %s</span>
                                </div>
                                <div class="info-row">
                                    <span><strong>Khởi hành:</strong></span>
                                    <span>%s</span>
                                </div>
                                <div class="info-row">
                                    <span><strong>Ghế:</strong></span>
                                    <span>%s (%s)</span>
                                </div>
                                <div class="info-row">
                                    <span><strong>Giá vé:</strong></span>
                                    <span>%,.0f VNĐ</span>
                                </div>
                                <div class="info-row">
                                    <span><strong>Trạng thái:</strong></span>
                                    <span><strong style="color: #ff9800;">Đã hủy</strong></span>
                                </div>
                            </div>

                            <p><strong>Thông tin hoàn tiền:</strong></p>
                            <ul>
                                <li>Nếu vé đã thanh toán, tiền sẽ được hoàn lại trong 3-5 ngày làm việc</li>
                                <li>Nếu vé chưa thanh toán, không có phí phát sinh</li>
                                <li>Ghế đã được giải phóng cho khách hàng khác</li>
                            </ul>

                            <p>Cảm ơn bạn đã sử dụng dịch vụ của <strong>CK DatVeXe</strong>. Chúng tôi hy vọng được phục vụ bạn trong những chuyến đi tiếp theo!</p>
                        </div>
                        <div class="footer">
                            <p>Trân trọng,<br><strong>Đội ngũ CK DatVeXe</strong></p>
                            <p>📧 Email: support@ckdatveexe.com | 📞 Hotline: 1900-xxxx</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                ticket.getPassengerName() != null ? ticket.getPassengerName() : "Quý khách",
                java.time.LocalDateTime.now().format(DATE_FORMATTER),
                ticket.getTicketCode(),
                getRouteInfo(ticket, "departure"),
                getRouteInfo(ticket, "arrival"),
                ticket.getDepartureTime().format(DATE_FORMATTER),
                ticket.getSeat().getSeatNumber(),
                ticket.getSeat().getSeatType().name(),
                ticket.getPrice()
        );
    }

private String buildExpirationContent(Ticket ticket) {
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
                        .expired-box { background: #ffebee; padding: 20px; border-left: 4px solid #f44336; margin: 20px 0; border-radius: 5px; }
                        .ticket-box { background: #f5f5f5; padding: 20px; border-left: 4px solid #9e9e9e; margin: 20px 0; border-radius: 5px; }
                        .retry-box { background: #e3f2fd; padding: 15px; border-left: 4px solid #2196f3; margin: 20px 0; border-radius: 5px; }
                        .info-row { display: flex; justify-content: space-between; margin: 10px 0; }
                        .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                        .logo { font-size: 24px; font-weight: bold; }
                        .ticket-code { background: #f5f5f5; padding: 10px; border-radius: 5px; font-family: monospace; font-size: 18px; font-weight: bold; text-align: center; margin: 15px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="logo">🚌 CK DatVeXe</div>
                            <h2>⏰ Vé hết hạn thanh toán</h2>
                        </div>
                        <div class="content">
                            <h3>Kính chào %s,</h3>

                            <div class="expired-box">
                                <h4>⏰ Vé đã hết hạn thanh toán</h4>
                                <p>Rất tiếc, vé của bạn đã hết thời gian thanh toán và đã bị hủy tự động.</p>
                                <p><strong>Hạn thanh toán:</strong> %s</p>
                                <p><strong>Thời gian hết hạn:</strong> %s</p>
                            </div>

                            <div class="ticket-box">
                                <h4>🎫 Thông tin vé đã hết hạn</h4>
                                <div class="ticket-code">%s</div>
                                <div class="info-row">
                                    <span><strong>Tuyến:</strong></span>
                                    <span>%s → %s</span>
                                </div>
                                <div class="info-row">
                                    <span><strong>Khởi hành:</strong></span>
                                    <span>%s</span>
                                </div>
                                <div class="info-row">
                                    <span><strong>Ghế:</strong></span>
                                    <span>%s (%s)</span>
                                </div>
                                <div class="info-row">
                                    <span><strong>Giá vé:</strong></span>
                                    <span>%,.0f VNĐ</span>
                                </div>
                                <div class="info-row">
                                    <span><strong>Trạng thái:</strong></span>
                                    <span><strong style="color: #f44336;">Hết hạn</strong></span>
                                </div>
                            </div>

                            <div class="retry-box">
                                <h4>🔄 Bạn có thể làm gì tiếp theo?</h4>
                                <ul>
                                    <li><strong>Đặt vé mới</strong> cho cùng chuyến xe (nếu còn ghế trống)</li>
                                    <li><strong>Chọn chuyến khác</strong> phù hợp với lịch trình</li>
                                    <li><strong>Liên hệ hỗ trợ</strong> nếu cần tư vấn</li>
                                </ul>
                            </div>

                            <p><strong>Lưu ý:</strong> Ghế đã được giải phóng và có thể được đặt bởi khách hàng khác. Để tránh tình trạng này, vui lòng thanh toán trong thời hạn quy định (5 phút sau khi đặt vé).</p>

                            <p>Cảm ơn bạn đã quan tâm đến dịch vụ của <strong>CK DatVeXe</strong>. Chúng tôi hy vọng được phục vụ bạn trong những lần đặt vé tiếp theo!</p>
                        </div>
                        <div class="footer">
                            <p>Trân trọng,<br><strong>Đội ngũ CK DatVeXe</strong></p>
                            <p>📧 Email: support@ckdatveexe.com | 📞 Hotline: 1900-xxxx</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                ticket.getPassengerName() != null ? ticket.getPassengerName() : "Quý khách",
                ticket.getPaymentDeadline() != null ? ticket.getPaymentDeadline().format(DATE_FORMATTER) : "Không xác định",
                java.time.LocalDateTime.now().format(DATE_FORMATTER),
                ticket.getTicketCode(),
                getRouteInfo(ticket, "departure"),
                getRouteInfo(ticket, "arrival"),
                ticket.getDepartureTime().format(DATE_FORMATTER),
                ticket.getSeat().getSeatNumber(),
                ticket.getSeat().getSeatType().name(),
                ticket.getPrice()
        );
    }

    private String buildPaymentReminderContent(Ticket ticket, int minutesRemaining) {
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
                        .urgent-box { background: #fff3cd; padding: 20px; border-left: 4px solid #ffc107; margin: 20px 0; border-radius: 5px; }
                        .ticket-box { background: #e3f2fd; padding: 20px; border-left: 4px solid #2196f3; margin: 20px 0; border-radius: 5px; }
                        .countdown { background: #ffebee; padding: 15px; border-left: 4px solid #f44336; margin: 20px 0; border-radius: 5px; text-align: center; }
                        .info-row { display: flex; justify-content: space-between; margin: 10px 0; }
                        .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                        .logo { font-size: 24px; font-weight: bold; }
                        .ticket-code { background: #f5f5f5; padding: 10px; border-radius: 5px; font-family: monospace; font-size: 18px; font-weight: bold; text-align: center; margin: 15px 0; }
                        .time-remaining { font-size: 24px; font-weight: bold; color: #f44336; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="logo">🚌 CK DatVeXe</div>
                            <h2>⏰ Nhắc nhở thanh toán</h2>
                        </div>
                        <div class="content">
                            <h3>Kính chào %s,</h3>

                            <div class="countdown">
                                <h4>⏰ Thời gian còn lại</h4>
                                <div class="time-remaining">%d phút</div>
                                <p>Vé sẽ tự động hủy nếu không thanh toán kịp thời!</p>
                            </div>

                            <div class="urgent-box">
                                <h4>🚨 Cần thanh toán ngay!</h4>
                                <p>Vé của bạn sắp hết hạn thanh toán. Vui lòng hoàn tất thanh toán để giữ vé.</p>
                                <p><strong>Hạn cuối:</strong> %s</p>
                            </div>

                            <div class="ticket-box">
                                <h4>🎫 Thông tin vé cần thanh toán</h4>
                                <div class="ticket-code">%s</div>
                                <div class="info-row">
                                    <span><strong>Tuyến:</strong></span>
                                    <span>%s → %s</span>
                                </div>
                                <div class="info-row">
                                    <span><strong>Khởi hành:</strong></span>
                                    <span>%s</span>
                                </div>
                                <div class="info-row">
                                    <span><strong>Ghế:</strong></span>
                                    <span>%s (%s)</span>
                                </div>
                                <div class="info-row">
                                    <span><strong>Giá vé:</strong></span>
                                    <span><strong>%,.0f VNĐ</strong></span>
                                </div>
                            </div>

                            <p><strong>Cách thanh toán nhanh:</strong></p>
                            <ol>
                                <li><strong>Đăng nhập</strong> vào tài khoản CK DatVeXe</li>
                                <li><strong>Vào mục</strong> "Vé của tôi"</li>
                                <li><strong>Chọn vé</strong> %s</li>
                                <li><strong>Chọn phương thức</strong> thanh toán</li>
                                <li><strong>Hoàn tất</strong> thanh toán ngay!</li>
                            </ol>

                            <p><strong>⚠️ Lưu ý:</strong> Sau khi hết thời gian, vé sẽ tự động hủy và ghế sẽ được giải phóng cho khách hàng khác.</p>
                        </div>
                        <div class="footer">
                            <p>Cần hỗ trợ? Liên hệ ngay!<br><strong>Đội ngũ CK DatVeXe</strong></p>
                            <p>📧 Email: support@ckdatveexe.com | 📞 Hotline: 1900-xxxx</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                ticket.getPassengerName() != null ? ticket.getPassengerName() : "Quý khách",
                minutesRemaining,
                ticket.getPaymentDeadline() != null ? ticket.getPaymentDeadline().format(DATE_FORMATTER) : "Không xác định",
                ticket.getTicketCode(),
                getRouteInfo(ticket, "departure"),
                getRouteInfo(ticket, "arrival"),
                ticket.getDepartureTime().format(DATE_FORMATTER),
                ticket.getSeat().getSeatNumber(),
                ticket.getSeat().getSeatType().name(),
                ticket.getPrice(),
                ticket.getTicketCode()
        );
    }

    private String getRouteInfo(Ticket ticket, String type) {
        try {
            if (ticket.getSchedule() != null && ticket.getSchedule().getRoute() != null) {
                if ("departure".equals(type)) {
                    return ticket.getSchedule().getRoute().getDepartureStation() != null 
                        ? ticket.getSchedule().getRoute().getDepartureStation().getName() 
                        : "Không xác định";
                } else if ("arrival".equals(type)) {
                    return ticket.getSchedule().getRoute().getArrivalStation() != null 
                        ? ticket.getSchedule().getRoute().getArrivalStation().getName() 
                        : "Không xác định";
                }
            }
        } catch (Exception e) {
            log.warn("Error getting route info for ticket: {}", ticket.getTicketCode(), e);
        }
        return "Không xác định";
    }
}