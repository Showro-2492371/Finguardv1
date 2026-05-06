package org.cts.adm.finguard.ComplianceReporting.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cts.adm.finguard.ComplianceReporting.DTO.ComplianceReportDTO;
import org.cts.adm.finguard.ComplianceReporting.Model.ComplianceReport;
import org.cts.adm.finguard.ComplianceReporting.Service.ComplianceService;
import org.cts.adm.finguard.CustomerOnboarding.Model.Customer;
import org.cts.adm.finguard.CustomerOnboarding.Repository.CustomerRepository;
import org.cts.adm.finguard.CustomerOnboarding.Service.CustomerLoginService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.cts.adm.finguard.ComplianceReporting.Model.AuditTrail;

import java.io.IOException;
import java.time.LocalDate;
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
    @PreAuthorize("hasRole('ADMIN')")
    public List<ComplianceReportDTO> generate(@RequestParam String user) {
        return service.generateReports(user);
    }

    @PostMapping("/generate/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ComplianceReportDTO generateForCustomer(
            @PathVariable Long customerId,
            @RequestParam String user) {

        log.info("API: Generate report for customerId={}", customerId);

        return service.generateReportByCustomer(customerId, user);
    }


    @GetMapping("/allreports")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ComplianceReportDTO> getAll() {
        return service.getReports();
    }


    @GetMapping("/reports/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ComplianceReportDTO> getByCustomer(@PathVariable Long customerId) {
        return service.getByCustomer(customerId);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> summary() {
        return service.getSummary();
    }

    @GetMapping("/export/csv/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long customerId) {
        service.deleteReport(customerId);
        return "Deleted";
    }



    @GetMapping("/reports/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ComplianceReportDTO> filterReports(

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false) Integer month,

            @RequestParam(required = false) Integer year) {

        log.info("API: Filter reports");

        return service.filterReports(startDate, endDate, month, year);
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditTrail> getAuditLogs() {
        return service.getAuditLogs();
    }
}