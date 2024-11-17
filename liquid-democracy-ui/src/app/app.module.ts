import {AppRoutingModule, routing} from './app.routes';
import {ReactiveFormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {NgModule} from "@angular/core";
import {LoginComponent} from "./login/login.component";

@NgModule({
  declarations: [],
  imports: [
    AppRoutingModule,
    routing,
    ReactiveFormsModule,
    HttpClientModule,
    LoginComponent
  ]
})
export class AppModule {
}
