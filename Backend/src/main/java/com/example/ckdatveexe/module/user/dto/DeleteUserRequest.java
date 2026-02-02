// module.user.dto.DeleteUserRequest.java
package com.example.ckdatveexe.module.user.dto;

import lombok.Data;

@Data
public class DeleteUserRequest {
    private String deletionReason;
    private boolean hardDelete = false;
}