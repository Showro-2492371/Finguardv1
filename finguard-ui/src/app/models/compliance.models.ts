export interface ComplianceReportDTO {
  reportId: number;
  customerId: number;
  fraudCases: number;
  riskScore: number;
  generatedDate: string | number[];
}

export interface ComplianceSummary {
  totalCustomers: number;
  totalFraudCases: number;
  totalRiskScore: number;
}

export interface AuditTrail {
  id: number;
  action: string;
  performedBy: string;
  timestamp: string | number[];
}

