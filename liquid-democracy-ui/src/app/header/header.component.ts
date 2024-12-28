import {Component} from '@angular/core';
import {Router, RouterLink} from "@angular/router";
import {SidebarModule} from "primeng/sidebar";
import {Button} from "primeng/button";
import {AuthService} from "../login/auth.service";

@Component({
  selector: 'app-header',
  standalone: true,
  templateUrl: './header.component.html',
  imports: [
    RouterLink,
    SidebarModule,
    Button
  ],
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  constructor(protected readonly authService: AuthService, private readonly router: Router) {
  }

  visibleSidebar: boolean = false;

  navigateTo(route: string) {
    this.closeSidebar();
    this.router.navigate([route]);
  }

  logout() {
    this.closeSidebar();
    this.authService.logout();
  }

  closeSidebar() {
    console.log("Closed Sidebar");
    this.visibleSidebar = false;
  }

  protected canShowSidebar() {
    return this.visibleSidebar && this.authService.isAuthenticated();
  }

  protected canShowSidebarButton() {
    return !this.visibleSidebar && this.authService.isAuthenticated();
  }
}
