package org.cts.adm.finguard.KycVerification.Dto;

import org.cts.adm.finguard.CustomerOnboarding.Eunm.KycStatus;

import java.time.LocalDateTime;

public class KycStatusResponseDto {
    private Long customerId;
    private KycStatus kycStatus;
    private boolean hasDocument;
    private String documentName;
    private LocalDateTime uploadedAt;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(KycStatus kycStatus) {
        this.kycStatus = kycStatus;
    }

    public boolean isHasDocument() {
        return hasDocument;
    }

    public void setHasDocument(boolean hasDocument) {
        this.hasDocument = hasDocument;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}

