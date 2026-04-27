package org.cts.adm.finguard.ComplianceReporting.DTO;

import java.time.LocalDateTime;

public class ComplianceReportDTO {
    private Long reportId;
    private int fraudCases;
    private double riskScore;
    private LocalDateTime generatedDate;

    public ComplianceReportDTO(int fraudCases, double riskScore, LocalDateTime generatedDate, Long reportId) {
        this.reportId = reportId;
        this.fraudCases = fraudCases;
        this.riskScore = riskScore;
        this.generatedDate = generatedDate;
    }

    public Long getReportId() {
        return reportId;
    }


    public int getFraudCases() {
        return fraudCases;
    }


    public double getRiskScore() {
        return riskScore;
    }


    public LocalDateTime getGeneratedDate() {
        return generatedDate;
    }
}
