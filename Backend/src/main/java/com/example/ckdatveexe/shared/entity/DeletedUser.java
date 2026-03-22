package com.example.ckdatveexe.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "deleted_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeletedUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "original_user_id", nullable = false)
    private Integer originalUserId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "id_card", length = 20)
    private String idCard;

    @Column(name = "bus_company_name")
    private String busCompanyName;

    @Column(name = "delete_reason", nullable = false)
    private String deleteReason;

    @Column(name = "delete_notes")
    private String deleteNotes;

    @Column(name = "deleted_by_admin_id", nullable = false)
    private Integer deletedByAdminId;

    @Column(name = "deleted_by_admin_name", nullable = false)
    private String deletedByAdminName;

    @CreationTimestamp
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "original_created_at")
    private LocalDateTime originalCreatedAt;

    public static DeletedUser fromUser(User user, Integer adminId, String adminName, String reason, String notes) {
        DeletedUser deletedUser = new DeletedUser();
        deletedUser.setOriginalUserId(user.getId());
        deletedUser.setFirstName(user.getFirstName());
        deletedUser.setLastName(user.getLastName());
        deletedUser.setEmail(user.getEmail());
        deletedUser.setPhone(user.getPhone());
        deletedUser.setIdCard(user.getIdCard());

        if (user.getBusCompany() != null) {
            deletedUser.setBusCompanyName(user.getBusCompany().getCompanyName());
        }

        deletedUser.setDeleteReason(reason);
        deletedUser.setDeleteNotes(notes);
        deletedUser.setDeletedByAdminId(adminId);
        deletedUser.setDeletedByAdminName(adminName);
        deletedUser.setOriginalCreatedAt(user.getCreatedAt());

        return deletedUser;
    }
}