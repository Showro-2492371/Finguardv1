package org.cts.adm.finguard.TransactionMonitoring.Dto;

import org.cts.adm.finguard.TransactionMonitoring.Enum.TransactionStatus;

import java.time.LocalDateTime;

public class FraudCheckResponse {

    private String transactionId;
    private Long customerId;
    private boolean fraudDetected;
    private int riskScore;
    private String fraudReason;
    private TransactionStatus status;
    private LocalDateTime createdAt;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public boolean isFraudDetected() {
        return fraudDetected;
    }

    public void setFraudDetected(boolean fraudDetected) {
        this.fraudDetected = fraudDetected;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getFraudReason() {
        return fraudReason;
    }

    public void setFraudReason(String fraudReason) {
        this.fraudReason = fraudReason;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
