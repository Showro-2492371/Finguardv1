export enum ChannelType {
  UPI = 'UPI',
  ONLINE_BANKING = 'ONLINE_BANKING',
  ATM = 'ATM'
}

export enum TransactionType {
  CREDITED = 'CREDITED',
  DEBITED = 'DEBITED'
}

export enum TransactionStatus {
  SUCCESS = 'SUCCESS',
  FLAGGED = 'FLAGGED',
  BLOCKED = 'BLOCKED'
}

export interface TransactionRequest {
  customerId: number;
  amount: number;
  channel: ChannelType;
  transactionType: TransactionType;
}

