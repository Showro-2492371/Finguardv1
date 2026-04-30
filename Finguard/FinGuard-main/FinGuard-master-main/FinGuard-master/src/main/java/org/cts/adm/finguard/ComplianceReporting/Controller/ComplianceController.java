package org.cts.adm.finguard.ComplianceReporting.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cts.adm.finguard.ComplianceReporting.DTO.ComplianceReportDTO;
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
import java.util.Map;

@RestController
@RequestMapping("/api/compliance")
@Slf4j
public class ComplianceController {

    private final ComplianceService service;
    private final CustomerLoginService customerLoginService;

    public ComplianceController(ComplianceService service ,CustomerLoginService customerLoginService)
    {
        this.service = service;
        this.customerLoginService = customerLoginService;

    }


    @PostMapping("/generate")
    public List<ComplianceReportDTO> generate(@RequestParam String user) {
        return service.generateReports(user);
    }


    @GetMapping("/allreports")
    public List<ComplianceReportDTO> getAll() {
        return service.getReports();
    }


    @GetMapping("/reports/{customerId}")
    public List<ComplianceReportDTO> getByCustomer(@PathVariable Long customerId) {
        return service.getByCustomer(customerId);
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return service.getSummary();
    }

    @GetMapping("/export/csv/{id}")
    public ResponseEntity<String> exportCSV(@PathVariable Long id,
                                            @RequestParam String user) throws IOException {

      Customer  customer = customerLoginService.getCustomerById(id);
      if(customer == null){
          System.out.println("No customer");
      }

        String file = service.exportReport(id, user);

        return ResponseEntity.ok("CSV generated at: " + file);
    }

    @DeleteMapping("/reports/{customerId}")
    public String delete(@PathVariable Long customerId) {
        service.deleteReport(customerId);
        return "Deleted";
    }

    @GetMapping("/audit-logs")
    public List<AuditTrail> getAuditLogs() {
        return service.getAuditLogs();
    }
}