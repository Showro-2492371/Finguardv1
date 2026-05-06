package org.cts.adm.finguard.RiskAlert.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cts.adm.finguard.RiskAlert.Enum.RiskAlertStatus;
import org.cts.adm.finguard.RiskAlert.Model.RiskAlert;
import org.cts.adm.finguard.RiskAlert.Repository.RiskAlertRepository;
import org.cts.adm.finguard.TransactionMonitoring.Model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service for RiskAlert operations - handles risk evaluation and alert creation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAlertService {

    private final RiskAlertRepository riskAlertRepository;

    @Value("${risk.escalation-threshold:80}")
    private BigDecimal escalationThreshold;

    /**
     * Calculates risk score for a transaction
     * Uses TransactionMonitoring score if available, otherwise computes based on amount and channel
     */
    public BigDecimal calculateRiskScore(Transaction transaction) {
        // Use existing risk score from TransactionMonitoring if available
        if (transaction.getRiskScore() != null && transaction.getRiskScore() > 0) {
            return BigDecimal.valueOf(transaction.getRiskScore());
        }

        // Calculate risk score based on transaction characteristics
        BigDecimal score = BigDecimal.ZERO;

        if (transaction.getAmount() != null) {
            BigDecimal amount = transaction.getAmount();
            if (amount.compareTo(new BigDecimal("50000")) > 0) {
                score = score.add(new BigDecimal("30"));
            } else if (amount.compareTo(new BigDecimal("20000")) > 0) {
                score = score.add(new BigDecimal("15"));
            } else if (amount.compareTo(new BigDecimal("5000")) > 0) {
                score = score.add(new BigDecimal("5"));
            }
        }

        // Add risk for ATM transactions
        if (transaction.getChannel() != null &&
                "ATM".equalsIgnoreCase(transaction.getChannel().name())) {
            score = score.add(new BigDecimal("5"));
        }

        return score.min(new BigDecimal("100"));
    }

    /**
     * Evaluates transaction and creates/updates risk alert
     * Called by TransactionMonitoringService after transaction creation
     */
    @Transactional
    public RiskAlert evaluateAndCreateAlert(Transaction transaction) {
        if (transaction.getTransactionId() == null) {
            throw new IllegalArgumentException("Transaction must have an ID");
        }

        String transactionId = transaction.getTransactionId();
        log.info("Evaluating risk for transaction: {}", transactionId);

        BigDecimal riskScore = calculateRiskScore(transaction);
        RiskAlertStatus status = determineStatus(riskScore);

        // Find existing alert or create new one
        RiskAlert alert = riskAlertRepository.findByTransactionId(transactionId)
                .orElse(new RiskAlert());

        alert.setTransactionId(transactionId);
        alert.setRiskScore(riskScore);
        alert.setStatus(status);
        alert.setAlertDate(LocalDateTime.now());

        RiskAlert savedAlert = riskAlertRepository.save(alert);
        log.info("Created/Updated RiskAlert ID: {} with status: {} and score: {}",
                savedAlert.getAlertId(), status, riskScore);

        return savedAlert;
    }

    /**
     * Determines alert status based on risk score
     */
    private RiskAlertStatus determineStatus(BigDecimal riskScore) {
        return riskScore.compareTo(escalationThreshold) >= 0
                ? RiskAlertStatus.ESCALATED
                : RiskAlertStatus.NEW;
    }
}
