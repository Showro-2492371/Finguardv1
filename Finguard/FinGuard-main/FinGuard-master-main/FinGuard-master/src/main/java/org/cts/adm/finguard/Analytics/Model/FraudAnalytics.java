package org.cts.adm.finguard.Analytics.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Analytics entity – stores a snapshot of fraud metrics at a point in time.
 *
 * Spec: Analytics & Dashboard Module (4.5)
 * Entity: FraudAnalytics
 *   - AnalyticsID
 *   - Metrics: FraudRate, RiskTrend
 *   - GeneratedDate
 */
@Entity
@Table(name = "fraud_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analytics_id")
    private Long analyticsId;

    /** Percentage of flagged/blocked transactions out of total transactions (0–100) */
    @Column(name = "fraud_rate", nullable = false)
    private double fraudRate;

    /** Average risk score across all transactions in this snapshot period */
    @Column(name = "risk_trend", nullable = false)
    private double riskTrend;

    /** Total number of transactions analysed */
    @Column(name = "total_transactions", nullable = false)
    private long totalTransactions;

    /** Number of flagged/blocked transactions (fraud suspected) */
    @Column(name = "flagged_transactions", nullable = false)
    private long flaggedTransactions;

    /** Number of transactions fully blocked */
    @Column(name = "blocked_transactions", nullable = false)
    private long blockedTransactions;

    /** Total number of unique customers with at least one transaction */
    @Column(name = "active_customers", nullable = false)
    private long activeCustomers;

    /** Timestamp when this analytics snapshot was generated */
    @Column(name = "generated_date", nullable = false)
    private LocalDateTime generatedDate;
}

