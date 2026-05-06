package org.cts.adm.finguard.RiskAlert.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cts.adm.finguard.RiskAlert.Enum.RiskAlertStatus;
import org.cts.adm.finguard.RiskAlert.Model.RiskAlert;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for RiskAlert responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskAlertResponseDto {

    private Long alertId;
    private String transactionId;
    private BigDecimal riskScore;
    private LocalDateTime alertDate;
    private RiskAlertStatus status;
    private String riskLevel;

    public static RiskAlertResponseDto fromEntity(RiskAlert alert) {
        if (alert == null) {
            return null;
        }

        String riskLevel = determineRiskLevel(alert.getRiskScore());

        return new RiskAlertResponseDto(
                alert.getAlertId(),
                alert.getTransactionId(),
                alert.getRiskScore(),
                alert.getAlertDate(),
                alert.getStatus(),
                riskLevel
        );
    }

    public static String determineRiskLevel(BigDecimal riskScore) {
        if (riskScore == null) {
            return "UNKNOWN";
        }

        if (riskScore.compareTo(new BigDecimal("80")) >= 0) {
            return "CRITICAL";
        } else if (riskScore.compareTo(new BigDecimal("50")) >= 0) {
            return "HIGH";
        } else if (riskScore.compareTo(new BigDecimal("25")) >= 0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}
