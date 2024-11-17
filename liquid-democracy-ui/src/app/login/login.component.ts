import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {AuthService} from "./auth.service";
import {HttpClientModule} from "@angular/common/http";
import {Router, RouterLink} from "@angular/router";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    HttpClientModule,
    RouterLink
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent implements OnInit{
  loginForm: FormGroup;

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  ngOnInit(): void { /* TODO document why this method 'ngOnInit' is empty */ }

  onSubmit(): void {
    if (this.loginForm.valid) {
      const { username, password } = this.loginForm.value;
      this.authService.login(username, password).subscribe(
        (response) => {
          // Handle successful login, navigate to dashboard
          localStorage.setItem('token', response.token);
          console.log('Token response', response.token);
          // this.router.navigate(['/dashboard']);
          this.router.navigate(['/']);
        },
        (error) => {
          // Handle login error
          console.error('Login failed', error);
        }
      );
    }
  }
}
