package org.cts.adm.finguard.RiskAlert.Controller;

import org.cts.adm.finguard.RiskAlert.Dto.RiskAlertResponseDto;
import org.cts.adm.finguard.RiskAlert.Dto.StatusUpdateDto;
import org.cts.adm.finguard.RiskAlert.Enum.RiskAlertStatus;
import org.cts.adm.finguard.RiskAlert.Model.RiskAlert;
import org.cts.adm.finguard.RiskAlert.Service.RiskAlertService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for RiskAlert operations
 */
@RestController
@RequestMapping("/api/admin/risk")
public class RiskAlertController {

    private static final Logger log = LoggerFactory.getLogger(RiskAlertController.class);

    private final RiskAlertService service;

    public RiskAlertController(RiskAlertService service) {
        this.service = service;
    }

    /**
     * Get all alerts or filter by status
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/alerts")
    public ResponseEntity<List<RiskAlertResponseDto>> list(@RequestParam(required = false) RiskAlertStatus status) {
        log.info("Fetching alerts with status filter: {}", status);
        List<RiskAlertResponseDto> response = service.listAlerts(status)
                .stream()
                .map(RiskAlertResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific alert by ID
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/alerts/{id}")
    public ResponseEntity<RiskAlertResponseDto> get(@PathVariable Long id) {
        log.info("Fetching alert with ID: {}", id);
        RiskAlert alert = service.getAlertById(id);
        return ResponseEntity.ok(RiskAlertResponseDto.fromEntity(alert));
    }

    /**
     * Update status for an existing alert following lifecycle transition rules.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/alerts/{id}/status")
    public ResponseEntity<RiskAlertResponseDto> updateStatus(@PathVariable Long id,
                                                             @Valid @RequestBody StatusUpdateDto request) {
        log.info("Updating status for alert ID: {} to {}", id, request.getStatus());
        RiskAlert updated = service.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(RiskAlertResponseDto.fromEntity(updated));
    }
}
