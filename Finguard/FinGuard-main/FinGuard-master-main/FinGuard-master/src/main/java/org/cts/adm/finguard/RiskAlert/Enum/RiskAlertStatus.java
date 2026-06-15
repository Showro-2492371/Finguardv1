package org.cts.adm.finguard.RiskAlert.Enum;

public enum RiskAlertStatus {
    NEW,
    REVIEWED,
    ESCALATED,
    RESOLVED,
    CLOSED,
    /**
     * Legacy transaction status values kept for backward compatibility.
     * They are normalized to lifecycle statuses before workflow operations.
     */
    @Deprecated
    BLOCKED,
    @Deprecated
    SUCCESS,
    @Deprecated
    FLAGGED

    ;

    public boolean isLegacyValue() {
        return this == BLOCKED || this == SUCCESS || this == FLAGGED;
    }

    public static RiskAlertStatus normalizeForWorkflow(RiskAlertStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case BLOCKED -> ESCALATED;
            case SUCCESS -> RESOLVED;
            case FLAGGED -> NEW;
            default -> status;
        };
    }
}
