package com.example.ckdatveexe.shared.entity;

public enum SeatStatus {
    AVAILABLE, // Ghế trống, có thể đặt
    LOCKED, // Ghế đang được giữ (10 phút)
    BOOKED, // Ghế đã được đặt và thanh toán
    MAINTENANCE, // Ghế đang bảo trì
    DELETED // Ghế đã bị xóa mềm
}