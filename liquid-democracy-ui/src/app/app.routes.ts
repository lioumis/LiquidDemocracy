import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import {PasswordResetComponent} from "./login/password-reset/password-reset.component";
import {RegistrationComponent} from "./login/registration/registration.component";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {SettingsComponent} from "./settings/settings.component";
import {DelegationsComponent} from "./delegations/delegations.component";
import {VotingsComponent} from "./votings/votings.component";

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  // { path: '', redirectTo: '/login', pathMatch: 'full' }, // Redirect to login by default
  { path: 'forgot-password', component: PasswordResetComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'settings', component: SettingsComponent },
  { path: 'delegations', component: DelegationsComponent },
  { path: 'register', component: RegistrationComponent },
  { path: 'votings', component: VotingsComponent },
  { path: '', redirectTo: '/', pathMatch: 'full' }, // Redirect to root
  { path: '**', redirectTo: '/login' } // Fallback route
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
export const routing = RouterModule.forRoot(
    routes,
    {
      enableTracing: false,
      onSameUrlNavigation: 'reload'
    }
);
