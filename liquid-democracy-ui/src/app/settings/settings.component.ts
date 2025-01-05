import {Component, OnInit} from '@angular/core';
import {ToastModule} from "primeng/toast";
import {AuthService} from "../login/auth.service";
import {MessageService} from "primeng/api";
import {PanelModule} from "primeng/panel";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {Button} from "primeng/button";

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    ToastModule,
    PanelModule,
    FormsModule,
    ReactiveFormsModule,
    Button,
  ],
  providers: [AuthService, MessageService],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css'
})
export class SettingsComponent implements OnInit {

  passwordForm: FormGroup;

  protected username: string | null = '';
  protected name: string | null = '';
  protected surname: string | null = '';
  protected email: string | null = '';

  constructor(private readonly authService: AuthService,
              private readonly messageService: MessageService,
              private readonly fb: FormBuilder) {
    this.passwordForm = this.fb.group({
        oldPassword: ['', Validators.required],
        newPassword: ['', Validators.required],
        confirmPassword: ['', Validators.required]
      },
      {
        validators: this.passwordMatchValidator
      });
  }

  passwordMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : {mismatch: true};
  }

  ngOnInit() {
    this.username = localStorage.getItem("username");
    this.name = localStorage.getItem("name");
    this.surname = localStorage.getItem("surname");
    this.email = localStorage.getItem("email");
  }

  onSubmit(): void {
    this.messageService.clear();
    if (this.passwordForm.valid) {
      const {oldPassword, newPassword} = this.passwordForm.value;

      this.authService.changePassword(oldPassword, newPassword).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Επιτυχία',
            detail: 'Ο κωδικός πρόσβασης άλλαξε με επιτυχία.'
          });
          this.passwordForm.reset();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Σφάλμα',
            detail: error.error.error || 'Η αλλαγή κωδικού απέτυχε'
          });
        }
      });
    } else {
      this.messageService.add({
        severity: 'warn',
        summary: 'Προειδοποίηση',
        detail: 'Συμπληρώστε σωστά όλα τα πεδία.'
      });
    }
  }
}
