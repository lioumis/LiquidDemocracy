import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import {PasswordResetComponent} from "./login/password-reset/password-reset.component";
import {RegistrationComponent} from "./login/registration/registration.component";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {SettingsComponent} from "./settings/settings.component";
import {DelegationsComponent} from "./delegations/delegations.component";
import {VotingsComponent} from "./votings/votings.component";
import {VotingComponent} from "./votings/voting/voting.component";
import {AdministrationComponent} from "./administration/administration.component";

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: PasswordResetComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'settings', component: SettingsComponent },
  { path: 'delegations', component: DelegationsComponent },
  { path: 'register', component: RegistrationComponent },
  { path: 'votings', component: VotingsComponent },
  { path: 'voting/:id', component: VotingComponent },
  { path: 'administration', component: AdministrationComponent },
  { path: '', redirectTo: '/', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
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
