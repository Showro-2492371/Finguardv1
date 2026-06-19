import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { jwtDecode } from 'jwt-decode';
import { map, Observable, tap } from 'rxjs';
import { JwtPayload, SignupRequest } from '../../models/auth.models';

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

  signup(req: SignupRequest): Observable<void> {
    return this.http
      .post('/api/customer/signup', req, { responseType: 'text' })
      .pipe(map(() => void 0));
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

