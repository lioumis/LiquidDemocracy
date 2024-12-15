import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import {PasswordResetComponent} from "./login/password-reset/password-reset.component";
import {RegistrationComponent} from "./login/registration/registration.component";

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  // { path: '', redirectTo: '/login', pathMatch: 'full' }, // Redirect to login by default
  { path: 'forgot-password', component: PasswordResetComponent },
  { path: 'register', component: RegistrationComponent },
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
