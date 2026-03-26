package com.example.ckdatveexe.module.buscompany.controller;

import com.example.ckdatveexe.module.buscompany.dto.BusCompanyResponse;
import com.example.ckdatveexe.module.buscompany.service.BusCompanyService;
import com.example.ckdatveexe.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/bus-companies")
@RequiredArgsConstructor
@Tag(name = "Bus Company User APIs", description = "APIs cho người dùng xem thông tin nhà xe")
public class BusCompanyUserController {

    private final BusCompanyService busCompanyService;

    @GetMapping("/test")
    @Operation(summary = "Test endpoint")
    public String testEndpoint() {
        System.out.println("🔍 [DEBUG] Test endpoint called!");
        return "Test endpoint works!";
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy danh sách tất cả nhà xe đối tác")
    public ResponseEntity<ApiResponse<Page<BusCompanyResponse>>> getAllPartnerBusCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        System.out.println("🔍 [DEBUG] getAllPartnerBusCompanies called with page=" + page + ", size=" + size);

        try {
            // Validate sortBy parameter to prevent PropertyReferenceException
            if (!isValidSortField(sortBy)) {
                sortBy = "id"; // fallback to safe default
            }

            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            System.out.println("🔍 [DEBUG] Calling busCompanyService.getAllBusCompanies...");
            Page<BusCompanyResponse> companies = busCompanyService.getAllBusCompanies(pageable);
            System.out.println("🔍 [DEBUG] Service returned: " + companies.getTotalElements() + " companies");

            ApiResponse<Page<BusCompanyResponse>> response = ApiResponse
                    .success("Lấy danh sách nhà xe đối tác thành công", companies);
            System.out.println("🔍 [DEBUG] Returning response: " + response);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ [ERROR] Exception in getAllPartnerBusCompanies: " + e.getMessage());
            e.printStackTrace();

            // Fallback: try without sorting if there's an issue
            try {
                Pageable pageable = PageRequest.of(page, size);
                Page<BusCompanyResponse> companies = busCompanyService.getAllBusCompanies(pageable);
                return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhà xe đối tác thành công", companies));
            } catch (Exception ex) {
                System.out.println("❌ [ERROR] Fallback also failed: " + ex.getMessage());
                ex.printStackTrace();
                return ResponseEntity.ok(ApiResponse.error("Có lỗi xảy ra: " + ex.getMessage()));
            }
        }
    }

    private boolean isValidSortField(String sortBy) {
        // List of valid sort fields for BusCompany entity
        return sortBy.equals("id") ||
                sortBy.equals("companyName") ||
                sortBy.equals("createdAt") ||
                sortBy.equals("updatedAt");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết nhà xe")
    public ResponseEntity<ApiResponse<BusCompanyResponse>> getBusCompanyDetail(@PathVariable Integer id) {
        BusCompanyResponse company = busCompanyService.getBusCompanyDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết nhà xe thành công", company));
    }
}