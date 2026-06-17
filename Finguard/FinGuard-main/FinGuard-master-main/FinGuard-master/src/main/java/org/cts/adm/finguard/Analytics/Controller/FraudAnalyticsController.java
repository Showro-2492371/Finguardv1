package org.cts.adm.finguard.Analytics.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cts.adm.finguard.Analytics.DTO.FraudAnalyticsDTO;
import org.cts.adm.finguard.Analytics.Service.FraudAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Analytics & Dashboard Module (Module 4.5)
 *
 * All endpoints are ADMIN-only. The SecurityConfig already locks down /api/analytics/**
 * to ROLE_ADMIN; the @PreAuthorize annotations provide an additional in-method guard.
 */
@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class FraudAnalyticsController {

    private final FraudAnalyticsService analyticsService;

    /**
     * Triggers a fresh analytics snapshot from current transaction data.
     * Admins use this to refresh the dashboard metrics on demand.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/generate")
    public ResponseEntity<FraudAnalyticsDTO> generateSnapshot() {
        log.info("Admin requested analytics snapshot generation");
        FraudAnalyticsDTO snapshot = analyticsService.generateSnapshot();
        return ResponseEntity.ok(snapshot);
    }

    /**
     * Returns the most recent analytics snapshot.
     * If no snapshot exists yet it is auto-generated from current data.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/latest")
    public ResponseEntity<FraudAnalyticsDTO> getLatest() {
        log.info("Fetching latest analytics snapshot");
        return ResponseEntity.ok(analyticsService.getLatestSnapshot());
    }

    /**
     * Returns up to 12 historical snapshots for trend charts (newest first).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/history")
    public ResponseEntity<List<FraudAnalyticsDTO>> getHistory() {
        log.info("Fetching analytics trend history");
        return ResponseEntity.ok(analyticsService.getTrendHistory());
    }
}

