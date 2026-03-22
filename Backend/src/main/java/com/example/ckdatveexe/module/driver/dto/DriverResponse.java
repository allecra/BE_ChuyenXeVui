package com.example.ckdatveexe.module.driver.dto;

import com.example.ckdatveexe.shared.entity.Driver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {
    private Integer id;
    private String fullName;
    private String phone;
    private String email;
    private String licenseNumber;
    private String licenseType;
    private LocalDate licenseExpiryDate;
    private LocalDate dateOfBirth;
    private String address;
    private String status;
    private Integer totalTrips;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Current bus info
    private Integer currentBusId;
    private String currentBusLicensePlate;
    private String currentBusType;

    public static DriverResponse fromEntity(Driver driver) {
        DriverResponse response = new DriverResponse();
        response.setId(driver.getId());
        response.setFullName(driver.getFullName());
        response.setPhone(driver.getPhone());
        response.setEmail(driver.getEmail());
        response.setLicenseNumber(driver.getLicenseNumber());
        response.setLicenseType(driver.getLicenseType());
        response.setLicenseExpiryDate(driver.getLicenseExpiryDate());
        response.setDateOfBirth(driver.getDateOfBirth());
        response.setAddress(driver.getAddress());
        response.setStatus(driver.getStatus().name());
        response.setTotalTrips(driver.getTotalTrips());
        response.setNotes(driver.getNotes());
        response.setCreatedAt(driver.getCreatedAt());
        response.setUpdatedAt(driver.getUpdatedAt());

        // Set current bus info if exists
        if (driver.getCurrentBus() != null) {
            response.setCurrentBusId(driver.getCurrentBus().getId());
            response.setCurrentBusLicensePlate(driver.getCurrentBus().getLicensePlate());
            response.setCurrentBusType(driver.getCurrentBus().getBusType().name());
        }

        return response;
    }
}