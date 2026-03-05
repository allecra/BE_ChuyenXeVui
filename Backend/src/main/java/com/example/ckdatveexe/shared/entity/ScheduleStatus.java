package com.example.ckdatveexe.shared.entity;

public enum ScheduleStatus {
    ACTIVE, // Lịch trình đang hoạt động
    INACTIVE, // Lịch trình tạm ngưng
    CANCELLED, // Lịch trình đã hủy
    FULL // Lịch trình đã hết chỗ (kept for backward compatibility)
}