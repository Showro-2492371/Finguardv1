package org.cts.adm.finguard.ComplianceReporting.Service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.cts.adm.finguard.ComplianceReporting.DTO.ComplianceReportDTO;
import org.cts.adm.finguard.ComplianceReporting.ExceptionHandling.BadRequestException;
import org.cts.adm.finguard.ComplianceReporting.ExceptionHandling.ResourceNotFoundException;
import org.cts.adm.finguard.ComplianceReporting.Model.*;
import org.cts.adm.finguard.ComplianceReporting.Repository.*;
import org.cts.adm.finguard.RiskAlert.Enum.RiskAlertStatus;
import org.cts.adm.finguard.RiskAlert.Model.RiskAlert;
import org.cts.adm.finguard.RiskAlert.Repository.RiskAlertRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Service
public class ComplianceService {

    private static final EnumSet<RiskAlertStatus> SUSPECTED_OR_CONFIRMED_STATUSES =
            EnumSet.of(RiskAlertStatus.NEW, RiskAlertStatus.ESCALATED);

    private final ComplianceRepository complianceRepo;
    private final AuditTrailRepository auditRepo;

    // Inject other modules (IMPORTANT)
    private final RiskAlertRepository riskAlertRepo;

    @Value("${compliance.fraud-weight:100}")
    private double fraudWeight;

    public ComplianceService(ComplianceRepository complianceRepo,
                             AuditTrailRepository auditRepo,
                             RiskAlertRepository riskAlertRepo) {
        this.complianceRepo = complianceRepo;
        this.auditRepo = auditRepo;
        this.riskAlertRepo = riskAlertRepo;
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
            ReportMetrics metrics = buildMetrics(customerId, customerAlerts);

//            complianceRepo.deleteByCustomerId(customerId);

            ComplianceReport report = ComplianceReport.builder()
                    .customerId(customerId)
                    .fraudCases(metrics.fraudCases())
                    .riskScore(metrics.riskScore())
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


    @Transactional
    public ComplianceReportDTO generateReportByCustomer(Long customerId, String user) {

        log.info("Generating report for customerId={}", customerId);

        List<RiskAlert> alerts = riskAlertRepo.findAll().stream()
                .filter(a -> a.getTransaction().getCustomer().getCustomerId().equals(customerId))
                .toList();

        if (alerts.isEmpty()) {
            log.warn("No alerts found for customerId={}", customerId);
            throw new ResourceNotFoundException("No alerts found for customerId: " + customerId);
        }
        ReportMetrics metrics = buildMetrics(customerId, alerts);

//        complianceRepo.deleteByCustomerId(customerId);

        ComplianceReport report = ComplianceReport.builder()
                .customerId(customerId)
                .fraudCases(metrics.fraudCases())
                .riskScore(metrics.riskScore())
                .generatedDate(LocalDateTime.now())
                .build();

        complianceRepo.save(report);

        auditRepo.save(new AuditTrail(
                "Generated report for customer " + customerId,
                user,
                LocalDateTime.now()
        ));

        log.info("Report generated successfully for customerId={}", customerId);

        return mapToDTO(report);
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




    public String exportReport(Long customerId, String user) throws IOException {

        log.info("Exporting report for customerId={}", customerId);

        List<ComplianceReport> reports = complianceRepo.findByCustomerId(customerId);

        if (reports.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No report found for customerId: " + customerId
            );
        }

        // Assuming latest report (only one usually exists)
        ComplianceReport report = reports.getFirst();
        ReportMetrics metrics = buildMetrics(customerId, riskAlertRepo.findByCustomerId(customerId));

        File dir = new File("reports");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Unable to create reports directory");
        }

        String filePath = "reports/report_customer_" + customerId + ".csv";

        try (FileWriter writer = new FileWriter(filePath)) {

            writer.append("ReportId,CustomerId,FraudCases,RiskScore,GeneratedDate\n");
            writer.append(
                    report.getReportId() + "," +
                            report.getCustomerId() + "," +
                            metrics.fraudCases() + "," +
                            metrics.riskScore() + "," +
                            report.getGeneratedDate()
            );
        }

        auditRepo.save(new AuditTrail(
                "Exported CSV for customer " + customerId,
                user,
                LocalDateTime.now()
        ));

        log.info("Report exported successfully for customerId={}", customerId);

        return filePath;
    }

