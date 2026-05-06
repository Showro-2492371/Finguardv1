package org.cts.adm.finguard.RiskAlert.Controller;

import org.cts.adm.finguard.RiskAlert.Dto.RiskAlertResponseDto;
import org.cts.adm.finguard.RiskAlert.Enum.RiskAlertStatus;
import org.cts.adm.finguard.RiskAlert.Exception.RiskAlertNotFoundException;
import org.cts.adm.finguard.RiskAlert.Model.RiskAlert;
import org.cts.adm.finguard.RiskAlert.Repository.RiskAlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller for RiskAlert operations
 */
@RestController
@RequestMapping("/api/admin/risk")
public class RiskAlertController {

    private static final Logger log = LoggerFactory.getLogger(RiskAlertController.class);

    private final RiskAlertRepository repo;

    public RiskAlertController(RiskAlertRepository repo) {
        this.repo = repo;
    }

    /**
     * Get all alerts or filter by status
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/alerts")
    public ResponseEntity<List<RiskAlertResponseDto>> list(@RequestParam(required = false) RiskAlertStatus status) {
        log.info("Fetching alerts with status filter: {}", status);

        List<RiskAlert> alerts = (status == null) ? repo.findAll() : repo.findByStatus(status);
        List<RiskAlertResponseDto> responseDtos = new ArrayList<>();

        for (RiskAlert alert : alerts) {
            responseDtos.add(RiskAlertResponseDto.fromEntity(alert));
        }

        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Get a specific alert by ID
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/alerts/{id}")
    public ResponseEntity<RiskAlertResponseDto> get(@PathVariable Long id) {
        log.info("Fetching alert with ID: {}", id);

        RiskAlert alert = repo.findById(id)
                .orElseThrow(() -> new RiskAlertNotFoundException(id));

        return ResponseEntity.ok(RiskAlertResponseDto.fromEntity(alert));
    }
}
