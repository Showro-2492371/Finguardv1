export interface LoginRequest {
  name: string;
  password: string;
}

export interface SignupRequest {
  name: string;
  contactInfo: string;
  password: string;
  mfaEnabled: boolean;
}

export interface ApiMessageResponse {
  status: string;
  message: string;
  code?: string;
}

export interface JwtPayload {
  sub: string;
  customerId: number;
  role: string;
  exp: number;
}

