package com.example.ckdatveexe.module.buscompany.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.auth.service.EmailService;
import com.example.ckdatveexe.module.buscompany.dto.*;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusCompanyService {

    private final BusCompanyRepository busCompanyRepository;
    private final BusCompanyRegistrationRepository registrationRepository;
    private final BusRepository busRepository;
    private final BusReviewRepository busReviewRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // USER APIs
    public Page<BusCompanyResponse> getAllBusCompanies(Pageable pageable) {
        Page<BusCompany> companies = busCompanyRepository.findAll(pageable);
        return companies.map(this::convertToResponse);
    }

    public BusCompanyResponse getBusCompanyDetail(Integer id) {
        BusCompany company = busCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + id));
        return convertToResponse(company);
    }

    // COMPANY REGISTRATION APIs
    // @Transactional - Tạm thời comment để test
    public BusCompanyRegistrationResponse registerBusCompany(BusCompanyRegistrationRequest request) {
        // Log request
        System.out.println("=== REGISTER BUS COMPANY ===");
        System.out.println("Company Name: " + request.getCompanyName());
        System.out.println("Email: " + request.getEmail());
        System.out.println("Phone: " + request.getPhoneNumber());

        // Kiểm tra email đã tồn tại
        if (registrationRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng để đăng ký");
        }

        BusCompanyRegistration registration = new BusCompanyRegistration();
        registration.setCompanyName(request.getCompanyName());
        registration.setEmail(request.getEmail());
        registration.setPhoneNumber(request.getPhoneNumber());
        registration.setImage(request.getImage());
        registration.setDescriptions(request.getDescriptions());
        registration.setBusinessLicense(request.getBusinessLicense());
        registration.setAddress(request.getAddress());
        registration.setStatus(RegistrationStatus.PENDING);

        System.out.println("Before save - Registration ID: " + registration.getId());
        BusCompanyRegistration saved = registrationRepository.save(registration);
        System.out.println("After save - Registration ID: " + saved.getId());

        // Gửi email xác nhận đăng ký
        try {
            emailService.sendRegistrationConfirmationEmail(
                    request.getEmail(),
                    request.getCompanyName());
            System.out.println("Email sent successfully");
        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }

        return convertToRegistrationResponse(saved);
    }

    public BusCompanyRegistrationResponse getRegistrationStatus(String email) {
        BusCompanyRegistration registration = registrationRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đăng ký với email: " + email));
        return convertToRegistrationResponse(registration);
    }

    // COMPANY MANAGEMENT APIs (sau khi được duyệt)
    @Transactional
    public BusCompanyResponse updateBusCompany(Integer companyId, BusCompanyUpdateRequest request) {
        BusCompany company = busCompanyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + companyId));

        if (request.getCompanyName() != null) {
            company.setCompanyName(request.getCompanyName());
        }
        if (request.getImage() != null) {
            company.setImage(request.getImage());
        }
        if (request.getDescriptions() != null) {
            company.setDescriptions(request.getDescriptions());
        }

        BusCompany updated = busCompanyRepository.save(company);
        return convertToResponse(updated);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        // Tìm user theo email của company
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }

        // Kiểm tra mật khẩu mới và xác nhận
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        // Cập nhật mật khẩu
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Gửi email thông báo đổi mật khẩu
        emailService.sendPasswordChangeNotification(email, user.getFirstName() + " " + user.getLastName());
    }

    public Page<BusCompanyResponse> getActiveBuses(Integer companyId, Pageable pageable) {
        // Lấy danh sách xe đang hoạt động của nhà xe
        Page<Bus> buses = busRepository.findByCompanyIdAndStatus(companyId, BusStatus.ACTIVE, pageable);

        // Convert sang response (có thể tạo BusResponse riêng nếu cần)
        List<BusCompanyResponse> responses = buses.getContent().stream()
                .map(bus -> {
                    BusCompanyResponse response = new BusCompanyResponse();
                    response.setId(bus.getId());
                    response.setCompanyName(bus.getCompany().getCompanyName());
                    // Thêm các thông tin khác của bus nếu cần
                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, buses.getTotalElements());
    }

    // ADMIN APIs
    @Transactional
    public BusCompanyRegistrationResponse approveRegistration(Integer registrationId, ApprovalRequest request,
            Integer adminId) {
        BusCompanyRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy đơn đăng ký với ID: " + registrationId));

        registration.setStatus(request.getStatus());
        registration.setAdminNotes(request.getAdminNotes());
        registration.setApprovedBy(adminId);
        registration.setApprovedAt(LocalDateTime.now());

        BusCompanyRegistration updated = registrationRepository.save(registration);

        // Nếu được duyệt, tạo BusCompany và User account
        if (request.getStatus() == RegistrationStatus.APPROVED) {
            createBusCompanyAndUser(registration);
            emailService.sendApprovalEmail(registration.getEmail(), registration.getCompanyName(), true,
                    request.getAdminNotes());
        } else if (request.getStatus() == RegistrationStatus.REJECTED) {
            emailService.sendApprovalEmail(registration.getEmail(), registration.getCompanyName(), false,
                    request.getAdminNotes());
        }

        return convertToRegistrationResponse(updated);
    }

    public Page<BusCompanyRegistrationResponse> getAllRegistrations(Pageable pageable) {
        Page<BusCompanyRegistration> registrations = registrationRepository.findAll(pageable);
        return registrations.map(this::convertToRegistrationResponse);
    }

    public Page<BusCompanyResponse> searchBusCompanies(String keyword, Pageable pageable) {
        Page<BusCompany> companies = busCompanyRepository.findByCompanyNameContainingIgnoreCase(keyword, pageable);
        return companies.map(this::convertToResponse);
    }

    @Transactional
    public void deleteBusCompany(Integer id) {
        BusCompany company = busCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + id));
        busCompanyRepository.delete(company);
    }

    // Helper methods
    private BusCompanyResponse convertToResponse(BusCompany company) {
        BusCompanyResponse response = new BusCompanyResponse();
        response.setId(company.getId());
        response.setCompanyName(company.getCompanyName());
        response.setImage(company.getImage());
        response.setDescriptions(company.getDescriptions());
        response.setCreatedAt(company.getCreatedAt());
        response.setUpdatedAt(company.getUpdatedAt());

        // Tính toán thống kê
        response.setTotalBuses((int) busRepository.countByCompanyIdAndStatus(company.getId(), BusStatus.ACTIVE));

        Double avgRating = busReviewRepository.getAverageRatingByCompanyId(company.getId());
        response.setAverageRating(avgRating != null ? avgRating : 0.0);

        response.setTotalReviews(busReviewRepository.countByBusCompanyId(company.getId()));

        return response;
    }

    private BusCompanyRegistrationResponse convertToRegistrationResponse(BusCompanyRegistration registration) {
        BusCompanyRegistrationResponse response = new BusCompanyRegistrationResponse();
        response.setId(registration.getId());
        response.setCompanyName(registration.getCompanyName());
        response.setEmail(registration.getEmail());
        response.setPhoneNumber(registration.getPhoneNumber());
        response.setImage(registration.getImage());
        response.setDescriptions(registration.getDescriptions());
        response.setBusinessLicense(registration.getBusinessLicense());
        response.setAddress(registration.getAddress());
        response.setStatus(registration.getStatus());
        response.setAdminNotes(registration.getAdminNotes());
        response.setApprovedBy(registration.getApprovedBy());
        response.setApprovedAt(registration.getApprovedAt());
        response.setCreatedAt(registration.getCreatedAt());
        response.setUpdatedAt(registration.getUpdatedAt());
        return response;
    }

    @Transactional
    private void createBusCompanyAndUser(BusCompanyRegistration registration) {
        // Tạo BusCompany
        BusCompany company = new BusCompany();
        company.setCompanyName(registration.getCompanyName());
        company.setImage(registration.getImage());
        company.setDescriptions(registration.getDescriptions());
        busCompanyRepository.save(company);

        // Tạo User account cho company (nếu chưa có)
        if (!userRepository.existsByEmail(registration.getEmail())) {
            User user = new User();
            user.setEmail(registration.getEmail());
            user.setFirstName(registration.getCompanyName());
            user.setLastName("Company");
            user.setPhone(registration.getPhoneNumber());

            // Tìm role ROLE_BUS_COMPANY
            Role companyRole = roleRepository.findByRoleName(RoleName.ROLE_BUS_COMPANY)
                    .orElseThrow(() -> new RuntimeException("Role ROLE_BUS_COMPANY not found"));
            user.setRoles(Set.of(companyRole));

            user.setPassword(passwordEncoder.encode("defaultPassword123")); // Mật khẩu tạm thời
            user.setBusCompany(company);
            userRepository.save(user);

            // Gửi email với thông tin đăng nhập
            emailService.sendCompanyAccountCreatedEmail(
                    registration.getEmail(),
                    registration.getCompanyName(),
                    "defaultPassword123");
        }
    }
}