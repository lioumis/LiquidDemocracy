import {Component} from '@angular/core';
import {Button} from "primeng/button";
import {PaginatorModule} from "primeng/paginator";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {Router, RouterLink} from "@angular/router";
import {ToastModule} from "primeng/toast";
import {AuthService} from "../auth.service";
import {MessageService} from "primeng/api";

@Component({
  selector: 'app-registration',
  standalone: true,
  imports: [
    Button,
    PaginatorModule,
    ReactiveFormsModule,
    RouterLink,
    ToastModule
  ],
  providers: [AuthService, MessageService],
  templateUrl: './registration.component.html',
  styleUrl: './registration.component.css'
})
export class RegistrationComponent {
  registrationForm: FormGroup;

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly messageService: MessageService
  ) {
    this.registrationForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', Validators.required],
      name: ['', Validators.required],
      surname: ['', Validators.required],
      password: ['', Validators.required],
      passwordRepeat: ['', Validators.required],
      securityQuestion: ['', Validators.required],
      securityAnswer: ['', Validators.required]
    });
  }

  onSubmit(): void {
    this.registrationForm.setErrors(null);
    // this.messageService.clear(); //TODO: Does not work
    if (this.registrationForm.valid) {
      const {username, email, name, surname, password, securityQuestion, securityAnswer} = this.registrationForm.value;
      this.authService.register(username, email, name, surname, password, securityQuestion, securityAnswer).subscribe(
        (response) => {
          this.router.navigate(['/login']);
        },
        (error) => {
          console.error('Login failed', error);
          this.messageService.add({ //TODO: Does not work
            severity: 'error',
            summary: 'Login Failed',
            detail: 'Invalid username or password.'
          });

          this.registrationForm.setErrors({registrationError: true});
        }
      );
    }
  }
}
