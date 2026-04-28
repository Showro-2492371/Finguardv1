package org.cts.adm.finguard.ComplianceReporting.Controller;

import org.cts.adm.finguard.ComplianceReporting.Model.ComplianceReport;
import org.cts.adm.finguard.ComplianceReporting.Service.ComplianceService;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerLoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.cts.adm.finguard.ComplianceReporting.Model.AuditTrail;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/compliance")
public class ComplianceController {

    private final ComplianceService service;
    private final CustomerLoginService customerLoginService;

    public ComplianceController(ComplianceService service ,CustomerLoginService customerLoginService)
    {
        this.service = service;
        this.customerLoginService = customerLoginService;

    }


    @PostMapping("/generate")
    public ComplianceReport generate(@RequestParam String user) {

        return service.generateReport(user);
    }


    @GetMapping("/allreports")
    public List<ComplianceReport> getAll() {
        return service.getReports();
    }

    @GetMapping("/export/csv/{id}")
    public ResponseEntity<String> exportCSV(@PathVariable Long id,
                                            @RequestParam String user) throws IOException {

      Customer  customer = customerLoginService.getCustomerById(id);
      if(customer == null){
          System.out.println("No customer");
      }

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