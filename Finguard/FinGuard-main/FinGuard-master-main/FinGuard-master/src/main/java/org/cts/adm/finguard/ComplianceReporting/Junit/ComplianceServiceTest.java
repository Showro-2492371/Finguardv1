//package org.cts.adm.finguard.ComplianceReporting.Junit;
//
//
//
//import org.cts.adm.finguard.ComplianceReporting.Exception.BadRequestException;
//import org.cts.adm.finguard.ComplianceReporting.Exception.ResourceNotFoundException;
//import org.cts.adm.finguard.ComplianceReporting.Model.ComplianceReport;
//import org.cts.adm.finguard.ComplianceReporting.Repository.ComplianceRepository;
//import org.cts.adm.finguard.ComplianceReporting.Repository.AuditTrailRepository;
//import org.cts.adm.finguard.RiskAlert.Model.RiskAlert;
//import org.cts.adm.finguard.RiskAlert.Repository.RiskAlertRepository;
//import org.cts.adm.finguard.TransactionMonitoring.Repository.TransactionMonitoringRepository;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ComplianceServiceTest {
//
//    @Mock
//    private ComplianceRepository complianceRepo;
//
//    @Mock
//    private AuditTrailRepository auditRepo;
//
//    @Mock
//    private RiskAlertRepository riskAlertRepo;
//
//    @Mock
//    private TransactionMonitoringRepository transactionRepo;
//
//    @InjectMocks
//    private ComplianceService service;
//
//    // ✅ 1. Test generateReports - SUCCESS
//    @Test
//    void testGenerateReportsSuccess() {
//
//        RiskAlert alert = mock(RiskAlert.class);
//        when(alert.getRiskScore()).thenReturn(50.0);
//        when(alert.getTransaction().getCustomer().getCustomerId()).thenReturn(101L);
//
//        when(riskAlertRepo.findAll()).thenReturn(List.of(alert));
//
//        List<?> result = service.generateReports("testUser");
//
//        assertFalse(result.isEmpty());
//        verify(complianceRepo, times(1)).save(any(ComplianceReport.class));
//    }
//
//    // ✅ 2. Test generateReports - NO DATA (Exception)
//    @Test
//    void testGenerateReportsNoAlerts() {
//
//        when(riskAlertRepo.findAll()).thenReturn(Collections.emptyList());
//
//        assertThrows(BadRequestException.class, () -> {
//            service.generateReports("testUser");
//        });
//    }
//
//    // ✅ 3. Test exportReport - NOT FOUND
//    @Test
//    void testExportReportNotFound() {
//
//        when(complianceRepo.findById(1L)).thenReturn(Optional.empty());
//
//        assertThrows(ResourceNotFoundException.class, () -> {
//            service.exportReport(1L, "user");
//        });
//    }
//
//    // ✅ 4. Test deleteReport - NOT FOUND
//    @Test
//    void testDeleteReportNotFound() {
//
//        when(complianceRepo.findByCustomerId(1L)).thenReturn(Collections.emptyList());
//
//        assertThrows(ResourceNotFoundException.class, () -> {
//            service.deleteReport(1L);
//        });
//    }
//
//    // ✅ 5. Test deleteReport - SUCCESS
//    @Test
//    void testDeleteReportSuccess() {
//
//        ComplianceReport report = new ComplianceReport();
//        when(complianceRepo.findByCustomerId(1L)).thenReturn(List.of(report));
//
//        service.deleteReport(1L);
//
//        verify(complianceRepo, times(1)).deleteByCustomerId(1L);
//    }
//}