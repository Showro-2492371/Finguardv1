package org.cts.adm.finguard.KycVerification.Dto;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycDocumentDto {
    private Long documentId;
    private String documentName;
    private String documentType;
    private byte[] fileData;
    private Customer customer;
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
