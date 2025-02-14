import {Component} from '@angular/core';
import {ButtonModule} from "primeng/button";
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
    ButtonModule,
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
        username: ['', Validators.compose([Validators.required, Validators.maxLength(255)])],
        email: ['', Validators.compose([Validators.required, Validators.maxLength(255)])],
        name: ['', Validators.compose([Validators.required, Validators.maxLength(255)])],
        surname: ['', Validators.compose([Validators.required, Validators.maxLength(255)])],
        password: ['', Validators.required],
        passwordRepeat: ['', Validators.required],
        securityQuestion: ['', Validators.compose([Validators.required, Validators.maxLength(255)])],
        securityAnswer: ['', Validators.required]
      },
      {
        validators: this.passwordMatchValidator
      });
  }

  passwordMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
    const password = group.get('password')?.value;
    const passwordRepeat = group.get('passwordRepeat')?.value;
    return password === passwordRepeat ? null : {mismatch: true};
  }

  onSubmit(): void {
    this.registrationForm.setErrors(null);
    this.messageService.clear();
    if (this.registrationForm.valid) {
      const {username, email, name, surname, password, securityQuestion, securityAnswer} = this.registrationForm.value;
      this.authService.register(username, email, name, surname, password, securityQuestion, securityAnswer).subscribe({
        next: () => {
          this.router.navigate(['/login']).then();
        },
        error: (error) => {
          console.error('Login failed', error.error.error);
          this.messageService.add({
            severity: 'error',
            summary: 'Αποτυχία Εγγραφής',
            detail: error.error.error
          });

          this.registrationForm.setErrors({registrationError: true});
        }
      });
    }
  }
}
