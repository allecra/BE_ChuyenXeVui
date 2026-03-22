package com.example.ckdatveexe.shared.entity;

public enum PaymentStatus {
    PENDING, // Đang chờ thanh toán
    PROCESSING, // Đang xử lý
    COMPLETED, // Thanh toán thành công
    FAILED, // Thanh toán thất bại
    CANCELLED, // Đã hủy
    EXPIRED, // Đã hết hạn
    REFUNDED // Đã hoàn tiền
}