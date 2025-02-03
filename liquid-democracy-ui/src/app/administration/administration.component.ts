import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {BreadcrumbModule} from "primeng/breadcrumb";
import {ToastModule} from "primeng/toast";
import {FilterService, MenuItem, MessageService} from "primeng/api";
import {AuthService} from "../login/auth.service";
import {TabViewModule} from "primeng/tabview";
import {Topic} from "../dashboard/dashboard.component";
import {ScrollerModule} from "primeng/scroller";
import {NgClass} from "@angular/common";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {Button} from "primeng/button";
import {Dropdown, DropdownModule} from "primeng/dropdown";
import {MultiSelectModule} from "primeng/multiselect";
import {Table, TableModule} from "primeng/table";
import {DialogModule} from "primeng/dialog";
import {ConfirmDialogModule} from "primeng/confirmdialog";

@Component({
  selector: 'app-administration',
  standalone: true,
  imports: [
    BreadcrumbModule,
    ToastModule,
    TabViewModule,
    ScrollerModule,
    FormsModule,
    ReactiveFormsModule,
    NgClass,
    Button,
    DropdownModule,
    MultiSelectModule,
    TableModule,
    DialogModule,
    ConfirmDialogModule
  ],
  providers: [AuthService, MessageService],
  templateUrl: './administration.component.html',
  styleUrl: './administration.component.css'
})
export class AdministrationComponent implements OnInit {
  @ViewChild('dt') dt: Table | undefined;
  @ViewChild('dropdown') dropdown: Dropdown | undefined;

  items: MenuItem[] = [
    {label: 'Διαχείριση'}
  ];

  home: MenuItem = {routerLink: ['/dashboard']};

  topics: string[] = [];

  newTopicForm: FormGroup;

  userDetails: UserDetails[] = [];

  selectedUser: UserDetails | null = null;

  loading: boolean = true;

  selectedRole: string = '';

  allRoles: string[] = [
    "Ψηφοφόρος",
    "Αντιπρόσωπος",
    "Εφορευτική Επιτροπή",
    "Διαχειριστής Συστήματος"
  ];

  roleDialogVisible: boolean = false;

  allowDialog: boolean = true;

  allowDropdown: boolean = true;

  constructor(private readonly authService: AuthService,
              private readonly messageService: MessageService,
              private readonly fb: FormBuilder,
              private readonly filterService: FilterService) {
    this.newTopicForm = this.fb.group({
      name: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.authService.getTopics().subscribe({
      next: (response: Topic[]) => {
        this.topics = response.map((topic) => topic.name)
          .sort((a, b) => a.localeCompare(b));
      },
      error: (error) => {
        console.error('Σφάλμα:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: error.error
        });
      }
    });

    this.loadTable();

    this.filterService.register('customArray', (value: any[], filter: any[]): boolean => {
      if (!filter || filter.length === 0) {
        return true;
      }
      if (Array.isArray(value)) {
        return filter.some(f => value.includes(f));
      }
      return false;
    });
  }

  onTopicSubmit(): void {
    this.messageService.clear();
    if (this.newTopicForm.valid) {
      const {name} = this.newTopicForm.value;
      this.authService.createTopic(name).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Επιτυχία',
            detail: 'Η θεματική περιοχή δημιουργήθηκε επιτυχώς'
          });
          this.topics.push(name);
          this.topics.sort((a, b) => a.localeCompare(b));
        },
        error: (error) => {
          console.error('Delegation failed', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Αποτυχία',
            detail: error.error.error
          });
        }
      });
    }
  }

  assignRole() {
    this.roleDialogVisible = true;
  }

  resetRoleDialog() {
    this.selectedRole = '';
    this.allowDialog = false;
    this.roleDialogVisible = false;
    setTimeout(() => {
      this.allowDialog = true;
    }, 0);
  }

  onRoleChange() {
    if (this.dropdown) {
      this.allowDropdown = false;
      this.dropdown.overlayVisible = false;
      setTimeout(() => {
        this.allowDropdown = true;
      }, 0);
    }
  }

  addRole() {
    if (this.selectedRole && this.selectedUser) {
      this.authService.addRole(this.selectedRole, this.selectedUser.id).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Επιτυχία',
            detail: 'Ο ρόλος ανατέθηκε με επιτυχία'
          });
          this.loadTable()
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Αποτυχία',
            detail: error.error.error
          });
        }
      });
    }
    this.selectedRole = '';
    this.resetRoleDialog();
  }

  loadTable() {
    this.loading = true;
    this.selectedUser = null;
    this.authService.getAllUserDetails().subscribe({
      next: (response) => {
        this.userDetails = response;
      },
      error: (error) => {
        console.error('Σφάλμα:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: error.error
        });
      }
    });
    this.loading = false;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (this.dropdown && this.dropdown.overlayVisible && !this.dropdown.el.nativeElement.contains(target)) {
      this.onRoleChange();
    }
  }

}

export interface UserDetails {
  id: number;
  username: string;
  name: string;
  surname: string;
  email: number;
  roles: string[];
}
