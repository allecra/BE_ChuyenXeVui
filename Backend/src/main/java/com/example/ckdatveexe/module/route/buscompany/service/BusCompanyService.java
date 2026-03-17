package com.example.ckdatveexe.module.route.buscompany.service;

import com.example.ckdatveexe.exception.ResourceNotFoundException;
import com.example.ckdatveexe.module.auth.service.EmailService;
import com.example.ckdatveexe.module.bus.dto.BusResponse;
import com.example.ckdatveexe.module.bus.dto.BusSearchRequest;
import com.example.ckdatveexe.module.bus.dto.SeatResponse;
import com.example.ckdatveexe.module.media.service.CloudinaryService;
import com.example.ckdatveexe.module.route.buscompany.dto.*;
import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusCompanyService {

    private final BusCompanyRepository busCompanyRepository;
    private final BusCompanyRegistrationRepository registrationRepository;
    private final EmailService emailService;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final BusRepository busRepository;
    private final SeatRepository seatRepository;

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

    public String getRegistrationStatusByEmail(String email) {
        BusCompanyRegistration registration = registrationRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký với email: " + email));
        return registration.getStatus().name();
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

        BusCompany savedBusCompany = busCompanyRepository.save(busCompany);

        // Tạo tài khoản User cho nhà xe
        String temporaryPassword = generateTemporaryPassword();
        createBusCompanyUser(registration, savedBusCompany, temporaryPassword);

        // Cập nhật trạng thái registration
        registration.setStatus(RegistrationStatus.APPROVED);
        registration.setApprovedBy(adminId);
        registration.setApprovedAt(LocalDateTime.now());
        registration.setAdminNotes(adminNotes);

        registrationRepository.save(registration);

        // Gửi email thông báo duyệt kèm thông tin đăng nhập
        try {
            emailService.sendApprovalNotificationWithAccount(
                    registration.getEmail(),
                    registration.getCompanyName(),
                    registration.getEmail(), // email làm username
                    temporaryPassword);
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

    private User createBusCompanyUser(BusCompanyRegistration registration, BusCompany busCompany,
            String temporaryPassword) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.findByEmail(registration.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã được sử dụng cho tài khoản khác");
        }

        // Lấy role BUS_COMPANY
        Role busCompanyRole = roleRepository.findByRoleName(RoleName.ROLE_BUS_COMPANY)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role BUS_COMPANY"));

        // Tạo User mới
        User user = new User();
        user.setEmail(registration.getEmail());
        user.setPassword(passwordEncoder.encode(temporaryPassword));

        // Tách tên từ company name hoặc email
        String[] nameParts = extractNameFromCompanyOrEmail(registration.getCompanyName(), registration.getEmail());
        user.setFirstName(nameParts[0]);
        user.setLastName(nameParts[1]);

        user.setPhone(normalizePhoneNumber(registration.getPhoneNumber()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(busCompanyRole));
        user.setBusCompany(busCompany);

        User savedUser = userRepository.save(user);
        log.info("Tạo tài khoản User cho nhà xe: {} với email: {}", busCompany.getCompanyName(),
                registration.getEmail());

        return savedUser;
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    private String[] extractNameFromCompanyOrEmail(String companyName, String email) {
        // Ưu tiên lấy từ tên công ty
        if (companyName != null && !companyName.trim().isEmpty()) {
            String cleanName = companyName.replaceAll("(?i)(nhà xe|công ty|cty|co\\.|ltd|limited)", "").trim();
            if (cleanName.contains(" ")) {
                String[] parts = cleanName.split("\\s+", 2);
                return new String[] { parts[0], parts[1] };
            } else {
                return new String[] { cleanName, "Company" };
            }
        }

        // Fallback: lấy từ email
        String emailPrefix = email.split("@")[0];
        return new String[] { emailPrefix, "User" };
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        // Loại bỏ khoảng trắng, dấu gạch ngang và các ký tự không phải số (giữ lại dấu
        // +)
        String normalized = phoneNumber.replaceAll("[^0-9+]", "");

        // Giới hạn độ dài tối đa 20 ký tự
        if (normalized.length() > 20) {
            normalized = normalized.substring(0, 20);
        }

        return normalized;
    }

    @Transactional
    public void resetBusCompanyPassword(String email) {
        // Tìm user theo email và role BUS_COMPANY
        User user = userRepository.findByEmail(email).orElse(null);

        // Nếu không tìm thấy user, kiểm tra trong bảng registration
        if (user == null) {
            BusCompanyRegistration registration = registrationRepository.findByEmailIgnoreCase(email).orElse(null);
            if (registration != null && registration.getStatus() == RegistrationStatus.APPROVED) {
                throw new IllegalArgumentException(
                        "Email này đã được duyệt nhưng chưa có tài khoản. Vui lòng liên hệ admin để tạo tài khoản.");
            } else if (registration != null) {
                throw new IllegalArgumentException(
                        "Email này đang trong quá trình đăng ký (trạng thái: " + registration.getStatus() + ")");
            } else {
                throw new ResourceNotFoundException("Không tìm thấy email này trong hệ thống");
            }
        }

        // Kiểm tra user có role BUS_COMPANY không
        boolean isBusCompany = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_BUS_COMPANY);

        if (!isBusCompany) {
            throw new IllegalArgumentException("Tài khoản này không phải là tài khoản nhà xe");
        }

        // Tạo mật khẩu mới
        String newPassword = generateTemporaryPassword();

        // Cập nhật mật khẩu
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // TODO: Tạo bản ghi password reset để tracking (tạm thời comment out)
        /*
         * try {
         * PasswordReset passwordReset = new PasswordReset();
         * passwordReset.setEmail(email);
         * passwordReset.setOtp(newPassword); // Lưu mật khẩu mới (chưa mã hóa để gửi
         * email)
         * passwordReset.setExpiresAt(java.time.LocalDateTime.now().plusHours(24)); //
         * Hết hạn sau 24h
         * passwordResetRepository.save(passwordReset);
         * } catch (Exception e) {
         * log.warn("Could not save password reset record: {}", e.getMessage());
         * // Không throw exception vì mật khẩu đã được reset thành công
         * }
         */

        // Gửi email thông báo mật khẩu mới
        try {
            String companyName = user.getBusCompany() != null ? user.getBusCompany().getCompanyName() : "Nhà xe";
            emailService.sendPasswordResetNotification(email, companyName, newPassword);
            log.info("Password reset successful for bus company email: {}", email);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email reset mật khẩu: ", e);
            throw new RuntimeException("Đã reset mật khẩu nhưng không thể gửi email thông báo");
        }
    }

    @Transactional
    public void changeBusCompanyPassword(Integer userId, ChangePasswordRequest request) {
        // Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        // Tìm user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        // Kiểm tra user có role BUS_COMPANY không
        boolean isBusCompany = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_BUS_COMPANY);

        if (!isBusCompany) {
            throw new IllegalArgumentException("Tài khoản này không phải là tài khoản nhà xe");
        }

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Gửi email thông báo đổi mật khẩu thành công
        try {
            String companyName = user.getBusCompany() != null ? user.getBusCompany().getCompanyName() : "Nhà xe";
            emailService.sendPasswordChangeNotification(user.getEmail(), companyName);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email thông báo đổi mật khẩu: ", e);
            // Không throw exception vì mật khẩu đã được đổi thành công
        }
    }

    public BusCompanyResponse getBusCompanyByUserId(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (user.getBusCompany() == null) {
            throw new IllegalArgumentException("Người dùng này không thuộc về nhà xe nào");
        }

        return convertToResponse(user.getBusCompany());
    }

    public BusCompanyResponse getBusCompanyByBusId(Integer busId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + busId));

        return convertToResponse(bus.getCompany());
    }

    public Page<BusResponse> getMyCompanyBuses(Integer companyId, BusSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Bus> buses;

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            buses = busRepository.searchBusesByCompany(
                    companyId,
                    request.getKeyword().trim(),
                    request.getStatus() != null ? request.getStatus() : BusStatus.ACTIVE,
                    pageable);
        } else {
            if (request.getStatus() != null) {
                buses = busRepository.findByCompanyIdAndStatus(companyId, request.getStatus(), pageable);
            } else {
                buses = busRepository.findByCompanyId(companyId, pageable);
            }
        }

        return buses.map(this::convertToBusResponse);
    }

    public BusResponse getMyCompanyBusDetail(Integer companyId, Integer busId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + busId));

        if (!bus.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Xe không thuộc về nhà xe này");
        }

        return convertToBusResponseWithSeats(bus);
    }

    private Pageable createPageable(BusSearchRequest request) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy());
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private BusResponse convertToBusResponse(Bus bus) {
        BusResponse response = new BusResponse();
        response.setId(bus.getId());
        response.setName(bus.getName());
        response.setDescriptions(bus.getDescriptions());
        response.setLicensePlate(bus.getLicensePlate());
        response.setCapacity(bus.getCapacity());
        response.setBusType(bus.getBusType());
        response.setStatus(bus.getStatus());
        response.setCompanyId(bus.getCompany().getId());
        response.setCompanyName(bus.getCompany().getCompanyName());
        response.setCreatedAt(bus.getCreatedAt());
        response.setUpdatedAt(bus.getUpdatedAt());

        // Thông tin ghế cơ bản
        List<Seat> seats = seatRepository.findByBusIdOrderByRowNumberAscColumnNumberAsc(bus.getId());
        response.setTotalSeats(seats.size());
        response.setAvailableSeats((int) seats.stream().filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count());

        return response;
    }

    private BusResponse convertToBusResponseWithSeats(Bus bus) {
        BusResponse response = convertToBusResponse(bus);

        // Thông tin chi tiết ghế
        List<Seat> seats = seatRepository.findByBusIdOrderByRowNumberAscColumnNumberAsc(bus.getId());
        response.setSeats(
                seats.stream().map(this::convertToSeatResponse).collect(java.util.stream.Collectors.toList()));

        return response;
    }

    private SeatResponse convertToSeatResponse(Seat seat) {
        SeatResponse response = new SeatResponse();
        response.setId(seat.getId());
        response.setSeatNumber(seat.getSeatNumber());
        response.setSeatType(seat.getSeatType());
        response.setStatus(seat.getStatus());
        response.setPriceForSeatType(seat.getPriceForSeatType());
        response.setRowNumber(seat.getRowNumber());
        response.setColumnNumber(seat.getColumnNumber());
        return response;
    }

    // New methods for BusCompanyAdminController

    public Page<BusCompanyResponse> getAllBusCompanies(BusSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<BusCompany> companies;

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            companies = busCompanyRepository.searchByIdOrName(request.getKeyword().trim(), pageable);
        } else {
            companies = busCompanyRepository.findAll(pageable);
        }

        return companies.map(this::convertToResponse);
    }

    @Transactional
    public BusCompanyResponse updateBusCompanyByAdmin(Integer companyId, BusCompanyUpdateRequest request) {
        BusCompany busCompany = busCompanyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + companyId));

        // Update company info
        if (request.getCompanyName() != null && !request.getCompanyName().trim().isEmpty()) {
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

        // Send email notification to company
        try {
            // Find user associated with this company
            User companyUser = userRepository.findByBusCompanyId(companyId)
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (companyUser != null) {
                emailService.sendCompanyUpdateNotification(
                        companyUser.getEmail(),
                        busCompany.getCompanyName(),
                        "Thông tin nhà xe của bạn đã được cập nhật bởi quản trị viên");
            }
        } catch (Exception e) {
            log.error("Lỗi khi gửi email thông báo cập nhật nhà xe: ", e);
        }

        return convertToResponse(updatedCompany);
    }

    public Page<BusCompanyResponse> searchBusCompanies(BusSearchRequest request) {
        Pageable pageable = createPageable(request);
        return busCompanyRepository.searchByIdOrName(request.getKeyword().trim(), pageable)
                .map(this::convertToResponse);
    }

    public BusResponse getBusDetailByCompanyForAdmin(Integer companyId, Integer busId) {
        // Verify company exists
        if (!busCompanyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + companyId);
        }

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + busId));

        if (!bus.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Xe không thuộc về nhà xe này");
        }

        return convertToBusResponseWithSeats(bus);
    }

    @Transactional
    public void deleteBusOfCompanyByAdmin(Integer companyId, Integer busId,
            com.example.ckdatveexe.module.bus.dto.DeleteBusRequest request) {
        // Verify company exists
        if (!busCompanyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + companyId);
        }

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + busId));

        if (!bus.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Xe không thuộc về nhà xe này");
        }

        if (request.isHardDelete()) {
            // Hard delete - xóa vĩnh viễn
            seatRepository.deleteByBusId(busId);
            busRepository.delete(bus);
        } else {
            // Soft delete - chuyển trạng thái
            bus.setStatus(BusStatus.MAINTENANCE);
            busRepository.save(bus);
        }
    }

    @Transactional
    public BusCompanyResponse restoreBusCompanyAccount(Integer companyId) {
        BusCompany company = busCompanyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + companyId));

        // Find associated user account
        User companyUser = userRepository.findByBusCompanyId(companyId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản của nhà xe"));

        // Restore account if it was blocked
        if (companyUser.getStatus() == UserStatus.BLOCKED) {
            companyUser.setStatus(UserStatus.ACTIVE);
            userRepository.save(companyUser);

            // Send email notification
            try {
                emailService.sendAccountRestorationNotification(
                        companyUser.getEmail(),
                        company.getCompanyName());
            } catch (Exception e) {
                log.error("Lỗi khi gửi email thông báo khôi phục tài khoản: ", e);
            }
        } else {
            throw new IllegalArgumentException("Tài khoản nhà xe này chưa bị khóa");
        }

        return convertToResponse(company);
    }

    // New methods for BusCompanyManagementController

    @Transactional
    public BusCompanyResponse updateMyCompany(Integer companyId, BusCompanyUpdateRequest request) {
        BusCompany busCompany = busCompanyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + companyId));

        // Update company info
        if (request.getCompanyName() != null && !request.getCompanyName().trim().isEmpty()) {
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

    public Page<BusResponse> searchMyCompanyBuses(Integer companyId, BusSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Bus> buses = busRepository.searchBusesByCompany(
                companyId,
                request.getKeyword().trim(),
                request.getStatus() != null ? request.getStatus() : BusStatus.ACTIVE,
                pageable);

        return buses.map(this::convertToBusResponse);
    }

    // New method for deleting bus company by admin
    @Transactional
    public void deleteBusCompanyByAdmin(Integer companyId, boolean hardDelete) {
        BusCompany company = busCompanyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + companyId));

        if (hardDelete) {
            // Hard delete - xóa vĩnh viễn
            // 1. Xóa tất cả ghế của các xe thuộc nhà xe
            List<Bus> companyBuses = busRepository.findByCompanyId(companyId);
            for (Bus bus : companyBuses) {
                seatRepository.deleteByBusId(bus.getId());
            }

            // 2. Xóa tất cả xe của nhà xe
            busRepository.deleteByCompanyId(companyId);

            // 3. Tìm và xóa user account của nhà xe
            userRepository.findByBusCompanyId(companyId)
                    .forEach(user -> userRepository.delete(user));

            // 4. Xóa nhà xe
            busCompanyRepository.delete(company);

            log.info("Hard deleted bus company with ID: {} and all related data", companyId);
        } else {
            // Soft delete - block user account
            List<User> companyUsers = userRepository.findByBusCompanyId(companyId);
            for (User user : companyUsers) {
                user.setStatus(UserStatus.BLOCKED);
                userRepository.save(user);

                // Send email notification
                try {
                    emailService.sendAccountBlockNotification(
                            user.getEmail(),
                            company.getCompanyName(),
                            "Tài khoản nhà xe đã bị tạm khóa bởi quản trị viên");
                } catch (Exception e) {
                    log.error("Lỗi khi gửi email thông báo khóa tài khoản: ", e);
                }
            }

            log.info("Soft deleted (blocked) bus company with ID: {}", companyId);
        }
    }
}