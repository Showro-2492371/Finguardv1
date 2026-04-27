package org.cts.adm.finguard.ComplianceReporting.Service;

import org.cts.adm.finguard.ComplianceReporting.Model.*;
import org.cts.adm.finguard.ComplianceReporting.Repository.*;
import org.cts.adm.finguard.RiskAlert.Repository.RiskAlertRepository;
import org.cts.adm.finguard.TransactionMonitoring.Enum.TransactionStatus;
import org.cts.adm.finguard.TransactionMonitoring.Repository.TransactionMonitoringRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
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

        int fraudCases = transactionRepo.countByStatus(TransactionStatus.FLAGGED);

        double avgRisk = riskAlertRepo.findAll()
                .stream()
                .mapToDouble(alert->alert.getRiskScore().doubleValue())
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
    public String exportReport(Long reportId, String user) throws IOException {

        ComplianceReport report = complianceRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        String filePath = "reports/report_" + reportId + ".csv";

        File file=new File(filePath);

        File parentDir=file.getParentFile();
        if(!parentDir.exists())
        {
            parentDir.mkdirs();
        }

        FileWriter writer = new FileWriter(file);

        writer.append("ReportId,FraudCases,AvgRiskScore,GeneratedDate\n");
        writer.append(report.getReportId() + ","
                + report.getFraudCases() + ","
                + report.getAvgRiskScore() + ","
                + report.getGeneratedDate());

        writer.flush();
        writer.close();

        // Audit log
        auditRepo.save(new AuditTrail(
                "CSV saved at: " + filePath,
                user,
                LocalDateTime.now()
        ));

        return "CSV file generated and saved sucessfully at: "+filePath;
    }
    public List<AuditTrail> getAuditLogs() {
        return auditRepo.findAll();
    }


}