    @Transactional
    public void deleteReport(Long customerId, String user) {

        log.info("Deleting report for customerId={}",customerId);

        List<ComplianceReport> reports=complianceRepo.findByCustomerId(customerId);

        if(reports.isEmpty())
        {
            throw new ResourceNotFoundException(
                    "No report found to delete for customerId: "+customerId
            );
        }
        complianceRepo.deleteByCustomerId(customerId);

        String actor = (user == null || user.isBlank()) ? "system" : user;
        auditRepo.save(new AuditTrail(
                "Deleted report(s) for customer " + customerId,
                actor,
                LocalDateTime.now()
        ));

    }


    public List<ComplianceReportDTO> filterReports(
            LocalDate startDate,
            LocalDate endDate,
            Integer month,
            Integer year) {

        log.info("Filtering reports startDate={}, endDate={}, month={}, year={}",
                startDate, endDate, month, year);

        List<ComplianceReport> reports = complianceRepo.findAll();

        if (reports.isEmpty()) {
            throw new ResourceNotFoundException("No reports available");
        }

        Stream<ComplianceReport> stream = reports.stream();

        if (startDate != null && endDate != null) {
            stream = stream.filter(r ->
                    r.getGeneratedDate().toLocalDate().isAfter(startDate.minusDays(1)) &&
                            r.getGeneratedDate().toLocalDate().isBefore(endDate.plusDays(1))
            );
        }

        if (month != null) {
            stream = stream.filter(r ->
                    r.getGeneratedDate().getMonthValue() == month
            );
        }

        if (year != null) {
            stream = stream.filter(r ->
                    r.getGeneratedDate().getYear() == year
            );
        }

        List<ComplianceReportDTO> result = stream.map(this::mapToDTO).toList();

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("No reports found for given filters");
        }

        return result;
    }




    public List<AuditTrail> getAuditLogs() {
        return auditRepo.findAll();
    }


    private ComplianceReportDTO mapToDTO(ComplianceReport r) {
        // Always derive metrics from live alerts so API responses cannot return stale inconsistencies.
        ReportMetrics metrics = buildMetrics(r.getCustomerId(), riskAlertRepo.findByCustomerId(r.getCustomerId()));
        return new ComplianceReportDTO(
                r.getReportId(),
                r.getCustomerId(),
                metrics.fraudCases(),
                metrics.riskScore(),
                r.getGeneratedDate()
        );
    }

    private ReportMetrics buildMetrics(Long customerId, List<RiskAlert> alerts) {
        long rawFraudCases = alerts.stream()
                .filter(this::isFraudCase)
                .count();
        int fraudCases = normalizeFraudCases(rawFraudCases);

        double baseRiskFromAlerts = alerts.stream()
                .map(RiskAlert::getRiskScore)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(java.math.BigDecimal::doubleValue)
                .sum();

        double riskScore = normalizeRiskScore(customerId, fraudCases, baseRiskFromAlerts);
        return new ReportMetrics(fraudCases, riskScore);
    }

    private boolean isFraudCase(RiskAlert alert) {
        if (alert == null) {
            return false;
        }
        RiskAlertStatus status = RiskAlertStatus.normalizeForWorkflow(alert.getStatus());
        return status != null && SUSPECTED_OR_CONFIRMED_STATUSES.contains(status);
    }

    private int normalizeFraudCases(long rawFraudCases) {
        if (rawFraudCases < 0) {
            throw new IllegalArgumentException("fraudCases cannot be negative");
        }
        double floored = Math.floor(rawFraudCases);
        if (floored > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("fraudCases exceeds supported range");
        }
        return (int) floored;
    }

    private double normalizeRiskScore(Long customerId, int fraudCases, double riskScoreFromAlerts) {
        double nonNegativeRisk = Math.max(0d, riskScoreFromAlerts);
        double minimumFraudRisk = fraudCases * fraudWeight;

        if (fraudCases > 0 && nonNegativeRisk == 0d) {
            log.warn("Risk/fraud mismatch for customerId={} (fraudCases={}, riskScore=0). Applying fallback.",
                    customerId, fraudCases);
        }

        if (nonNegativeRisk < minimumFraudRisk) {
            log.warn("Risk score below fraud minimum for customerId={} (fraudCases={}, riskScore={}, minimum={}).",
                    customerId, fraudCases, nonNegativeRisk, minimumFraudRisk);
        }

        return Math.max(nonNegativeRisk, minimumFraudRisk);
    }

    private record ReportMetrics(int fraudCases, double riskScore) {
    }




}