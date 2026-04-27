package org.cts.adm.finguard.ComplianceReporting.Controller;

import org.cts.adm.finguard.ComplianceReporting.Model.ComplianceReport;
import org.cts.adm.finguard.ComplianceReporting.Service.ComplianceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.cts.adm.finguard.ComplianceReporting.Model.AuditTrail;

import java.io.IOException;
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
    @GetMapping("/allreports")
    public List<ComplianceReport> getAll() {
        return service.getReports();
    }

    // ✅ Export report
    @GetMapping("/export/csv/{id}")
    public ResponseEntity<String> exportCSV(@PathVariable Long id,
                                            @RequestParam String user) throws IOException {

        String csvData = service.exportReport(id, user);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=report.csv")
                .header("Content-Type", "text/csv")
                .body(csvData);
    }

    @GetMapping("/audit-logs")
    public List<AuditTrail> getAuditLogs() {
        return service.getAuditLogs();
    }
}