package org.cts.adm.finguard.RiskAlert.Exception;

/**
 * Exception thrown when a RiskAlert is not found
 */
public class RiskAlertNotFoundException extends RuntimeException {

    public RiskAlertNotFoundException(Long alertId) {
        super("RiskAlert not found with ID: " + alertId);
    }

    public RiskAlertNotFoundException(String transactionId) {
        super("RiskAlert not found for transaction: " + transactionId);
    }
}
