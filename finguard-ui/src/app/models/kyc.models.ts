export type KycStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'VERIFIED' | 'REJECTED';

export interface KycStatusResponse {
  customerId: number;
  kycStatus: KycStatus;
  hasDocument: boolean;
  documentName: string | null;
  uploadedAt: string | number[] | null;
}

export type AccountStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'CLOSED';

export interface KycAdminRecord {
  customerId: number;
  customerName: string;
  contactInfo: string;
  kycStatus: KycStatus;
  accountStatus: AccountStatus;
  documentId: number;
  documentName: string;
  documentType: string;
  uploadedAt: string | number[] | null;
}
