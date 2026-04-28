package org.cts.adm.finguard.ComplianceReporting.DTO;

import java.time.LocalDateTime;

public class ComplianceReportDTO {
    private Long reportId;
    private int fraudCases;
    private double avgRiskScore;
    private LocalDateTime generatedDate;

    public ComplianceReportDTO(int fraudCases, double avgRiskScore, LocalDateTime generatedDate, Long reportId) {
        this.reportId = reportId;
        this.fraudCases = fraudCases;
        this.avgRiskScore = avgRiskScore;
        this.generatedDate = generatedDate;
    }

    public Long getReportId() {
        return reportId;
    }


    public int getFraudCases() {
        return fraudCases;
    }


    public double getRiskScore() {
        return avgRiskScore;
    }


    public LocalDateTime getGeneratedDate() {
        return generatedDate;
    }
}
