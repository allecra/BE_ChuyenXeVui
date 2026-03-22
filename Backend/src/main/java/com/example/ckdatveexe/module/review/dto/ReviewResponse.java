package com.example.ckdatveexe.module.review.dto;

import com.example.ckdatveexe.shared.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Integer id;
    private Integer rating;
    private String comment;
    private String status;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;

    // User info
    private Integer userId;
    private String userName;
    private String userEmail;

    // Ticket info
    private Integer ticketId;
    private String ticketCode;

    // Route info
    private Integer routeId;
    private String routeName;
    private String departureLocation;
    private String arrivalLocation;

    // Company info
    private Integer busCompanyId;
    private String busCompanyName;

    // Admin info
    private Integer reviewedByAdminId;
    private String reviewedByAdminName;

    public static ReviewResponse fromEntity(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setStatus(review.getStatus().name());
        response.setAdminNotes(review.getAdminNotes());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        response.setReviewedAt(review.getReviewedAt());

        // Set user info
        if (review.getUser() != null) {
            response.setUserId(review.getUser().getId());
            response.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
            response.setUserEmail(review.getUser().getEmail());
        }

        // Set ticket info
        if (review.getTicket() != null) {
            response.setTicketId(review.getTicket().getId());
            response.setTicketCode(review.getTicket().getTicketCode());
        }

        // Set route info
        if (review.getRoute() != null) {
            response.setRouteId(review.getRoute().getId());
            response.setRouteName(
                    review.getRoute().getDepartureLocation() + " - " + review.getRoute().getArrivalLocation());
            response.setDepartureLocation(review.getRoute().getDepartureLocation());
            response.setArrivalLocation(review.getRoute().getArrivalLocation());
        }

        // Set company info
        if (review.getBusCompany() != null) {
            response.setBusCompanyId(review.getBusCompany().getId());
            response.setBusCompanyName(review.getBusCompany().getCompanyName());
        }

        // Set admin info
        if (review.getReviewedByAdmin() != null) {
            response.setReviewedByAdminId(review.getReviewedByAdmin().getId());
            response.setReviewedByAdminName(review.getReviewedByAdmin().getFirstName() + " " +
                    review.getReviewedByAdmin().getLastName());
        }

        return response;
    }
}