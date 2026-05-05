package org.cts.adm.finguard.KycVerification.Service;

import org.cts.adm.finguard.CustomerOnboarding.Eunm.KycStatus;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerLoginService;
import org.cts.adm.finguard.KycVerification.Model.KycDocument;
import org.cts.adm.finguard.KycVerification.Repository.KycDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class KycVerificationService {

    private static final Logger logger =
            LoggerFactory.getLogger(KycVerificationService.class);

    private final KycDocumentRepository kycDocumentRepository;
    private final CustomerLoginService customerLoginService;

    public KycVerificationService(KycDocumentRepository kycDocumentRepository,
                                  CustomerLoginService customerLoginService) {
        this.kycDocumentRepository = kycDocumentRepository;
        this.customerLoginService = customerLoginService;
    }

    public KycDocument findKycDocumentByCustomerId(Long customerId) {
        logger.info("Fetching KYC document for customerId={}", customerId);

        return kycDocumentRepository
                .findByCustomerCustomerId(customerId)
                .orElseThrow(() -> {
                    logger.error("KYC document not found for customerId={}", customerId);
                    return new RuntimeException(
                            "KYC document not found for customer id: " + customerId);
                });
    }

    public void saveKycDocument(MultipartFile file, Long customerId) throws IOException {
        logger.info("Saving KYC document for customerId={}", customerId);
        logger.debug("Uploaded file name={}, type={}",
                file.getOriginalFilename(), file.getContentType());

        Customer customer = customerLoginService.getCustomerById(customerId);

        KycDocument doc = new KycDocument();
        doc.setDocumentName(file.getOriginalFilename());
        doc.setDocumentType(file.getContentType());
        doc.setFileData(file.getBytes());
        doc.setCustomer(customer);

        customer.setKycStatus(KycStatus.IN_PROGRESS);
        logger.info("KYC status set to IN_PROGRESS for customerId={}", customerId);

        kycDocumentRepository.save(doc);
        logger.info("KYC document successfully saved for customerId={}", customerId);
    }

    public void updateKycDocument(MultipartFile file, Long customerId) throws IOException {
        logger.info("Updating KYC document for customerId={}", customerId);

        KycDocument existingDoc = findKycDocumentByCustomerId(customerId);

        existingDoc.setDocumentName(file.getOriginalFilename());
        existingDoc.setDocumentType(file.getContentType());
        existingDoc.setFileData(file.getBytes());

        kycDocumentRepository.save(existingDoc);
        logger.info("KYC document updated for customerId={}", customerId);
    }

    public void updateKycStatus(Long customerId, KycStatus status) {
        logger.info("Updating KYC status for customerId={}, status={}", customerId, status);

        Customer customer = customerLoginService.getCustomerById(customerId);
        customer.setKycStatus(status);

        logger.info("KYC status updated successfully for customerId={}", customerId);
    }

    public void deleteKycDocument(Long customerId) {
        logger.warn("Deleting KYC document for customerId={}", customerId);

        KycDocument doc = findKycDocumentByCustomerId(customerId);
        kycDocumentRepository.delete(doc);

        logger.info("KYC document deleted for customerId={}", customerId);
    }
}