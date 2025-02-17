import {Component} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../auth.service';
import {Router, RouterLink} from '@angular/router';
import {MessageService} from 'primeng/api';
import {ButtonModule} from "primeng/button";
import {PaginatorModule} from "primeng/paginator";
import {ToastModule} from "primeng/toast";

@Component({
  selector: 'app-password-reset',
  standalone: true,
  imports: [
    ButtonModule,
    PaginatorModule,
    ReactiveFormsModule,
    RouterLink,
    ToastModule
  ],
  providers: [AuthService, MessageService],
  templateUrl: './password-reset.component.html',
  styleUrl: './password-reset.component.css'
})
export class PasswordResetComponent {
  stepOneForm: FormGroup;
  stepTwoForm: FormGroup;

  currentStep = 1;
  securityQuestion: string = '';

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly messageService: MessageService
  ) {
    this.stepOneForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]]
    });

    this.stepTwoForm = this.fb.group({
        securityAnswer: ['', Validators.required],
        password: ['', Validators.required],
        passwordRepeat: ['', Validators.required]
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

  onStepOneSubmit(): void {
    const {username, email} = this.stepOneForm.value;
    this.authService.getSecurityQuestion(username, email).subscribe({
      next: (response) => {
        this.securityQuestion = response.message;
        this.currentStep = 2;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: 'Δεν βρέθηκε το όνομα χρήστη ή το email.'
        });
        this.stepOneForm.setErrors({unknownUserError: true});
      }
    });
  }

  onStepTwoSubmit(): void {
    const {username, email} = this.stepOneForm.value;
    const {securityAnswer, password} = this.stepTwoForm.value;

    this.authService.reset(username, email, password, securityAnswer).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Επιτυχία',
          detail: 'Ο κωδικός πρόσβασης επαναφέρθηκε με επιτυχία.'
        });
        this.router.navigate(['/login']).then();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Αποτυχία',
          detail: 'Η επαναφορά κωδικού απέτυχε.'
        });
        this.stepTwoForm.setErrors({incorrectAnswerError: true});
      }
    });
  }

  onPrevious(): void {
    this.currentStep = 1;
  }
}
