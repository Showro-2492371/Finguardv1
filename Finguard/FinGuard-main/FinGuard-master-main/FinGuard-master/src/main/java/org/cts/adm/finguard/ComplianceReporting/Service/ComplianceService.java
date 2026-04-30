package org.cts.adm.finguard.ComplianceReporting.Service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.cts.adm.finguard.ComplianceReporting.DTO.ComplianceReportDTO;
import org.cts.adm.finguard.ComplianceReporting.ExceptionHandling.BadRequestException;
import org.cts.adm.finguard.ComplianceReporting.ExceptionHandling.ResourceNotFoundException;
import org.cts.adm.finguard.ComplianceReporting.Model.*;
import org.cts.adm.finguard.ComplianceReporting.Repository.*;
import org.cts.adm.finguard.RiskAlert.Model.RiskAlert;
import org.cts.adm.finguard.RiskAlert.Repository.RiskAlertRepository;
import org.cts.adm.finguard.TransactionMonitoring.Enum.TransactionStatus;
import org.cts.adm.finguard.TransactionMonitoring.Repository.TransactionMonitoringRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;


@Slf4j
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


    @Transactional
    public List<ComplianceReportDTO> generateReports(String user) {

        log.info("Generating compliance reports...");

        List<RiskAlert> alerts = riskAlertRepo.findAll();

        if(alerts.isEmpty())
        {
            log.warn("No risk alerts found");
            throw new BadRequestException("No risk alerts found to generate report");
        }

        Map<Long, List<RiskAlert>> grouped =
                alerts.stream().collect(Collectors.groupingBy(a -> a.getTransaction().getCustomer().getCustomerId()));

        List<ComplianceReportDTO> result = new ArrayList<>();

        for (Map.Entry<Long, List<RiskAlert>> entry : grouped.entrySet()) {

            Long customerId = entry.getKey();
            List<RiskAlert> customerAlerts = entry.getValue();

            int fraudCases = customerAlerts.size();

            double riskScore = customerAlerts.stream()
                    .mapToDouble(a -> a.getRiskScore().doubleValue())
                    .sum();

            complianceRepo.deleteByCustomerId(customerId);

            ComplianceReport report = ComplianceReport.builder()
                    .customerId(customerId)
                    .fraudCases(fraudCases)
                    .riskScore(riskScore)
                    .generatedDate(LocalDateTime.now())
                    .build();


            complianceRepo.save(report);

            result.add(mapToDTO(report));

            log.info("Report generated for customer {}", customerId);
        }

        auditRepo.save(new AuditTrail(
                "Generated customer-wise reports",
                user,
                LocalDateTime.now()
        ));

        return result;
    }



    public List<ComplianceReportDTO> getReports() {
        return complianceRepo.findAll().stream().map(this::mapToDTO).toList();
    }


    public List<ComplianceReportDTO> getByCustomer(Long customerId) {

        List<ComplianceReport> reports=complianceRepo.findByCustomerId(customerId);

        if(reports.isEmpty())
        {
            throw new ResourceNotFoundException("No reports found for customerId: "+customerId);
        }

        return complianceRepo.findByCustomerId(customerId)
                .stream().map(this::mapToDTO).toList();
    }

    public Map<String, Object> getSummary() {

        List<ComplianceReport> reports = complianceRepo.findAll();

        if(reports.isEmpty())
        {
            throw new ResourceNotFoundException("No compliance reports available");
        }

        return Map.of(
                "totalCustomers", reports.size(),
                "totalFraudCases", reports.stream().mapToInt(ComplianceReport::getFraudCases).sum(),
                "totalRiskScore", reports.stream().mapToDouble(ComplianceReport::getRiskScore).sum()
        );
    }


    public String exportReport(Long reportId, String user) throws IOException {

        ComplianceReport report = complianceRepo.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: "+reportId));

        File dir = new File("reports");
        if (!dir.exists()) dir.mkdirs();

        String filePath = "reports/report_" + reportId + ".csv";

        FileWriter writer = new FileWriter(filePath);

        writer.append("ReportId,CustomerId,FraudCases,RiskScore,GeneratedDate\n");
        writer.append(report.getReportId() + "," +
                report.getCustomerId() + "," +
                report.getFraudCases() + "," +
                report.getRiskScore() + "," +
                report.getGeneratedDate());

        writer.close();

        auditRepo.save(new AuditTrail("Exported CSV for report " + reportId, user, LocalDateTime.now()));

        return filePath;
    }

    @Transactional
    public void deleteReport(Long customerId) {

        log.info("Deleting report for customerId={}",customerId);

        List<ComplianceReport> reports=complianceRepo.findByCustomerId(customerId);

        if(reports.isEmpty())
        {
            throw new ResourceNotFoundException(
                    "No report found to delete for customerId: "+customerId
            );
        }
        complianceRepo.deleteByCustomerId(customerId);

    }

    public List<AuditTrail> getAuditLogs() {
        return auditRepo.findAll();
    }


    private ComplianceReportDTO mapToDTO(ComplianceReport r) {
        return new ComplianceReportDTO(
                r.getReportId(),
                r.getCustomerId(),
                r.getFraudCases(),
                r.getRiskScore(),
                r.getGeneratedDate()
        );
    }



}