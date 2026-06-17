export interface FraudAnalyticsDTO {
  analyticsId: number;
  /** Fraud rate as a percentage (0–100) */
  fraudRate: number;
  /** Average risk score – the risk trend */
  riskTrend: number;
  totalTransactions: number;
  flaggedTransactions: number;
  blockedTransactions: number;
  activeCustomers: number;
  generatedDate: string | number[];
}

