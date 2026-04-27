package org.cts.adm.finguard.ComplianceReporting.Service;

import org.cts.adm.finguard.ComplianceReporting.Model.*;
import org.cts.adm.finguard.ComplianceReporting.Repository.*;
import org.cts.adm.finguard.RiskAlert.Repository.RiskAlertRepository;
import org.cts.adm.finguard.TransactionMonitoring.Repository.TransactionMonitoringRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import org.cts.adm.finguard.RiskAlert.Model.RiskAlert;
import java.util.List;

@Service
public class ComplianceService {

    private final ComplianceRepository complianceRepo;
    private final AuditTrailRepository auditRepo;

    // Inject other modules (IMPORTANT)
    private final RiskAlertRepository riskAlertRepo;
    private final TransactionMonitoringRepository transactionRepo;

    public ComplianceService(ComplianceRepository complianceRepo,
                             AuditTrailRepository auditRepo,
                             RiskAlertRepository riskAlertRepo,
                             TransactionMonitoringRepository transactionRepo) {
        this.complianceRepo = complianceRepo;
        this.auditRepo = auditRepo;
        this.riskAlertRepo = riskAlertRepo;
        this.transactionRepo = transactionRepo;
    }

    // ✅ Generate AML Report
    public ComplianceReport generateReport(String user) {

        int fraudCases = transactionRepo.countByStatus("FLAGGED");

        double avgRisk = riskAlertRepo.findAll()
                .stream()
                .mapToDouble(RiskAlert::getRiskScore)
                .average()
                .orElse(0.0);

        ComplianceReport report = new ComplianceReport(
                fraudCases,
                avgRisk,
                LocalDateTime.now()
        );

        complianceRepo.save(report);

        // Audit Trail
        auditRepo.save(new AuditTrail(
                "Generated Compliance Report",
                user,
                LocalDateTime.now()
        ));

        return report;
    }

    // ✅ Get all reports
    public List<ComplianceReport> getReports() {
        return complianceRepo.findAll();
    }

    // ✅ Export report (TEXT format - no third party)
    public String exportReport(Long reportId) {

        ComplianceReport report = complianceRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        return "REPORT ID: " + report.getReportId() + "\n" +
                "Fraud Cases: " + report.getFraudCases() + "\n" +
                "Avg Risk Score: " + report.getAvgRiskScore() + "\n" +
                "Generated Date: " + report.getGeneratedDate();
    }
    public List<AuditTrail> getAuditLogs() {
        return auditRepo.findAll();
    }


}