package org.cts.adm.finguard.ComplianceReporting.Service;

import org.cts.adm.finguard.ComplianceReporting.ExceptionHandling.BadRequestException;
import org.cts.adm.finguard.ComplianceReporting.ExceptionHandling.ResourceNotFoundException;
import org.cts.adm.finguard.ComplianceReporting.Model.AuditTrail;
import org.cts.adm.finguard.ComplianceReporting.Model.ComplianceReport;
import org.cts.adm.finguard.ComplianceReporting.Repository.AuditTrailRepository;
import org.cts.adm.finguard.ComplianceReporting.Repository.ComplianceRepository;
import org.cts.adm.finguard.RiskAlert.Model.RiskAlert;
import org.cts.adm.finguard.RiskAlert.Repository.RiskAlertRepository;
import org.cts.adm.finguard.TransactionMonitoring.Model.Transaction;
import org.cts.adm.finguard.TransactionMonitoring.Repository.TransactionMonitoringRepository;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplianceServiceTest {

    @Mock
    private ComplianceRepository complianceRepo;

    @Mock
    private AuditTrailRepository auditRepo;

    @Mock
    private RiskAlertRepository riskAlertRepo;

    @Mock
    private TransactionMonitoringRepository transactionRepo;

    @InjectMocks
    private org.cts.adm.finguard.ComplianceReporting.Service.ComplianceService service;

    @BeforeEach
    void defaultStubs() {
        lenient().when(riskAlertRepo.findByCustomerId(anyLong())).thenReturn(Collections.emptyList());
    }

    // From earlier tests: generateReports success
    @Test
    void testGenerateReportsSuccess() {
        RiskAlert alert = mock(RiskAlert.class);
        Transaction tx = mock(Transaction.class);
        Customer cust = mock(Customer.class);

        when(alert.getRiskScore()).thenReturn(BigDecimal.valueOf(50));
        when(alert.getTransaction()).thenReturn(tx);
        when(tx.getCustomer()).thenReturn(cust);
        when(cust.getCustomerId()).thenReturn(101L);

        when(riskAlertRepo.findAll()).thenReturn(List.of(alert));

        List<?> result = service.generateReports("testUser");

        assertFalse(result.isEmpty());
        verify(complianceRepo, times(1)).save(any(ComplianceReport.class));
        verify(auditRepo, times(1)).save(any());
    }

    @Test
    void testGenerateReportsNoAlerts() {
        when(riskAlertRepo.findAll()).thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, () -> service.generateReports("testUser"));
    }

    @Test
    void testExportReportNotFound() {
        when(complianceRepo.findByCustomerId(1L)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> service.exportReport(1L, "user"));
    }

    @Test
    void testDeleteReportNotFound() {
        when(complianceRepo.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteReport(1L, "admin"));
    }

    @Test
    void testDeleteReportSuccess() {
        ComplianceReport report = ComplianceReport.builder()
                .reportId(1L)
                .customerId(1L)
                .fraudCases(2)
                .riskScore(100.0)
                .generatedDate(LocalDateTime.now())
                .build();

        when(complianceRepo.findById(1L)).thenReturn(java.util.Optional.of(report));

        service.deleteReport(1L, "admin");

        verify(complianceRepo, times(1)).deleteById(1L);
        verify(auditRepo, times(1)).save(any(AuditTrail.class));
    }

    @Test
    void testGenerateReportByCustomerNotFound() {
        when(riskAlertRepo.findAll()).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> service.generateReportByCustomer(999L, "user"));
    }

    // Additional tests merged
    @Test
    void testGenerateReportByCustomerSuccess() {
        RiskAlert alert = mock(RiskAlert.class);
        Transaction tx = mock(Transaction.class);
        Customer cust = mock(Customer.class);

        when(alert.getRiskScore()).thenReturn(BigDecimal.valueOf(20));
        when(alert.getTransaction()).thenReturn(tx);
        when(tx.getCustomer()).thenReturn(cust);
        when(cust.getCustomerId()).thenReturn(555L);

        when(riskAlertRepo.findAll()).thenReturn(List.of(alert));

        var dto = service.generateReportByCustomer(555L, "admin");

        assertNotNull(dto);
        assertEquals(555L, dto.getCustomerId());
        verify(complianceRepo, times(1)).save(any(ComplianceReport.class));
        verify(auditRepo, times(1)).save(any(AuditTrail.class));
    }

    @Test
    void testGetReportsAndGetByCustomerSuccess() {
        ComplianceReport r1 = ComplianceReport.builder()
                .reportId(1L).customerId(10L).fraudCases(1).riskScore(10.0).generatedDate(LocalDateTime.now()).build();

        when(complianceRepo.findAll()).thenReturn(List.of(r1));
        when(complianceRepo.findByCustomerId(10L)).thenReturn(List.of(r1));

        var all = service.getReports();
        var byCust = service.getByCustomer(10L);

        assertFalse(all.isEmpty());
        assertFalse(byCust.isEmpty());
    }

    @Test
    void testGetSummarySuccess() {
        ComplianceReport r1 = ComplianceReport.builder()
                .reportId(1L).customerId(10L).fraudCases(2).riskScore(30.0).generatedDate(LocalDateTime.now()).build();

        when(complianceRepo.findAll()).thenReturn(List.of(r1));

        var summary = service.getSummary();

        assertEquals(1, summary.get("totalCustomers"));
        assertEquals(2, summary.get("totalFraudCases"));
        assertEquals(30.0, summary.get("totalRiskScore"));
    }

    @Test
    void testFilterReportsVarious() {
        ComplianceReport jan = ComplianceReport.builder()
                .reportId(1L).customerId(1L).fraudCases(1).riskScore(5.0)
                .generatedDate(LocalDateTime.of(2026,1,15,0,0)).build();

        ComplianceReport feb = ComplianceReport.builder()
                .reportId(2L).customerId(2L).fraudCases(2).riskScore(10.0)
                .generatedDate(LocalDateTime.of(2026,2,20,0,0)).build();

        when(complianceRepo.findAll()).thenReturn(List.of(jan, feb));

        // filter by month=2
        var byMonth = service.filterReports(null, null, 2, null);
        assertEquals(1, byMonth.size());

        // filter by year=2026
        var byYear = service.filterReports(null, null, null, 2026);
        assertEquals(2, byYear.size());

        // filter by date range covering jan only
        var byRange = service.filterReports(LocalDate.of(2026,1,1), LocalDate.of(2026,1,31), null, null);
        assertEquals(1, byRange.size());

        // filter that yields empty -> exception
        assertThrows(ResourceNotFoundException.class, () -> service.filterReports(LocalDate.of(2025,1,1), LocalDate.of(2025,1,31), null, null));
    }

    @Test
    void testExportReportSuccessCreatesFile() throws IOException {
        ComplianceReport report = ComplianceReport.builder()
                .reportId(3L).customerId(77L).fraudCases(3).riskScore(15.0)
                .generatedDate(LocalDateTime.now()).build();

        when(complianceRepo.findByCustomerId(77L)).thenReturn(List.of(report));

        String path = service.exportReport(77L, "user");

        File f = new File(path);
        assertTrue(f.exists());

        // cleanup
        f.delete();
    }

    @Test
    void testGetAuditLogs() {
        AuditTrail a = new AuditTrail("act", "user", LocalDateTime.now());
        when(auditRepo.findAll()).thenReturn(List.of(a));

        var logs = service.getAuditLogs();
        assertEquals(1, logs.size());
    }

}
