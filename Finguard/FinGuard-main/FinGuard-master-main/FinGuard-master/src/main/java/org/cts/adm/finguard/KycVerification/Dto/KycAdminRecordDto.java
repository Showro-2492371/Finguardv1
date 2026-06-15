package org.cts.adm.finguard.KycVerification.Dto;

import org.cts.adm.finguard.CustomerOnboarding.Eunm.AccountStatus;
import org.cts.adm.finguard.CustomerOnboarding.Eunm.KycStatus;

import java.time.LocalDateTime;

public record KycAdminRecordDto(
        Long customerId,
        String customerName,
        String contactInfo,
        KycStatus kycStatus,
        AccountStatus accountStatus,
        Long documentId,
        String documentName,
        String documentType,
        LocalDateTime uploadedAt
) {
}
