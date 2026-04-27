package org.cts.adm.finguard.ComplianceReporting.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private String performedBy;
    private LocalDateTime timestamp;

    // Constructors
    public AuditTrail() {}

    public AuditTrail(String action, String performedBy, LocalDateTime timestamp) {
        this.action = action;
        this.performedBy = performedBy;
        this.timestamp = timestamp;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}