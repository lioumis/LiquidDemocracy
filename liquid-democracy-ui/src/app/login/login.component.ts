import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {AuthService} from "./auth.service";
import {HttpClientModule} from "@angular/common/http";
import {Router, RouterLink} from "@angular/router";
import {MessageService} from "primeng/api";
import {ButtonModule} from "primeng/button";
import {ToastModule} from "primeng/toast";
import {CardModule} from "primeng/card";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    HttpClientModule,
    RouterLink,
    ButtonModule,
    ToastModule,
    CardModule
  ],
  providers: [AuthService, MessageService],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly messageService: MessageService
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']).then();
    }
  }

  onSubmit(): void {
    this.messageService.clear();
    if (this.loginForm.valid) {
      const {username, password} = this.loginForm.value;
      this.authService.login(username, password).subscribe({
        next: (response) => {
          localStorage.setItem('token', response.token);
          this.getDetailsAndLogin();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Αποτυχία Σύνδεσης',
            detail: error.error
          });

          this.loginForm.setErrors({invalidCredentials: true});
        }
      });
    }
  }

  getDetailsAndLogin() {
    this.authService.getUserDetails().subscribe({
      next: (response) => {
        localStorage.setItem('username', response.username);
        localStorage.setItem('name', response.name);
        localStorage.setItem('surname', response.surname);
        localStorage.setItem('email', response.email);
        localStorage.setItem('roles', response.roles);
        localStorage.setItem('selectedRole', response.roles[0]);
        this.router.navigate(['/dashboard']).then();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: error.error
        });
      }
    });
  }
}
