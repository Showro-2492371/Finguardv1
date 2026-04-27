package org.cts.adm.finguard.KycVerification.Controller;

import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerLoginService;
import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerSignupService;
import org.cts.adm.finguard.Jwt.JwtUtil;
import org.cts.adm.finguard.KycVerification.Model.KycDocument;
import org.cts.adm.finguard.KycVerification.Repository.KycDocumentRepository;
import org.cts.adm.finguard.KycVerification.Service.KycVerificationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/customer/kyc")
public class KycVerificationController {

    private final KycDocumentRepository kycDocumentRepository;
    private final CustomerLoginService customerLoginService;
    private final KycVerificationService kycVerificationService;
    private final JwtUtil jwtUtil;

    KycVerificationController(KycDocumentRepository kycDocumentRepository,
                              CustomerLoginService customerLoginService,
                              KycVerificationService kycVerificationService, JwtUtil jwtUtil){

        this.kycDocumentRepository =    kycDocumentRepository;
        this.customerLoginService = customerLoginService;
        this.kycVerificationService = kycVerificationService;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("customerId") Long customerId) throws Exception {
        kycVerificationService.saveKycDocument(file,customerId);
        return ResponseEntity.ok("File uploaded successfully");
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Long id) throws Throwable {
            KycDocument doc = kycVerificationService.findKycDocumentByCustomerId(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getDocumentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getDocumentName() + "\"")
                .body(doc.getFileData());
    }

}
