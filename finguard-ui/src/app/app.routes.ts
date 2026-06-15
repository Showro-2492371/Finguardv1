import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { userGuard } from './core/guards/user.guard';
import { LoginComponent } from './pages/auth/login/login.component';
import { SignupComponent } from './pages/auth/signup/signup.component';
import { UserDashboardComponent } from './pages/user/user-dashboard/user-dashboard.component';
import { TransactionPageComponent } from './pages/user/transaction-page/transaction-page.component';
import { KycPageComponent } from './pages/user/kyc-page/kyc-page.component';
import { AdminDashboardComponent } from './pages/admin/admin-dashboard/admin-dashboard.component';
import { RiskAlertsPageComponent } from './pages/admin/risk-alerts-page/risk-alerts-page.component';
import { CompliancePageComponent } from './pages/admin/compliance-page/compliance-page.component';
import { KycManagementPageComponent } from './pages/admin/kyc-management-page/kyc-management-page.component';

export const routes: Routes = [
  { path: '', redirectTo: 'user/login', pathMatch: 'full' },
  { path: 'login', redirectTo: 'user/login', pathMatch: 'full' },
  { path: 'signup', redirectTo: 'user/signup', pathMatch: 'full' },

  // Public auth pages (separate for user/admin)
  { path: 'user/login', component: LoginComponent, data: { portal: 'user' } },
  { path: 'user/signup', component: SignupComponent, data: { portal: 'user' } },
  { path: 'admin/login', component: LoginComponent, data: { portal: 'admin' } },
  { path: 'admin/signup', component: SignupComponent, data: { portal: 'admin' } },
  {
    path: 'user',
    canActivate: [authGuard, userGuard],
    children: [
      { path: '',             redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard',    component: UserDashboardComponent },
      { path: 'transactions', component: TransactionPageComponent },
      { path: 'kyc',          component: KycPageComponent }
    ]
  },
  {
    path: 'admin',
    canActivate: [authGuard, adminGuard],
    children: [
      { path: '',                  redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard',         component: AdminDashboardComponent },
      { path: 'risk-alerts',       component: RiskAlertsPageComponent },
      { path: 'compliance',        component: CompliancePageComponent },
      { path: 'kyc-management',    component: KycManagementPageComponent }
    ]
  },
  { path: '**', redirectTo: 'user/login' }
];
