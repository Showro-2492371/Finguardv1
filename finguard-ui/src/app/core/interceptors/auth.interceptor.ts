import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  const cloned = token
    ? req.clone({ headers: req.headers.set('Authorization', `Bearer ${token}`) })
    : req;

  return next(cloned).pipe(
    catchError((error: HttpErrorResponse) => {
      const isAdminApiCall = req.url.startsWith('/api/admin/');
      if (error.status === 401 || (error.status === 403 && isAdminApiCall)) {
        authService.logout();
      }
      return throwError(() => error);
    })
  );
};

