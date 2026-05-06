package org.cts.adm.finguard.RiskAlert.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cts.adm.finguard.RiskAlert.Enum.RiskAlertStatus;

/**
 * DTO for updating RiskAlert status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateDto {

    @NotNull(message = "Status is required")
    private RiskAlertStatus status;
}
