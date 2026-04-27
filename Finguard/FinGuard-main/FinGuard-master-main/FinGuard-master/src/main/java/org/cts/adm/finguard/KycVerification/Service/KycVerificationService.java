package org.cts.adm.finguard.KycVerification.Service;


import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerLoginService;
import org.cts.adm.finguard.KycVerification.Model.KycDocument;
import org.cts.adm.finguard.KycVerification.Repository.KycDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class KycVerificationService{

    private final KycDocumentRepository kycDocumentRepository;
    private final CustomerLoginService customerLoginService;

    KycVerificationService(KycDocumentRepository kycDocumentRepository ,
                           CustomerLoginService customerLoginService){
        this.kycDocumentRepository =    kycDocumentRepository;
        this.customerLoginService = customerLoginService;
    }

    public KycDocument findKycDocumentByCustomerId(Long customerId) {
        return kycDocumentRepository
                .findByCustomerCustomerId(customerId)
                .orElseThrow(() ->
                        new RuntimeException("KYC document not found for customer id: " + customerId)
                );
    }

    public void saveKycDocument(MultipartFile file , Long customerId) throws IOException {
        KycDocument doc = new KycDocument();
        doc.setDocumentName(file.getOriginalFilename());
        doc.setDocumentType(file.getContentType());
        doc.setFileData(file.getBytes());
        doc.setCustomer(customerLoginService.getCustomerById(customerId));
        kycDocumentRepository.save(doc);
    }
}
