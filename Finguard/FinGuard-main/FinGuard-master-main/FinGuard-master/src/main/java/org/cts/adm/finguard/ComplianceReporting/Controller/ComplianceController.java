package org.cts.adm.finguard.ComplianceReporting.Controller;

import org.cts.adm.finguard.ComplianceReporting.Model.ComplianceReport;
import org.cts.adm.finguard.ComplianceReporting.Service.ComplianceService;
import org.springframework.web.bind.annotation.*;
import org.cts.adm.finguard.ComplianceReporting.Model.AuditTrail;

import java.util.List;

@RestController
@RequestMapping("/api/compliance")
public class ComplianceController {

    private final ComplianceService service;

    public ComplianceController(ComplianceService service) {
        this.service = service;
    }

    // ✅ Generate report
    @PostMapping("/generate")
    public ComplianceReport generate(@RequestParam String user) {
        return service.generateReport(user);
    }

    // ✅ Get reports
    @GetMapping
    public List<ComplianceReport> getAll() {
        return service.getReports();
    }

    // ✅ Export report
    @GetMapping("/export/{id}")
    public String export(@PathVariable Long id) {
        return service.exportReport(id);
    }



    @GetMapping("/audit-logs")
    public List<AuditTrail> getAuditLogs() {
        return service.getAuditLogs();
    }
}