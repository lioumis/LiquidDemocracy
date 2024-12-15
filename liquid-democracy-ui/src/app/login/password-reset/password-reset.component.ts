import {Component} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../auth.service';
import {Router, RouterLink} from '@angular/router';
import {MessageService} from 'primeng/api';
import {Button} from "primeng/button";
import {PaginatorModule} from "primeng/paginator";
import {ToastModule} from "primeng/toast";

@Component({
  selector: 'app-password-reset',
  standalone: true,
  imports: [
    Button,
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
    });
  }

  onStepOneSubmit(): void {
    const {username, email} = this.stepOneForm.value;
    this.authService.getSecurityQuestion(username, email).subscribe(
      (response) => {
        this.securityQuestion = response.message;
        this.currentStep = 2;
      },
      (error) => {
        console.error('Error fetching security question:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: 'Δεν βρέθηκε το όνομα χρήστη ή το email.'
        });
        this.stepOneForm.setErrors({unknownUserError: true});
      }
    );
  }

  onStepTwoSubmit(): void {
    const {username, email} = this.stepOneForm.value;
    const {securityAnswer, password} = this.stepTwoForm.value;

    this.authService.reset(username, email, password, securityAnswer).subscribe(
      () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Επιτυχία',
          detail: 'Ο κωδικός πρόσβασης επαναφέρθηκε με επιτυχία.'
        });
        this.router.navigate(['/login']);
      },
      (error) => {
        console.error('Error during password reset:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Αποτυχία',
          detail: 'Η επαναφορά κωδικού απέτυχε.'
        });
        console.log(error.error.error);
        this.stepTwoForm.setErrors({incorrectAnswerError: true});
      }
    );
  }

  onPrevious(): void {
    this.currentStep = 1;
  }
}
