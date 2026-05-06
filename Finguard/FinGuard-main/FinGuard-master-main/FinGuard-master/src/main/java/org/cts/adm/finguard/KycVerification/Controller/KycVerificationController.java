package org.cts.adm.finguard.KycVerification.Controller;

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

    // ✅ USER only
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/customer/kyc/upload")
    public ResponseEntity<String> upload(
            @RequestParam MultipartFile file,
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

    // ✅ USER only
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/customer/kyc/download/{customerId}")
    public ResponseEntity<byte[]> download(@PathVariable Long customerId) {

        var doc = kycVerificationService.findKycDocumentByCustomerId(customerId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getDocumentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getDocumentName() + "\"")
                .body(doc.getFileData());
    }

    // ✅ USER only
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/customer/kyc/update-document")
    public ResponseEntity<String> updateDocument(
            @RequestParam MultipartFile file,
            @RequestParam Long customerId) throws Exception {

        kycVerificationService.updateKycDocument(file, customerId);
        return ResponseEntity.ok("KYC document updated successfully");
    }

    // ✅ ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/kyc/update-status")
    public ResponseEntity<String> updateStatus(
            @RequestParam Long customerId,
            @RequestParam KycStatus status) {

        kycVerificationService.updateKycStatus(customerId, status);
        return ResponseEntity.ok("KYC status updated to " + status);
    }

    // ✅ USER & ADMIN
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/customer/kyc/delete/{customerId}")
    public ResponseEntity<String> delete(@PathVariable Long customerId) {

        kycVerificationService.deleteKycDocument(customerId);
        return ResponseEntity.ok("KYC document deleted successfully");
    }
}

