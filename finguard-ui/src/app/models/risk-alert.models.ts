export type RiskAlertStatus =
  | 'NEW' | 'ESCALATED' | 'RESOLVED';

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | 'UNKNOWN';

export interface RiskAlertResponse {
  alertId: number;
  transactionId: string;
  riskScore: number;
  alertDate: string | number[];
  status: RiskAlertStatus;
  riskLevel: RiskLevel;
}

