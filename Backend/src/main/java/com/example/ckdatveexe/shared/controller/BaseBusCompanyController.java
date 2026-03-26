package com.example.ckdatveexe.shared.controller;

import com.example.ckdatveexe.config.UserDetailsImpl;
import com.example.ckdatveexe.shared.entity.User;
import org.springframework.security.core.Authentication;

/**
 * Base controller for Bus Company operations
 * Provides common utility methods for authentication and authorization
 */
public abstract class BaseBusCompanyController {

    /**
     * Extract bus company ID from authentication
     * 
     * @param authentication Spring Security Authentication object
     * @return Bus company ID
     * @throws IllegalArgumentException if user is not associated with any bus
     *                                  company
     */
    protected Integer getBusCompanyIdFromAuth(Authentication authentication) {
        if (authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            User user = userDetails.getUser();
            if (user.getBusCompany() != null) {
                return user.getBusCompany().getId();
            } else {
                throw new IllegalArgumentException("Người dùng không thuộc về nhà xe nào");
            }
        }
        throw new IllegalArgumentException("Không thể xác định thông tin người dùng");
    }

    /**
     * Extract user ID from authentication
     * 
     * @param authentication Spring Security Authentication object
     * @return User ID
     * @throws IllegalArgumentException if user details cannot be extracted
     */
    protected Integer getUserIdFromAuth(Authentication authentication) {
        if (authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails.getUser().getId();
        }
        throw new IllegalArgumentException("Không thể xác định thông tin người dùng");
    }

    /**
     * Extract user details from authentication
     * 
     * @param authentication Spring Security Authentication object
     * @return UserDetailsImpl object
     * @throws IllegalArgumentException if user details cannot be extracted
     */
    protected UserDetailsImpl getUserDetailsFromAuth(Authentication authentication) {
        if (authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails;
        }
        throw new IllegalArgumentException("Không thể xác định thông tin người dùng");
    }
}