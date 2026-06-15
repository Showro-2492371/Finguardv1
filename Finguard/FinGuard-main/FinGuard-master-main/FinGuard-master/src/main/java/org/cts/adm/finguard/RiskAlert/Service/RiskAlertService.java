package org.cts.adm.finguard.RiskAlert.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cts.adm.finguard.RiskAlert.Enum.RiskAlertStatus;
import org.cts.adm.finguard.RiskAlert.Exception.RiskAlertNotFoundException;
import org.cts.adm.finguard.RiskAlert.Model.RiskAlert;
import org.cts.adm.finguard.RiskAlert.Repository.RiskAlertRepository;
import org.cts.adm.finguard.TransactionMonitoring.Model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for RiskAlert operations - handles risk evaluation and alert creation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAlertService {

    private static final Map<RiskAlertStatus, EnumSet<RiskAlertStatus>> ALLOWED_TRANSITIONS = buildTransitionMap();

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
        RiskAlertStatus computedStatus = determineStatus(riskScore);

        // Find existing alert or create new one
        RiskAlert alert = riskAlertRepository.findByTransactionId(transactionId)
                .orElse(new RiskAlert());

        RiskAlertStatus existingStatus = normalizeStatus(alert.getStatus());

        alert.setTransactionId(transactionId);
        alert.setRiskScore(riskScore);
        alert.setStatus(resolveStatusForReevaluation(existingStatus, computedStatus));
        alert.setAlertDate(LocalDateTime.now());

        RiskAlert savedAlert = riskAlertRepository.save(alert);
        log.info("Created/Updated RiskAlert ID: {} with status: {} and score: {}",
                savedAlert.getAlertId(), savedAlert.getStatus(), riskScore);

        return savedAlert;
    }

    @Transactional(readOnly = true)
    public List<RiskAlert> listAlerts(RiskAlertStatus status) {
        RiskAlertStatus normalizedFilter = normalizeStatus(status);
        List<RiskAlert> alerts = normalizedFilter == null
                ? riskAlertRepository.findAll()
                : riskAlertRepository.findByStatusIn(getCompatibleStatuses(normalizedFilter));
        alerts.forEach(this::normalizeEntityStatusInMemory);
        return alerts;
    }

    @Transactional(readOnly = true)
    public RiskAlert getAlertById(Long alertId) {
        RiskAlert alert = riskAlertRepository.findById(alertId)
                .orElseThrow(() -> new RiskAlertNotFoundException(alertId));
        normalizeEntityStatusInMemory(alert);
        return alert;
    }

    @Transactional
    public RiskAlert updateStatus(Long alertId, RiskAlertStatus requestedStatus) {
        RiskAlert alert = riskAlertRepository.findById(alertId)
                .orElseThrow(() -> new RiskAlertNotFoundException(alertId));

        RiskAlertStatus currentStatus = normalizeStatus(alert.getStatus());
        RiskAlertStatus targetStatus = normalizeStatus(requestedStatus);

        if (targetStatus == null) {
            throw new IllegalArgumentException("Status is required");
        }

        if (currentStatus != null && currentStatus == targetStatus) {
            return alert;
        }

        validateTransition(currentStatus, targetStatus);

        alert.setStatus(targetStatus);
        alert.setAlertDate(LocalDateTime.now());
        RiskAlert saved = riskAlertRepository.save(alert);

        log.info("Risk alert status updated: alertId={} from={} to={}",
                alertId, currentStatus, targetStatus);
        return saved;
    }

    /**
     * Determines alert status based on risk score
     */
    private RiskAlertStatus determineStatus(BigDecimal riskScore) {
        return riskScore.compareTo(escalationThreshold) >= 0
                ? RiskAlertStatus.ESCALATED
                : RiskAlertStatus.NEW;
    }

    private RiskAlertStatus resolveStatusForReevaluation(RiskAlertStatus existingStatus,
                                                         RiskAlertStatus computedStatus) {
        if (existingStatus == null) {
            return computedStatus;
        }

        // Keep terminal decisions stable unless explicitly changed by admin workflow.
        if (existingStatus == RiskAlertStatus.CLOSED || existingStatus == RiskAlertStatus.RESOLVED) {
            return existingStatus;
        }

        // Do not automatically downgrade escalated/reviewed alerts during re-evaluation.
        if (existingStatus == RiskAlertStatus.ESCALATED || existingStatus == RiskAlertStatus.REVIEWED) {
            return existingStatus;
        }

        // NEW alerts can be auto-escalated when the current score crosses the threshold.
        if (existingStatus == RiskAlertStatus.NEW && computedStatus == RiskAlertStatus.ESCALATED) {
            return RiskAlertStatus.ESCALATED;
        }

        return existingStatus;
    }

    private void validateTransition(RiskAlertStatus currentStatus, RiskAlertStatus targetStatus) {
        if (currentStatus == null) {
            return;
        }
        EnumSet<RiskAlertStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(RiskAlertStatus.class));
        if (!allowed.contains(targetStatus)) {
            throw new IllegalArgumentException(
                    String.format("Invalid status transition from %s to %s", currentStatus, targetStatus)
            );
        }
    }

    private RiskAlertStatus normalizeStatus(RiskAlertStatus status) {
        return RiskAlertStatus.normalizeForWorkflow(status);
    }

    private void normalizeEntityStatusInMemory(RiskAlert alert) {
        if (alert == null) {
            return;
        }
        alert.setStatus(normalizeStatus(alert.getStatus()));
    }

    private Set<RiskAlertStatus> getCompatibleStatuses(RiskAlertStatus normalizedFilter) {
        return switch (normalizedFilter) {
            case NEW -> EnumSet.of(RiskAlertStatus.NEW, RiskAlertStatus.FLAGGED);
            case ESCALATED -> EnumSet.of(RiskAlertStatus.ESCALATED, RiskAlertStatus.BLOCKED);
            case RESOLVED -> EnumSet.of(RiskAlertStatus.RESOLVED, RiskAlertStatus.SUCCESS);
            default -> EnumSet.of(normalizedFilter);
        };
    }

    private static Map<RiskAlertStatus, EnumSet<RiskAlertStatus>> buildTransitionMap() {
        Map<RiskAlertStatus, EnumSet<RiskAlertStatus>> map = new EnumMap<>(RiskAlertStatus.class);
        map.put(RiskAlertStatus.NEW, EnumSet.of(RiskAlertStatus.REVIEWED, RiskAlertStatus.ESCALATED, RiskAlertStatus.CLOSED));
        map.put(RiskAlertStatus.REVIEWED, EnumSet.of(RiskAlertStatus.ESCALATED, RiskAlertStatus.RESOLVED, RiskAlertStatus.CLOSED));
        map.put(RiskAlertStatus.ESCALATED, EnumSet.of(RiskAlertStatus.REVIEWED, RiskAlertStatus.RESOLVED, RiskAlertStatus.CLOSED));
        map.put(RiskAlertStatus.RESOLVED, EnumSet.of(RiskAlertStatus.CLOSED));
        map.put(RiskAlertStatus.CLOSED, EnumSet.noneOf(RiskAlertStatus.class));
        return Collections.unmodifiableMap(map);
    }
}
