package org.cts.adm.finguard.KycVerification.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.cts.adm.finguard.CustomerOnboarding.Eunm.KycStatus;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerLoginService;
import org.cts.adm.finguard.Jwt.JwtUser;
import org.cts.adm.finguard.KycVerification.Dto.KycAdminRecordDto;
import org.cts.adm.finguard.KycVerification.Dto.KycStatusResponseDto;
import org.cts.adm.finguard.KycVerification.Service.KycVerificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class KycVerificationController {

    private static final Logger logger =
            LoggerFactory.getLogger(KycVerificationController.class);

    private final KycVerificationService kycVerificationService;
    private final CustomerLoginService customerLoginService;

    public KycVerificationController(KycVerificationService kycVerificationService,
                                     CustomerLoginService customerLoginService) {
        this.kycVerificationService = kycVerificationService;
        this.customerLoginService = customerLoginService;
    }

    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Upload KYC document",
            description = "Allows user to upload KYC document when KYC status is NOT_STARTED"
    )
    @ApiResponse(responseCode = "200", description = "KYC document uploaded")
    @PostMapping(value = "/customer/kyc/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(
            @Parameter(description = "KYC document file", required = true)
            @RequestParam MultipartFile file,

            @Parameter(description = "Customer ID", required = true)
            @RequestParam Long customerId,
            Authentication authentication) throws Exception {

        logger.info("Received KYC upload request for customerId={}", customerId);
        ensureOwnershipOrAdmin(authentication, customerId);

        Customer customer = customerLoginService.getCustomerById(customerId);
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }

        if (customer.getKycStatus() == KycStatus.NOT_STARTED || customer.getKycStatus() == KycStatus.REJECTED) {
            kycVerificationService.saveKycDocument(file, customerId);
            return ResponseEntity.ok("KYC document uploaded successfully and sent for review");
        }

        if (customer.getKycStatus() == KycStatus.IN_PROGRESS) {
            throw new RuntimeException("KYC document is already under review");
        }

        if (customer.getKycStatus() == KycStatus.VERIFIED) {
            throw new RuntimeException("KYC is already verified");
        }

        throw new RuntimeException("KYC upload is not allowed for the current status");
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/customer/kyc/status/{customerId}")
    public ResponseEntity<KycStatusResponseDto> getStatus(@PathVariable Long customerId,
                                                          Authentication authentication) {
        ensureOwnershipOrAdmin(authentication, customerId);
        return ResponseEntity.ok(kycVerificationService.getKycStatus(customerId));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Download KYC document")
    @ApiResponse(
            responseCode = "200",
            description = "KYC document downloaded",
            content = @Content(
                    mediaType = "application/octet-stream",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @GetMapping("/customer/kyc/download/{customerId}")
    public ResponseEntity<byte[]> download(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long customerId,
            Authentication authentication) {

        ensureOwnershipOrAdmin(authentication, customerId);

        var doc = kycVerificationService.findKycDocumentByCustomerId(customerId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getDocumentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getDocumentName() + "\"")
                .body(doc.getFileData());
    }

    // USER only
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update existing KYC document")
    @ApiResponse(responseCode = "200", description = "KYC document updated")
    @PutMapping(value = "/customer/kyc/update-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateDocument(
            @Parameter(description = "Updated KYC file", required = true)
            @RequestParam MultipartFile file,

            @Parameter(description = "Customer ID", required = true)
            @RequestParam Long customerId,
            Authentication authentication) throws Exception {

        ensureOwnershipOrAdmin(authentication, customerId);
        Customer customer = customerLoginService.getCustomerById(customerId);
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }
        if (customer.getKycStatus() != KycStatus.REJECTED) {
            throw new RuntimeException("KYC document can only be updated when the current status is REJECTED");
        }

        kycVerificationService.updateKycDocument(file, customerId);
        return ResponseEntity.ok("KYC document updated successfully and re-submitted for review");
    }

    // ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/kyc/records")
    public ResponseEntity<List<KycAdminRecordDto>> getAllKycRecords() {
        return ResponseEntity.ok(kycVerificationService.getAllKycRecords());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update KYC status by Admin")
    @ApiResponse(responseCode = "200", description = "KYC status updated")
    @PutMapping("/admin/kyc/update-status")
    public ResponseEntity<String> updateStatus(
            @Parameter(description = "Customer ID", required = true)
            @RequestParam Long customerId,

            @Parameter(description = "New KYC status", required = true)
            @RequestParam String status) {

        try {
            KycStatus kycStatus = KycStatus.valueOf(status.toUpperCase());
            kycVerificationService.updateKycStatus(customerId, kycStatus);
            return ResponseEntity.ok("KYC status updated to " + kycStatus);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        }
    }

    // USER & ADMIN
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Delete KYC document")
    @ApiResponse(responseCode = "200", description = "KYC document deleted")
    @DeleteMapping("/customer/kyc/delete/{customerId}")
    public ResponseEntity<String> delete(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long customerId,
            Authentication authentication) {

        ensureOwnershipOrAdmin(authentication, customerId);
        kycVerificationService.deleteKycDocument(customerId, isAdmin(authentication));
        return ResponseEntity.ok("KYC document deleted successfully");
    }

    private void ensureOwnershipOrAdmin(Authentication authentication, Long customerId) {
        if (isAdmin(authentication)) {
            return;
        }

        Object principal = authentication != null ? authentication.getPrincipal() : null;
        if (!(principal instanceof JwtUser jwtUser) || !customerId.equals(jwtUser.getCustomerId())) {
            throw new RuntimeException("You are not authorized to access this KYC document");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}