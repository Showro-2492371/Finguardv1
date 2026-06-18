import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { jwtDecode } from 'jwt-decode';
import { Observable, map, tap } from 'rxjs';
import { ApiMessageResponse, JwtPayload, SignupRequest } from '../../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'fg_token';
  private http = inject(HttpClient);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);

  login(name: string, password: string): Observable<string> {
    return this.http
      .post('/api/customer/login', { name, password }, { responseType: 'text' })
      .pipe(
        tap((token: string) => {
          if (isPlatformBrowser(this.platformId)) {
            localStorage.setItem(this.TOKEN_KEY, token);
          }
        })
      );
  }

  signup(req: SignupRequest): Observable<ApiMessageResponse> {
    // Request text so both JSON and plain-text backend responses are handled safely.
    return this.http
      .post('/api/customer/signup', req, { responseType: 'text' })
      .pipe(map((raw) => this.parseApiMessage(raw, 'Customer registered successfully', 'success')));
  }

  extractApiErrorMessage(error: unknown, fallback = 'Request failed. Please try again.'): string {
    if (!(error instanceof HttpErrorResponse)) {
      return fallback;
    }

    const payload = error.error;
    if (typeof payload === 'string') {
      const parsed = this.tryParseJson(payload);
      if (parsed?.['message']) {
        return String(parsed['message']);
      }
      return payload.trim() || fallback;
    }

    if (payload && typeof payload === 'object' && 'message' in payload) {
      return String((payload as { message?: unknown }).message ?? fallback);
    }

    return error.message || fallback;
  }

  private parseApiMessage(raw: string, fallbackMessage: string, fallbackStatus: string): ApiMessageResponse {
    const parsed = this.tryParseJson(raw);
    if (parsed && typeof parsed === 'object') {
      return {
        status: String(parsed['status'] ?? fallbackStatus),
        message: String(parsed['message'] ?? fallbackMessage),
        code: parsed['code'] ? String(parsed['code']) : undefined
      };
    }

    return {
      status: fallbackStatus,
      message: (raw || fallbackMessage).trim()
    };
  }

  private tryParseJson(value: string): Record<string, unknown> | null {
    try {
      return JSON.parse(value) as Record<string, unknown>;
    } catch {
      return null;
    }
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem(this.TOKEN_KEY);
    }
    return null;
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = jwtDecode<JwtPayload>(token);
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  getCustomerId(): number {
    const token = this.getToken();
    if (!token) return 0;
    try { return jwtDecode<JwtPayload>(token).customerId; } catch { return 0; }
  }

  getUsername(): string {
    const token = this.getToken();
    if (!token) return '';
    try { return jwtDecode<JwtPayload>(token).sub; } catch { return ''; }
  }

  getRole(): string {
    const token = this.getToken();
    if (!token) return '';
    try { return jwtDecode<JwtPayload>(token).role; } catch { return ''; }
  }

  isAdmin(): boolean {
    return this.getRole() === 'ROLE_ADMIN';
  }

  logout(): void {
    const wasAdmin = this.isAdmin();
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.TOKEN_KEY);
    }
    this.router.navigate([wasAdmin ? '/admin/login' : '/user/login']);
  }
}

