package org.cts.adm.finguard.RiskAlert.Enum;

public enum RiskAlertStatus {
    NEW,
    /**
     * Kept only for backward compatibility with historical rows.
     * Current workflow does not create REVIEWED directly.
     */
    @Deprecated
    REVIEWED,
    ESCALATED,
    RESOLVED,
    /**
     * Kept only for backward compatibility with historical rows.
     * Current workflow does not create CLOSED directly.
     */
    @Deprecated
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
            case SUCCESS -> NEW;
            case FLAGGED, BLOCKED, REVIEWED -> ESCALATED;
            case CLOSED -> RESOLVED;
            default -> status;
        };
    }
}
