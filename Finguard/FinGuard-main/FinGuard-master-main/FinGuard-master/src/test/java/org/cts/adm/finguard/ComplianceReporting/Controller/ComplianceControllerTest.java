package org.cts.adm.finguard.ComplianceReporting.Controller;

import org.cts.adm.finguard.ComplianceReporting.DTO.ComplianceReportDTO;
import org.cts.adm.finguard.ComplianceReporting.Model.AuditTrail;
import org.cts.adm.finguard.ComplianceReporting.Service.ComplianceService;
import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerLoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ComplianceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ComplianceService service;

    @Mock
    private CustomerLoginService customerLoginService;

    private ComplianceController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new ComplianceController(service, customerLoginService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGenerateEndpoint() throws Exception {
        ComplianceReportDTO dto = new ComplianceReportDTO(1L, 2L, 1, 5.0, LocalDateTime.now());
        when(service.generateReports("admin")).thenReturn(List.of(dto));

        mockMvc.perform(post("/api/compliance/generate").param("user", "admin"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGenerateForCustomer() throws Exception {
        ComplianceReportDTO dto = new ComplianceReportDTO(1L, 5L, 1, 5.0, LocalDateTime.now());
        when(service.generateReportByCustomer(5L, "admin")).thenReturn(dto);

        mockMvc.perform(post("/api/compliance/generate/5").param("user", "admin"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetAllReports() throws Exception {
        ComplianceReportDTO dto = new ComplianceReportDTO(1L, 2L, 1, 5.0, LocalDateTime.now());
        when(service.getReports()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/compliance/allreports"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetByCustomer() throws Exception {
        ComplianceReportDTO dto = new ComplianceReportDTO(1L, 3L, 1, 5.0, LocalDateTime.now());
        when(service.getByCustomer(3L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/compliance/reports/3"))
                .andExpect(status().isOk());
    }

    @Test
    void testSummary() throws Exception {
        when(service.getSummary()).thenReturn(Map.of("totalCustomers", 1));

        mockMvc.perform(get("/api/compliance/summary"))
                .andExpect(status().isOk());
    }

    @Test
    void testExportCSV() throws Exception {
        when(service.exportReport(8L, "user")).thenReturn("reports/report_customer_8.csv");

        mockMvc.perform(get("/api/compliance/export/csv/8").param("user", "user"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("reports/report_customer_8.csv")));
    }

    @Test
    void testDeleteReport() throws Exception {
        doNothing().when(service).deleteReport(4L, "system");

        mockMvc.perform(delete("/api/compliance/reports/4"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted"));
    }

    @Test
    void testFilterReports() throws Exception {
        ComplianceReportDTO dto = new ComplianceReportDTO(1L, 2L, 1, 5.0, LocalDateTime.now());
        when(service.filterReports(null, null, 2, null)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/compliance/reports/filter").param("month", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAuditLogs() throws Exception {
        AuditTrail a = new AuditTrail("act", "user", LocalDateTime.now());
        when(service.getAuditLogs()).thenReturn(List.of(a));

        mockMvc.perform(get("/api/compliance/audit-logs"))
                .andExpect(status().isOk());
    }

}
