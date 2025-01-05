import {AppRoutingModule, routing} from './app.routes';
import {ReactiveFormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {NgModule} from "@angular/core";
import {LoginComponent} from "./login/login.component";
import {ToastModule} from "primeng/toast";
import {BrowserModule} from "@angular/platform-browser";
import {ButtonModule} from "primeng/button";
import {HeaderComponent} from "./header/header.component";
import {RegistrationComponent} from "./login/registration/registration.component";
import {PasswordResetComponent} from "./login/password-reset/password-reset.component";
import {MessageService} from "primeng/api";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {RouterModule} from "@angular/router";
import {VotingsComponent} from "./votings/votings.component";
import {VotingComponent} from "./votings/voting/voting.component";

@NgModule({
  declarations: [],
  imports: [
    AppRoutingModule,
    routing,
    ReactiveFormsModule,
    HttpClientModule,
    ToastModule,
    RouterModule,
    BrowserModule,
    ButtonModule,
    LoginComponent,
    HeaderComponent,
    RegistrationComponent,
    PasswordResetComponent,
    VotingsComponent,
    VotingComponent,
    BrowserAnimationsModule
  ],
  providers: [MessageService]
})
export class AppModule {
}
