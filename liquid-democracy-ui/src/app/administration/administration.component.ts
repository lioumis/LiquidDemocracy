import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {BreadcrumbModule} from "primeng/breadcrumb";
import {ToastModule} from "primeng/toast";
import {FilterService, MenuItem, MessageService} from "primeng/api";
import {AuthService} from "../login/auth.service";
import {TabViewModule} from "primeng/tabview";
import {Topic} from "../dashboard/dashboard.component";
import {ScrollerModule} from "primeng/scroller";
import {NgClass, NgForOf} from "@angular/common";
import {
  FormArray,
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from "@angular/forms";
import {Button} from "primeng/button";
import {Dropdown, DropdownModule} from "primeng/dropdown";
import {MultiSelect, MultiSelectModule} from "primeng/multiselect";
import {Table, TableModule} from "primeng/table";
import {DialogModule} from "primeng/dialog";
import {ConfirmDialogModule} from "primeng/confirmdialog";
import {VotingsService} from "../votings/votings.service";
import {AdministrationService} from "./administration.service";
import {InputTextModule} from "primeng/inputtext";
import {Ripple} from "primeng/ripple";

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
    ConfirmDialogModule,
    InputTextModule,
    NgForOf,
    Ripple
  ],
  providers: [AuthService, VotingsService, AdministrationService, MessageService],
  templateUrl: './administration.component.html',
  styleUrl: './administration.component.css'
})
export class AdministrationComponent implements OnInit {
  @ViewChild('dt') dt: Table | undefined;
  @ViewChild('assignDropdown') assignDropdown: Dropdown | undefined;
  @ViewChild('revokeDropdown') revokeDropdown: Dropdown | undefined;
  @ViewChild('topicDropdown') topicDropdown: Dropdown | undefined;
  @ViewChild('roleMultiSelect') roleMultiSelect!: MultiSelect;

  items: MenuItem[] = [
    {label: 'Διαχείριση'}
  ];

  home: MenuItem = {routerLink: ['/dashboard']};

  topics: string[] = [];

  newTopicForm: FormGroup;

  newVotingForm: FormGroup;

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

  assignRoleDialogVisible: boolean = false;

  revokeRoleDialogVisible: boolean = false;

  allowAssignDialog: boolean = true;

  allowRevokeDialog: boolean = true;

  allowAssignDropdown: boolean = true;

  allowRevokeDropdown: boolean = true;

  allowTopicDropdown: boolean = true;

  allowRoleMultiSelect: boolean = true;

  selectedTopic: string = '';

  constructor(private readonly authService: AuthService,
              private readonly votingsService: VotingsService,
              private readonly administrationService: AdministrationService,
              private readonly messageService: MessageService,
              private readonly fb: FormBuilder,
              private readonly filterService: FilterService) {
    this.newTopicForm = this.fb.group({
      name: ['', Validators.compose([Validators.required, Validators.maxLength(255)])],
    });
    this.newVotingForm = this.fb.group({
      votingName: ['', Validators.compose([Validators.required, Validators.maxLength(255)])],
      topic: ['', Validators.required],
      members: this.fb.array([this.fb.control('', Validators.required)])
    });
    this.members.setValidators(() => this.duplicateValuesValidator(this.members));
    this.addMember();
    this.addMember();
  }

