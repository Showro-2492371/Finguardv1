package org.cts.adm.finguard.RiskAlert.Service;

import org.cts.adm.finguard.RiskAlert.Enum.RiskAlertStatus;
import org.cts.adm.finguard.RiskAlert.Model.RiskAlert;
import org.cts.adm.finguard.RiskAlert.Repository.RiskAlertRepository;
import org.cts.adm.finguard.TransactionMonitoring.Model.Transaction;
import org.cts.adm.finguard.TransactionMonitoring.Enum.TransactionStatus;
import org.cts.adm.finguard.TransactionMonitoring.Repository.TransactionMonitoringRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RiskAlertService {

    private final RiskAlertRepository repo;
    private final TransactionMonitoringRepository txRepo; // optional defensive check

    @Value("${risk.escalation-threshold:80}")
    private BigDecimal escalationThreshold;

    private static final Logger logger = LoggerFactory.getLogger(RiskAlertService.class);

    public RiskAlertService(RiskAlertRepository repo, TransactionMonitoringRepository txRepo) {
        this.repo = repo;
        this.txRepo = txRepo;
    }

    public BigDecimal computeRiskScore(Transaction tx) {
        BigDecimal score = BigDecimal.ZERO;

        if (tx.getRiskScore() != null) {
            score = BigDecimal.valueOf(tx.getRiskScore());
        } else {
            if (tx.getAmount() != null) {
                if (tx.getAmount().compareTo(new BigDecimal("50000")) > 0) score = score.add(new BigDecimal("30"));
                else if (tx.getAmount().compareTo(new BigDecimal("20000")) > 0) score = score.add(new BigDecimal("15"));
                else if (tx.getAmount().compareTo(new BigDecimal("5000")) > 0) score = score.add(new BigDecimal("5"));
            }
            if (tx.getChannel() != null && tx.getChannel().name().equalsIgnoreCase("ATM")) score = score.add(new BigDecimal("5"));
        }

        logger.debug("Computed risk score: {}", score);

        return score.min(new BigDecimal("100"));
    }

    @Transactional
    public RiskAlert evaluateAndPersist(Transaction tx) {
        if (tx.getTransactionId() == null) {
            throw new IllegalArgumentException("Transaction must have an ID");
        }

        String txId = tx.getTransactionId();
        logger.info("Processing transaction {} with risk score: {}", txId, tx.getRiskScore());

        BigDecimal computed = computeRiskScore(tx);

        // Use the risk score already calculated by TransactionMonitoringService
        // instead of recalculating with the simpler logic
        BigDecimal finalRiskScore;
        if (tx.getRiskScore() != null && tx.getRiskScore() > 0) {
            finalRiskScore = BigDecimal.valueOf(tx.getRiskScore());
            logger.info("Using TransactionMonitoring risk score: {}", finalRiskScore);
        } else {
            finalRiskScore = computed;
            logger.info("Using computed risk score: {}", finalRiskScore);
        }

        RiskAlert alert = repo.findByTransactionId(txId).orElse(new RiskAlert());
        alert.setTransactionId(txId);
        alert.setRiskScore(finalRiskScore);
        alert.setAlertDate(LocalDateTime.now());

        // Use transaction status to determine alert status
        if (tx.getStatus() != null) {
            switch (tx.getStatus()) {
                case BLOCKED:
                    alert.setStatus(RiskAlertStatus.BLOCKED);
                    break;
                case FLAGGED:
                    alert.setStatus(RiskAlertStatus.FLAGGED);
                    break;
                case SUCCESS:
                    alert.setStatus(RiskAlertStatus.SUCCESS);
                    break;
                default:
                    alert.setStatus(finalRiskScore.compareTo(escalationThreshold) >= 0
                        ? RiskAlertStatus.ESCALATED : RiskAlertStatus.NEW);
            }
        } else {
            alert.setStatus(finalRiskScore.compareTo(escalationThreshold) >= 0
                ? RiskAlertStatus.ESCALATED : RiskAlertStatus.NEW);
        }

        logger.info("Created RiskAlert with risk score: {} and status: {}", finalRiskScore, alert.getStatus());
        return repo.save(alert);
    }


    @Transactional
    public RiskAlert updateAlertStatus(Long alertId, RiskAlertStatus status) {
        RiskAlert alert = repo.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("RiskAlert not found: " + alertId));
        alert.setStatus(status);
        alert.setAlertDate(LocalDateTime.now());
        return repo.save(alert);
    }

}
