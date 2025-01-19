import {Component, HostListener, ViewChild} from '@angular/core';
import {Router, RouterLink} from "@angular/router";
import {Sidebar, SidebarModule} from "primeng/sidebar";
import {ButtonModule} from "primeng/button";
import {AuthService} from "../login/auth.service";
import {Dropdown, DropdownModule} from "primeng/dropdown";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-header',
  standalone: true,
  templateUrl: './header.component.html',
  imports: [
    RouterLink,
    SidebarModule,
    ButtonModule,
    DropdownModule,
    FormsModule
  ],
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  @ViewChild('dropdown') dropdown: Dropdown | undefined;
  @ViewChild('sidebar') sidebar: Sidebar | undefined;

  constructor(protected readonly authService: AuthService, private readonly router: Router) {
  }

  protected readonly localStorage = localStorage;
  protected visibleSidebar: boolean = false;
  protected sidebarButtonClicked: boolean = false;
  protected selectedRole: string = '';

  navigateTo(route: string) {
    this.closeSidebar();
    this.router.navigate([route]).then();
  }

  logout() {
    this.closeSidebar();
    this.authService.logout();
  }

  openSidebar() {
    this.sidebarButtonClicked = true;
    this.visibleSidebar = true
  }

  closeSidebar() {
    this.visibleSidebar = false;
  }

  protected canShowSidebar() {
    return this.visibleSidebar && this.authService.isAuthenticated();
  }

  protected canShowSidebarButton() {
    return !this.visibleSidebar && this.authService.isAuthenticated();
  }

  onRoleChange(event: any) {
    this.selectedRole = event.value;
    if (this.dropdown) {
      this.dropdown.overlayVisible = false;
    }
  }

  protected getRoles() {
    let roleString = this.localStorage.getItem('roles');
    if (roleString) {
      return roleString.split(',');
    }
    return [];
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (this.dropdown && !this.dropdown.el.nativeElement.contains(target)) {
      this.dropdown.overlayVisible = false;
    }

    if (this.visibleSidebar && this.sidebar && !this.sidebar.el.nativeElement.contains(target) && !this.sidebarButtonClicked) {
      this.closeSidebar();
    }

    this.sidebarButtonClicked = false;
  }
}