  ngOnInit(): void {
    this.administrationService.getTopics().subscribe({
      next: (response: Topic[]) => {
        this.topics = response.map((topic) => topic.name)
          .sort((a, b) => a.localeCompare(b));
      },
      error: (error) => {
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

  get members(): FormArray {
    return this.newVotingForm.get('members') as FormArray;
  }

  addMember() {
    this.members.push(this.fb.control('', Validators.required));
    this.members.updateValueAndValidity();
  }

  removeMember(index: number) {
    if (this.members.length > 3) {
      this.members.removeAt(index);
      this.members.updateValueAndValidity();
    }
  }

  onTopicSubmit(): void {
    this.messageService.clear();
    if (this.newTopicForm.valid) {
      const {name} = this.newTopicForm.value;
      this.administrationService.createTopic(name).subscribe({
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
    this.assignRoleDialogVisible = true;
  }

  revokeRole() {
    this.revokeRoleDialogVisible = true;
  }

  resetAssignRoleDialog() {
    this.selectedRole = '';
    this.allowAssignDialog = false;
    this.assignRoleDialogVisible = false;
    setTimeout(() => {
      this.allowAssignDialog = true;
    }, 0);
  }

  resetRevokeRoleDialog() {
    this.selectedRole = '';
    this.allowRevokeDialog = false;
    this.revokeRoleDialogVisible = false;
    setTimeout(() => {
      this.allowRevokeDialog = true;
    }, 0);
  }

  onRoleChange() {
    if (this.assignDropdown) {
      this.allowAssignDropdown = false;
      this.assignDropdown.overlayVisible = false;
      setTimeout(() => {
        this.allowAssignDropdown = true;
      }, 0);
    }

    if (this.revokeDropdown) {
      this.allowRevokeDropdown = false;
      this.revokeDropdown.overlayVisible = false;
      setTimeout(() => {
        this.allowRevokeDropdown = true;
      }, 0);
    }
  }

  onTopicChange(event: any) {
    this.selectedTopic = event.value;
    this.resetDropdown();
  }

  resetDropdown() {
    if (this.topicDropdown) {
      this.allowTopicDropdown = false;
      this.topicDropdown.overlayVisible = false;
      setTimeout(() => {
        this.allowTopicDropdown = true;
      }, 0);
    }
  }

  resetMultiSelect() {
    if (this.roleMultiSelect) {
      this.allowRoleMultiSelect = false;
      this.roleMultiSelect.overlayVisible = false;
      setTimeout(() => {
        this.allowRoleMultiSelect = true;
      }, 0);
    }
  }

  resetVotingForm() {
    this.newVotingForm.reset();
  }

  onSubmit(): void {
    this.messageService.clear();
    if (this.newVotingForm.valid) {
      const {votingName, topic, members} = this.newVotingForm.value;
      this.votingsService.createNewVoting(votingName, topic, members).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Επιτυχία',
            detail: 'Η ψηφοφορία δημιουργήθηκε επιτυχώς'
          });
          this.resetVotingForm();
          this.loadTable();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Αποτυχία',
            detail: error.error.error
          });

          Object.keys(error.error).forEach(key => {
            if (key.startsWith('member')) {
              const index = parseInt(key.replace('member', ''), 10);
              const control = (this.newVotingForm.get('members') as FormArray).at(index);
              if (control) {
                control.setErrors({backend: error.error[key]});
              }
            }
          });
        }
      });
    }
  }

  addRole() {
    if (this.selectedRole && this.selectedUser) {
      this.administrationService.addRole(this.selectedRole, this.selectedUser.id).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Επιτυχία',
            detail: 'Ο ρόλος ανατέθηκε με επιτυχία'
          });
          this.loadTable();
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
    this.resetAssignRoleDialog();
  }

  removeRole() {
    if (this.selectedRole && this.selectedUser) {
      this.administrationService.revokeRole(this.selectedRole, this.selectedUser.id).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Επιτυχία',
            detail: 'Ο ρόλος ανακλήθηκε με επιτυχία'
          });
          this.loadTable();
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
    this.resetRevokeRoleDialog();
  }

  loadTable() {
    this.loading = true;
    this.selectedUser = null;
    this.authService.getAllUserDetails().subscribe({
      next: (response) => {
        this.userDetails = response;
      },
      error: (error) => {
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
    if (this.assignDropdown && this.assignDropdown.overlayVisible && !this.assignDropdown.el.nativeElement.contains(target) ||
      this.revokeDropdown && this.revokeDropdown.overlayVisible && !this.revokeDropdown.el.nativeElement.contains(target)) {
      this.onRoleChange();
    }
    if (this.topicDropdown && this.topicDropdown.overlayVisible && !this.topicDropdown.el.nativeElement.contains(target)) {
      this.resetDropdown();
    }
    if (this.roleMultiSelect && this.roleMultiSelect.overlayVisible && !this.roleMultiSelect.el.nativeElement.contains(target)) {
      this.resetMultiSelect();
    }
  }

  duplicateValuesValidator(formArray: FormArray): ValidationErrors | null {
    const values = formArray.controls.map(control => control.value?.toLowerCase());
    const duplicates = values.filter((value, index, arr) => value && arr.indexOf(value) !== index);

    formArray.controls.forEach(control => {
      const isDuplicate = duplicates.includes(control.value?.toLowerCase());
      if (isDuplicate) {
        control.setErrors({...control.errors, duplicate: true});
      } else {
        if (control.errors) {
          const {duplicate, ...remainingErrors} = control.errors;
          control.setErrors(Object.keys(remainingErrors).length > 0 ? remainingErrors : null);
        }
      }
    });

    return duplicates.length ? {duplicateValues: true} : null;
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
