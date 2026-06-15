package org.cts.adm.finguard.KycVerification.Service;

import org.cts.adm.finguard.CustomerOnboarding.Eunm.KycStatus;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerLoginService;
import org.cts.adm.finguard.KycVerification.Dto.KycAdminRecordDto;
import org.cts.adm.finguard.KycVerification.Dto.KycStatusResponseDto;
import org.cts.adm.finguard.KycVerification.Model.KycDocument;
import org.cts.adm.finguard.KycVerification.Repository.KycDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class KycVerificationService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    private static final Logger logger =
            LoggerFactory.getLogger(KycVerificationService.class);

    private final KycDocumentRepository kycDocumentRepository;
    private final CustomerLoginService customerLoginService;
    @Autowired
    private CustomerRepository customerRepository;

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

    public KycStatusResponseDto getKycStatus(Long customerId) {
        Customer customer = getCustomerOrThrow(customerId);
        KycStatusResponseDto response = new KycStatusResponseDto();
        response.setCustomerId(customerId);
        response.setKycStatus(customer.getKycStatus());

        kycDocumentRepository.findByCustomerCustomerId(customerId).ifPresentOrElse(doc -> {
            response.setHasDocument(true);
            response.setDocumentName(doc.getDocumentName());
            response.setUploadedAt(doc.getUploadedAt());
        }, () -> response.setHasDocument(false));

        return response;
    }

    public void saveKycDocument(MultipartFile file, Long customerId) throws IOException {
        logger.info("Saving KYC document for customerId={}", customerId);
        logger.debug("Uploaded file name={}, type={}",
                file.getOriginalFilename(), file.getContentType());

        validateFile(file);
        Customer customer = getCustomerOrThrow(customerId);

        KycDocument doc = kycDocumentRepository.findByCustomerCustomerId(customerId)
                .orElseGet(KycDocument::new);
        doc.setDocumentName(file.getOriginalFilename());
        doc.setDocumentType(file.getContentType());
        doc.setFileData(file.getBytes());
        doc.setCustomer(customer);
        doc.setUploadedAt(LocalDateTime.now());

        customer.setKycStatus(KycStatus.IN_PROGRESS);
        logger.info("KYC status set to IN_PROGRESS for customerId={}", customerId);

        kycDocumentRepository.save(doc);
        customerRepository.save(customer);
        logger.info("KYC document successfully saved for customerId={}", customerId);
    }

    public void updateKycDocument(MultipartFile file, Long customerId) throws IOException {
        logger.info("Updating KYC document for customerId={}", customerId);

        validateFile(file);
        Customer customer = getCustomerOrThrow(customerId);
        KycDocument existingDoc = findKycDocumentByCustomerId(customerId);

        existingDoc.setDocumentName(file.getOriginalFilename());
        existingDoc.setDocumentType(file.getContentType());
        existingDoc.setFileData(file.getBytes());
        existingDoc.setUploadedAt(LocalDateTime.now());

        customer.setKycStatus(KycStatus.IN_PROGRESS);
        customerRepository.save(customer);

        kycDocumentRepository.save(existingDoc);
        logger.info("KYC document updated for customerId={}", customerId);
    }

    public void updateKycStatus(Long customerId, KycStatus status) {
        logger.info("Updating KYC status for customerId={}, status={}", customerId, status);

        Customer customer = getCustomerOrThrow(customerId);
        validateStatusTransition(customer.getKycStatus(), status);
        customer.setKycStatus(status);

        customerRepository.save(customer);
        logger.info("KYC status updated successfully for customerId={}", customerId);
    }

    public List<KycAdminRecordDto> getAllKycRecords() {
        return kycDocumentRepository.findAllByOrderByUploadedAtDesc()
                .stream()
                .map(doc -> new KycAdminRecordDto(
                        doc.getCustomer().getCustomerId(),
                        doc.getCustomer().getName(),
                        doc.getCustomer().getContactInfo(),
                        doc.getCustomer().getKycStatus(),
                        doc.getCustomer().getAccountStatus(),
                        doc.getDocumentId(),
                        doc.getDocumentName(),
                        doc.getDocumentType(),
                        doc.getUploadedAt()
                ))
                .toList();
    }

    public void deleteKycDocument(Long customerId, boolean allowVerifiedDelete) {
        logger.warn("Deleting KYC document for customerId={}", customerId);

        Customer customer = getCustomerOrThrow(customerId);
        if (!allowVerifiedDelete && customer.getKycStatus() == KycStatus.VERIFIED) {
            throw new RuntimeException("Verified KYC document cannot be deleted by the customer");
        }

        KycDocument doc = findKycDocumentByCustomerId(customerId);
        kycDocumentRepository.delete(doc);
        customer.setKycStatus(KycStatus.NOT_STARTED);
        customerRepository.save(customer);

        logger.info("KYC document deleted for customerId={}", customerId);
    }

    private Customer getCustomerOrThrow(Long customerId) {
        Customer customer = customerLoginService.getCustomerById(customerId);
        if (customer == null) {
            throw new RuntimeException("Customer not found for customer id: " + customerId);
        }
        return customer;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("KYC document file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("KYC document size must not exceed 5 MB");
        }
        if (file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("Only PDF, JPG, and PNG files are allowed for KYC");
        }
    }

    private void validateStatusTransition(KycStatus currentStatus, KycStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        boolean allowed = switch (currentStatus) {
            case NOT_STARTED -> false;
            case IN_PROGRESS -> newStatus == KycStatus.VERIFIED || newStatus == KycStatus.REJECTED;
            case VERIFIED -> newStatus == KycStatus.IN_PROGRESS;
            case REJECTED -> newStatus == KycStatus.IN_PROGRESS;
        };

        if (!allowed) {
            throw new RuntimeException("Invalid KYC status transition from " + currentStatus + " to " + newStatus);
        }
    }
}