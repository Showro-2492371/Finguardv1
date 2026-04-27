package org.cts.adm.finguard.RiskAlert.Dto;

import jakarta.validation.constraints.NotNull;
import org.cts.adm.finguard.RiskAlert.Enum.RiskAlertStatus;

public class StatusUpdateDto {

    @NotNull(message = "Alert ID is required")
    private Long alertId;

    @NotNull(message = "Status is required")
    private RiskAlertStatus status;

    public StatusUpdateDto() {}
    
    public StatusUpdateDto(Long alertId, RiskAlertStatus status) {
        this.alertId = alertId;
        this.status = status;
    }

    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }

    public RiskAlertStatus getStatus() { return status; }
    public void setStatus(RiskAlertStatus status) { this.status = status; }
}
