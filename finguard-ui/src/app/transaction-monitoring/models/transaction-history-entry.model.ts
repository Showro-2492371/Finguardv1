import { FraudCheckResponse } from './fraud-check-response.model';
import { ChannelType, TransactionType } from './transaction-request.model';

export interface TransactionHistoryEntry extends FraudCheckResponse {
  amount: number;
  channel: ChannelType;
  transactionType: TransactionType;
}

