import {Component, OnInit} from '@angular/core';
import {AuthService} from "../login/auth.service";
import {MessageService} from "primeng/api";

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [],
  providers: [AuthService, MessageService],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  constructor(private readonly authService: AuthService, private readonly messageService: MessageService) {
  }

  ngOnInit(): void {
    let identifier = localStorage.getItem('identifier');
    if(identifier) {
      this.authService.getUserDetails(identifier).subscribe(
        (response) => {
          localStorage.setItem('username', response.username);
          localStorage.setItem('name', response.name);
          localStorage.setItem('surname', response.surname);
          localStorage.setItem('email', response.email);
          localStorage.setItem('roles', response.roles);
        },
        (error) => {
          console.error('Σφάλμα:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Σφάλμα',
            detail: error.error
          });
        }
      );
    }
  }

}
