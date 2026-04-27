package org.cts.adm.finguard.RiskAlert.Model;
import jakarta.persistence.*;
import org.cts.adm.finguard.RiskAlert.Enum.RiskAlertStatus;
import org.cts.adm.finguard.TransactionMonitoring.Model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_alert")
public class RiskAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long alertId;

    // column that holds FK value
    @Column(name = "transaction_id", nullable = false, length = 150)
    private String transactionId;

    @Column(name = "risk_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Column(name = "alert_date")
    private LocalDateTime alertDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private RiskAlertStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_risk_transaction"))
    private Transaction transaction;

    public RiskAlert() {}

    // Getters and setters
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public BigDecimal getRiskScore() { return riskScore; }
    public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }

    public LocalDateTime getAlertDate() { return alertDate; }
    public void setAlertDate(LocalDateTime alertDate) { this.alertDate = alertDate; }

    public RiskAlertStatus getStatus() { return status; }
    public void setStatus(RiskAlertStatus status) { this.status = status; }

    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }
}
