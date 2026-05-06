package org.cts.adm.finguard.RiskAlert.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cts.adm.finguard.RiskAlert.Enum.RiskAlertStatus;
import org.cts.adm.finguard.TransactionMonitoring.Model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_alert")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long alertId;

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
}
