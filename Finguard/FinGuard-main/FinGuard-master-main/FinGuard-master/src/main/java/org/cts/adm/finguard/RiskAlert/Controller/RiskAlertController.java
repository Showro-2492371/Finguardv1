package org.cts.adm.finguard.RiskAlert.Controller;

import org.cts.adm.finguard.RiskAlert.Dto.StatusUpdateDto;
import org.cts.adm.finguard.RiskAlert.Enum.RiskAlertStatus;
import org.cts.adm.finguard.RiskAlert.Model.RiskAlert;
import org.cts.adm.finguard.RiskAlert.Repository.RiskAlertRepository;
import org.cts.adm.finguard.RiskAlert.Service.RiskAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/risk")
public class RiskAlertController {

    private final RiskAlertService service;
    private final RiskAlertRepository repo;

    public RiskAlertController(RiskAlertService service, RiskAlertRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<RiskAlert>> list(@RequestParam(required = false) RiskAlertStatus status) {
        List<RiskAlert> list = (status == null) ? repo.findAll() : repo.findByStatus(status.name());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/alerts/{id}")
    public ResponseEntity<RiskAlert> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/alerts/{id}/status")
    public ResponseEntity<RiskAlert> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateDto body) {
        RiskAlert updated = service.updateAlertStatus(id, body.getStatus());
        return ResponseEntity.ok(updated);
    }
}
