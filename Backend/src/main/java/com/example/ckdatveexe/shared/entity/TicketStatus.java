package com.example.ckdatveexe.shared.entity;

public enum TicketStatus {
    PENDING, // Vé đang chờ thanh toán (ghế đã lock)
    CONFIRMED, // Vé đã thanh toán thành công
    CANCELLED, // Vé đã bị hủy
    EXPIRED, // Vé hết hạn (không thanh toán trong 10 phút)
    BOOKED // Deprecated - use CONFIRMED instead
}