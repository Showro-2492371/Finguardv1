import { TransactionStatus } from './transaction-request.model';

export interface FraudCheckResponse {
  transactionId: string | null;
  customerId: number;
  fraudDetected: boolean;
  riskScore: number;
  fraudReason: string;
  status: TransactionStatus;
  createdAt: string | number[] | null;
}

