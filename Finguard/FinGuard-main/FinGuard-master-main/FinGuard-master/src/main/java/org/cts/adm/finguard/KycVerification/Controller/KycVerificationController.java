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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/customer/kyc")
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

    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @RequestParam MultipartFile file,
            @RequestParam Long customerId) throws Exception {

        logger.info("Received KYC upload request for customerId={}", customerId);
        logger.debug("Uploaded file name={}, type={}",
                file.getOriginalFilename(), file.getContentType());
        Customer customer = customerLoginService.getCustomerById(customerId);
        if(customer.getKycStatus() == KycStatus.NOT_STARTED){
            kycVerificationService.saveKycDocument(file, customerId);
            customer.setKycStatus(KycStatus.IN_PROGRESS);
            customerRepository.save(customer);
            logger.info("KYC upload successful for customerId={}", customerId);
            return ResponseEntity.ok("KYC document uploaded successfully");
        }
        logger.info("KYC upload failed for customerId={} because its already their", customerId);
        return  ResponseEntity.ok("KYC document upload failed because documents are already uploaded before try to update it");
    }

    @GetMapping("/download/{customerId}")
    public ResponseEntity<byte[]> download(@PathVariable Long customerId) {
        logger.info("Received KYC download request for customerId={}", customerId);

        var doc = kycVerificationService.findKycDocumentByCustomerId(customerId);

        logger.info("KYC document download successful for customerId={}", customerId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getDocumentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getDocumentName() + "\"")
                .body(doc.getFileData());
    }

    @PutMapping("/update-document")
    public ResponseEntity<String> updateDocument(
            @RequestParam MultipartFile file,
            @RequestParam Long customerId) throws Exception {

        logger.info("Received KYC document update request for customerId={}", customerId);

        kycVerificationService.updateKycDocument(file, customerId);

        logger.info("KYC document updated successfully for customerId={}", customerId);
        return ResponseEntity.ok("KYC document updated successfully");
    }

    @PutMapping("/update-status")
    public ResponseEntity<String> updateStatus(
            @RequestParam Long customerId,
            @RequestParam KycStatus status) {

        logger.info("Updating KYC status for customerId={} to {}", customerId, status);

        kycVerificationService.updateKycStatus(customerId, status);

        logger.info("KYC status updated successfully for customerId={}", customerId);
        return ResponseEntity.ok("KYC status updated to " + status);
    }

    @DeleteMapping("/delete/{customerId}")
    public ResponseEntity<String> delete(@PathVariable Long customerId) {
        logger.warn("KYC delete request received for customerId={}", customerId);

        kycVerificationService.deleteKycDocument(customerId);

        logger.info("KYC document deleted successfully for customerId={}", customerId);
        return ResponseEntity.ok("KYC document deleted successfully");
    }
}