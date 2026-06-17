package org.cts.adm.finguard.Analytics.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO returned by the Analytics API.
 * Matches FraudAnalytics entity fields; keeps the entity from leaking into the API contract.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FraudAnalyticsDTO {

    private Long analyticsId;

    /** Fraud rate as a percentage (0–100) */
    private double fraudRate;

    /** Average risk score – represents the risk trend */
    private double riskTrend;

    private long totalTransactions;
    private long flaggedTransactions;
    private long blockedTransactions;
    private long activeCustomers;
    private LocalDateTime generatedDate;
}

