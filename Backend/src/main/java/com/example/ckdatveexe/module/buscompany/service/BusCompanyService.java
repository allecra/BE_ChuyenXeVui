package com.example.ckdatveexe.module.buscompany.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.auth.service.EmailService;
import com.example.ckdatveexe.module.buscompany.dto.*;
import com.example.ckdatveexe.module.media.service.CloudinaryService;
import com.example.ckdatveexe.shared.entity.BusCompany;
import com.example.ckdatveexe.shared.entity.BusCompanyRegistration;
import com.example.ckdatveexe.shared.entity.RegistrationStatus;
import com.example.ckdatveexe.shared.repository.BusCompanyRegistrationRepository;
import com.example.ckdatveexe.shared.repository.BusCompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusCompanyService {

    private final BusCompanyRepository busCompanyRepository;
    private final BusCompanyRegistrationRepository registrationRepository;
    private final EmailService emailService;
    private final CloudinaryService cloudinaryService;

    // CRUD Operations for BusCompany
    public Page<BusCompanyResponse> getAllBusCompanies(String companyName, Pageable pageable) {
        return busCompanyRepository.findByFilters(companyName, pageable)
                .map(this::convertToResponse);
    }

    public Page<BusCompanyResponse> searchBusCompanies(String searchTerm, Pageable pageable) {
        return busCompanyRepository.searchByIdOrName(searchTerm, pageable)
                .map(this::convertToResponse);
    }

    public BusCompanyResponse getBusCompanyById(Integer id) {
        BusCompany busCompany = busCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + id));
        return convertToResponse(busCompany);
    }

    @Transactional
    public BusCompanyResponse createBusCompany(BusCompanyCreateRequest request) {
        if (busCompanyRepository.existsByCompanyNameIgnoreCase(request.getCompanyName())) {
            throw new IllegalArgumentException("Tên công ty đã tồn tại");
        }

        BusCompany busCompany = new BusCompany();
        busCompany.setCompanyName(request.getCompanyName());
        busCompany.setImage(request.getImage());
        busCompany.setDescriptions(request.getDescriptions());

        BusCompany savedCompany = busCompanyRepository.save(busCompany);
        return convertToResponse(savedCompany);
    }

    @Transactional
    public BusCompanyResponse updateBusCompany(Integer id, BusCompanyUpdateRequest request) {
        BusCompany busCompany = busCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + id));

        if (request.getCompanyName() != null && !request.getCompanyName().isEmpty()) {
            if (!busCompany.getCompanyName().equalsIgnoreCase(request.getCompanyName()) &&
                    busCompanyRepository.existsByCompanyNameIgnoreCase(request.getCompanyName())) {
                throw new IllegalArgumentException("Tên công ty đã tồn tại");
            }
            busCompany.setCompanyName(request.getCompanyName());
        }

        if (request.getImage() != null) {
            busCompany.setImage(request.getImage());
        }

        if (request.getDescriptions() != null) {
            busCompany.setDescriptions(request.getDescriptions());
        }

        BusCompany updatedCompany = busCompanyRepository.save(busCompany);
        return convertToResponse(updatedCompany);
    }

    @Transactional
    public void deleteBusCompany(Integer id) {
        if (!busCompanyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + id);
        }
        busCompanyRepository.deleteById(id);
    }

    @Transactional
    public String uploadBusCompanyImage(Integer id, MultipartFile file) {
        BusCompany busCompany = busCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + id));

        // Upload ảnh lên Cloudinary với folder riêng cho bus companies
        Map<String, Object> uploadResult = cloudinaryService.upload(file, "bus-companies");
        String imageUrl = (String) uploadResult.get("secure_url");

        // Cập nhật URL ảnh vào database
        busCompany.setImage(imageUrl);
        busCompanyRepository.save(busCompany);

        log.info("Uploaded image for bus company ID {}: {}", id, imageUrl);
        return imageUrl;
    }

    public String uploadImageForRegistration(MultipartFile file) {
        // Upload ảnh lên Cloudinary với folder riêng cho registrations
        Map<String, Object> uploadResult = cloudinaryService.upload(file, "bus-company-registrations");
        String imageUrl = (String) uploadResult.get("secure_url");

        log.info("Uploaded image for registration: {}", imageUrl);
        return imageUrl;
    }

    // Registration Operations
    @Transactional
    public void registerBusCompany(BusCompanyRegistrationRequest request) {
        if (registrationRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng để đăng ký");
        }

        if (registrationRepository.existsByCompanyNameIgnoreCase(request.getCompanyName())) {
            throw new IllegalArgumentException("Tên công ty đã được đăng ký");
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

        registrationRepository.save(registration);

        // Gửi email xác nhận đăng ký
        try {
            emailService.sendRegistrationConfirmation(request.getEmail(), request.getCompanyName());
        } catch (Exception e) {
            log.error("Lỗi khi gửi email xác nhận đăng ký: ", e);
        }
    }

    public Page<BusCompanyRegistration> getAllRegistrations(RegistrationStatus status, String companyName,
            Pageable pageable) {
        return registrationRepository.findByFilters(status, companyName, pageable);
    }

    public Page<BusCompanyRegistration> searchRegistrations(RegistrationStatus status, String searchTerm,
            Pageable pageable) {
        return registrationRepository.searchByIdOrName(status, searchTerm, pageable);
    }

    public BusCompanyRegistration getRegistrationById(Integer id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký với ID: " + id));
    }

    @Transactional
    public void approveRegistration(Integer registrationId, Integer adminId, String adminNotes) {
        BusCompanyRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký với ID: " + registrationId));

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new IllegalArgumentException("Đăng ký này đã được xử lý");
        }

        // Tạo BusCompany từ registration
        BusCompany busCompany = new BusCompany();
        busCompany.setCompanyName(registration.getCompanyName());
        busCompany.setImage(registration.getImage());
        busCompany.setDescriptions(registration.getDescriptions());

        busCompanyRepository.save(busCompany);

        // Cập nhật trạng thái registration
        registration.setStatus(RegistrationStatus.APPROVED);
        registration.setApprovedBy(adminId);
        registration.setApprovedAt(LocalDateTime.now());
        registration.setAdminNotes(adminNotes);

        registrationRepository.save(registration);

        // Gửi email thông báo duyệt
        try {
            emailService.sendApprovalNotification(registration.getEmail(), registration.getCompanyName());
        } catch (Exception e) {
            log.error("Lỗi khi gửi email thông báo duyệt: ", e);
        }
    }

    @Transactional
    public void rejectRegistration(Integer registrationId, Integer adminId, String adminNotes) {
        BusCompanyRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký với ID: " + registrationId));

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new IllegalArgumentException("Đăng ký này đã được xử lý");
        }

        registration.setStatus(RegistrationStatus.REJECTED);
        registration.setApprovedBy(adminId);
        registration.setApprovedAt(LocalDateTime.now());
        registration.setAdminNotes(adminNotes);

        registrationRepository.save(registration);

        // Gửi email thông báo từ chối
        try {
            emailService.sendRejectionNotification(registration.getEmail(), registration.getCompanyName(), adminNotes);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email thông báo từ chối: ", e);
        }
    }

    private BusCompanyResponse convertToResponse(BusCompany busCompany) {
        BusCompanyResponse response = new BusCompanyResponse();
        response.setId(busCompany.getId());
        response.setCompanyName(busCompany.getCompanyName());
        response.setImage(busCompany.getImage());
        response.setDescriptions(busCompany.getDescriptions());
        response.setCreatedAt(busCompany.getCreatedAt());
        response.setUpdatedAt(busCompany.getUpdatedAt());
        return response;
    }
}