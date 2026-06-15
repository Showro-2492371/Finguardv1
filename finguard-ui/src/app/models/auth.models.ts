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

export interface JwtPayload {
  sub: string;
  customerId: number;
  role: string;
  exp: number;
}

