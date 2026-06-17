package org.cts.adm.finguard.Analytics.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cts.adm.finguard.Analytics.DTO.FraudAnalyticsDTO;
import org.cts.adm.finguard.Analytics.Model.FraudAnalytics;
import org.cts.adm.finguard.Analytics.Repository.FraudAnalyticsRepository;
import org.cts.adm.finguard.TransactionMonitoring.Enum.TransactionStatus;
import org.cts.adm.finguard.TransactionMonitoring.Model.Transaction;
import org.cts.adm.finguard.TransactionMonitoring.Repository.TransactionMonitoringRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Analytics & Dashboard Module – Service (Module 4.5)
 *
 * Derives fraud metrics from live transaction and risk alert data.
 * Does NOT rely on AI models (per project constraints) – uses rule-based aggregation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudAnalyticsService {

    private final TransactionMonitoringRepository transactionRepo;
    private final FraudAnalyticsRepository analyticsRepo;

    /**
     * Generates a new analytics snapshot from current transaction data and persists it.
     * Called by admins or scheduled tasks to refresh analytics.
     *
     * @return the persisted FraudAnalyticsDTO snapshot
     */
    public FraudAnalyticsDTO generateSnapshot() {
        log.info("Generating fraud analytics snapshot...");

        List<Transaction> all = transactionRepo.findAll();
        long total = all.size();

        if (total == 0) {
            log.warn("No transactions found – returning zero-valued analytics snapshot");
            FraudAnalytics empty = FraudAnalytics.builder()
                    .fraudRate(0)
                    .riskTrend(0)
                    .totalTransactions(0)
                    .flaggedTransactions(0)
                    .blockedTransactions(0)
                    .activeCustomers(0)
                    .generatedDate(LocalDateTime.now())
                    .build();
            return toDTO(analyticsRepo.save(empty));
        }

        long flagged  = all.stream().filter(t -> t.getStatus() == TransactionStatus.FLAGGED).count();
        long blocked  = all.stream().filter(t -> t.getStatus() == TransactionStatus.BLOCKED).count();
        long suspected = flagged + blocked;

        // Fraud rate = (suspected / total) * 100
        double fraudRate = (double) suspected / total * 100.0;

        // Risk trend = average risk score across all transactions
        OptionalDouble avgRisk = all.stream()
                .filter(t -> t.getRiskScore() != null)
                .mapToInt(Transaction::getRiskScore)
                .average();
        double riskTrend = avgRisk.orElse(0.0);

        // Unique customers with at least one transaction
        long activeCustomers = all.stream()
                .map(t -> t.getCustomer().getCustomerId())
                .distinct()
                .count();

        FraudAnalytics snapshot = FraudAnalytics.builder()
                .fraudRate(Math.round(fraudRate * 100.0) / 100.0)
                .riskTrend(Math.round(riskTrend * 100.0) / 100.0)
                .totalTransactions(total)
                .flaggedTransactions(flagged)
                .blockedTransactions(blocked)
                .activeCustomers(activeCustomers)
                .generatedDate(LocalDateTime.now())
                .build();

        FraudAnalytics saved = analyticsRepo.save(snapshot);
        log.info("Analytics snapshot generated: fraudRate={}% riskTrend={} total={}",
                saved.getFraudRate(), saved.getRiskTrend(), saved.getTotalTransactions());
        return toDTO(saved);
    }

    /**
     * Returns the most recent analytics snapshot (or generates one if none exists).
     */
    public FraudAnalyticsDTO getLatestSnapshot() {
        return analyticsRepo.findTop12ByOrderByGeneratedDateDesc()
                .stream()
                .findFirst()
                .map(this::toDTO)
                .orElseGet(this::generateSnapshot);
    }

    /**
     * Returns the latest 12 snapshots for trend charting (newest first).
     */
    public List<FraudAnalyticsDTO> getTrendHistory() {
        List<FraudAnalytics> history = analyticsRepo.findTop12ByOrderByGeneratedDateDesc();
        if (history.isEmpty()) {
            // Auto-generate first snapshot
            generateSnapshot();
            history = analyticsRepo.findTop12ByOrderByGeneratedDateDesc();
        }
        return history.stream().map(this::toDTO).toList();
    }

    private FraudAnalyticsDTO toDTO(FraudAnalytics a) {
        return FraudAnalyticsDTO.builder()
                .analyticsId(a.getAnalyticsId())
                .fraudRate(a.getFraudRate())
                .riskTrend(a.getRiskTrend())
                .totalTransactions(a.getTotalTransactions())
                .flaggedTransactions(a.getFlaggedTransactions())
                .blockedTransactions(a.getBlockedTransactions())
                .activeCustomers(a.getActiveCustomers())
                .generatedDate(a.getGeneratedDate())
                .build();
    }
}

