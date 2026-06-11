package org.cts.adm.finguard.KycVerification.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.cts.adm.finguard.CustomerOnboarding.Eunm.KycStatus;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerLoginService;
import org.cts.adm.finguard.KycVerification.Service.KycVerificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class KycVerificationController {

    private static final Logger logger =
            LoggerFactory.getLogger(KycVerificationController.class);

    private final KycVerificationService kycVerificationService;
    private final CustomerLoginService customerLoginService;

    @Autowired
    private CustomerRepository customerRepository;

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
            @RequestParam Long customerId) throws Exception {

        logger.info("Received KYC upload request for customerId={}", customerId);

        Customer customer = customerLoginService.getCustomerById(customerId);

        if (customer.getKycStatus() == KycStatus.NOT_STARTED) {
            kycVerificationService.saveKycDocument(file, customerId);
            customer.setKycStatus(KycStatus.IN_PROGRESS);
            customerRepository.save(customer);
            return ResponseEntity.ok("KYC document uploaded successfully");
        }

        return ResponseEntity.ok("KYC already uploaded earlier");
    }

    @PreAuthorize("hasRole('USER')")
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
            @PathVariable Long customerId) {

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
            @RequestParam Long customerId) throws Exception {

        kycVerificationService.updateKycDocument(file, customerId);
        return ResponseEntity.ok("KYC document updated successfully");
    }

    // ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update KYC status by Admin")
    @ApiResponse(responseCode = "200", description = "KYC status updated")
    @PutMapping("/admin/kyc/update-status")
    public ResponseEntity<String> updateStatus(
            @Parameter(description = "Customer ID", required = true)
            @RequestParam Long customerId,

            @Parameter(description = "New KYC status", required = true)
            @RequestParam KycStatus status) {

        kycVerificationService.updateKycStatus(customerId, status);
        return ResponseEntity.ok("KYC status updated to " + status);
    }

    // USER & ADMIN
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Delete KYC document")
    @ApiResponse(responseCode = "200", description = "KYC document deleted")
    @DeleteMapping("/customer/kyc/delete/{customerId}")
    public ResponseEntity<String> delete(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long customerId) {

        kycVerificationService.deleteKycDocument(customerId);
        return ResponseEntity.ok("KYC document deleted successfully");
    }
}