package com.example.ckdatveexe.module.payment.dto;

import com.example.ckdatveexe.shared.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa thông tin thanh toán")
public class PaymentResponse {

    @Schema(description = "ID thanh toán", example = "1")
    private Integer id;

    @Schema(description = "Mã giao dịch", example = "PAY_20260210_001")
    private String transactionId;

    @Schema(description = "Mã giao dịch từ nhà cung cấp", example = "MOMO123456789")
    private String providerTransactionId;

    @Schema(description = "Nhà cung cấp thanh toán", example = "MOMO")
    private String provider;

    @Schema(description = "Số tiền (VND)", example = "500000")
    private Double amount;

    @Schema(description = "Đơn vị tiền tệ", example = "VND")
    private String currency;

    @Schema(description = "Trạng thái thanh toán", example = "PENDING")
    private PaymentStatus status;

    @Schema(description = "Mô tả", example = "Thanh toán vé xe khách")
    private String description;

    @Schema(description = "URL QR Code", example = "https://api.momo.vn/qr/123456")
    private String qrCodeUrl;

    @Schema(description = "URL thanh toán", example = "https://payment.momo.vn/pay/123456")
    private String paymentUrl;

    @Schema(description = "Thời gian hết hạn", example = "2026-02-10T08:15:00")
    private LocalDateTime expiredAt;

    @Schema(description = "Thời gian thanh toán", example = "2026-02-10T08:10:00")
    private LocalDateTime paidAt;

    @Schema(description = "Thời gian tạo", example = "2026-02-10T08:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật", example = "2026-02-10T08:10:00")
    private LocalDateTime updatedAt;

    @Schema(description = "ID người dùng", example = "1")
    private Integer userId;

    @Schema(description = "ID vé", example = "1")
    private Integer ticketId;

    // Static factory methods
    public static PaymentResponse fromEntity(com.example.ckdatveexe.shared.entity.Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setTransactionId(payment.getTransactionId());
        response.setProviderTransactionId(payment.getProviderTransactionId());
        response.setProvider(payment.getPaymentProvider().getProviderName());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus());
        response.setDescription(payment.getDescription());
        response.setQrCodeUrl(payment.getQrCodeUrl());
        response.setPaymentUrl(payment.getPaymentUrl());
        response.setExpiredAt(payment.getExpiredAt());
        response.setPaidAt(payment.getPaidAt());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        response.setUserId(payment.getUser().getId());
        response.setTicketId(payment.getTicket() != null ? payment.getTicket().getId() : null);
        return response;
    }
}