export type ChannelType = 'UPI' | 'ONLINE_BANKING' | 'ATM';
export type TransactionType = 'CREDITED' | 'DEBITED';
export type TransactionStatus = 'SUCCESS' | 'FLAGGED' | 'BLOCKED';

export interface TransactionRequest {
  customerId: number;
  amount: number;
  channel: ChannelType;
  transactionType: TransactionType;
}

export interface FraudCheckResponse {
  transactionId: string | null;
  customerId: number;
  fraudDetected: boolean;
  riskScore: number;
  fraudReason: string;
  status: TransactionStatus;
  createdAt: string | number[] | null;
}

export interface TransactionHistoryEntry extends FraudCheckResponse {
  amount: number;
  channel: ChannelType;
  transactionType: TransactionType;
}

