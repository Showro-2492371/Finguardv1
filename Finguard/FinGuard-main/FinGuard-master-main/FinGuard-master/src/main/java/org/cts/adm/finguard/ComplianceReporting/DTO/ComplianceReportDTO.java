package org.cts.adm.finguard.ComplianceReporting.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComplianceReportDTO {

    private Long reportId;
    private Long customerId;
    private int fraudCases;
    private double riskScore;
    private LocalDateTime generatedDate;

}
