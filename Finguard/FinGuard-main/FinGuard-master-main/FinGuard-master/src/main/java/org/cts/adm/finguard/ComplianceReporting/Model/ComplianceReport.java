package org.cts.adm.finguard.ComplianceReporting.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ComplianceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    private int fraudCases;
    private double avgRiskScore;

    private LocalDateTime generatedDate;


    public ComplianceReport() {}

    public ComplianceReport(int fraudCases, double avgRiskScore, LocalDateTime generatedDate) {
        this.fraudCases = fraudCases;
        this.avgRiskScore = avgRiskScore;
        this.generatedDate = generatedDate;
    }


    public Long getReportId() { return reportId; }
    public int getFraudCases() { return fraudCases; }
    public void setFraudCases(int fraudCases) { this.fraudCases = fraudCases; }

    public double getAvgRiskScore() { return avgRiskScore; }
    public void setAvgRiskScore(double avgRiskScore) { this.avgRiskScore = avgRiskScore; }

    public LocalDateTime getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDateTime generatedDate) { this.generatedDate = generatedDate; }
}