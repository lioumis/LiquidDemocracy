import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router, RouterOutlet} from '@angular/router';
import {AuthService} from "./login/auth.service";
import {HttpClientModule} from "@angular/common/http";
import {HeaderComponent} from "./header/header.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, HttpClientModule, HeaderComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit {
  constructor(private readonly router: Router, private readonly authService: AuthService) {
  }

  title = 'app';

  ngOnInit(): void {
    localStorage.removeItem('selectedRole');
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']).then();
    } else {
      this.router.navigate(['/dashboard']).then();
    }
  }
}
