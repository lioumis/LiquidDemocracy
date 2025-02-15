import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {MultiSelectModule} from "primeng/multiselect";
import {PanelModule} from "primeng/panel";
import {ConfirmationService, MenuItem, MessageService, PrimeTemplate} from "primeng/api";
import {TableModule} from "primeng/table";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {Delegation, Voting} from "../dashboard/dashboard.component";
import {AuthService} from "../login/auth.service";
import {Dropdown, DropdownModule} from "primeng/dropdown";
import {Button} from "primeng/button";
import {ToastModule} from "primeng/toast";
import {BreadcrumbModule} from "primeng/breadcrumb";
import {Router} from "@angular/router";
import {ConfirmDialogModule} from "primeng/confirmdialog";
import {VotingsService} from "../votings/votings.service";
import {DelegationsService} from "./delegations.service";

@Component({
  selector: 'app-delegations',
  standalone: true,
  imports: [
    MultiSelectModule,
    PanelModule,
    PrimeTemplate,
    TableModule,
    FormsModule,
    ReactiveFormsModule,
    DropdownModule,
    ToastModule,
    Button,
    BreadcrumbModule,
    ConfirmDialogModule
  ],
  providers: [AuthService, VotingsService, DelegationsService, MessageService, ConfirmationService],
  templateUrl: './delegations.component.html',
  styleUrl: './delegations.component.css'
})
export class DelegationsComponent implements OnInit {
  @ViewChild('dropdown') dropdown: Dropdown | undefined;

  delegationForm: FormGroup;

  selectedVoting: string = '';

  delegations: Delegation[] = [];

  votings: string[] = [];

  completeVotings: Voting[] = [];

  loading: boolean = true;

  allowDropdown: boolean = true;

  showConfirmDialog: boolean = true;

  items: MenuItem[] = [
    {label: 'Αναθέσεις'}
  ];

  home: MenuItem = {routerLink: ['/dashboard']};

  constructor(private readonly authService: AuthService,
              private readonly votingsService: VotingsService,
              private readonly delegationsService: DelegationsService,
              private readonly messageService: MessageService,
              private readonly confirmationService: ConfirmationService,
              private readonly router: Router,
              private readonly fb: FormBuilder) {
    this.delegationForm = this.fb.group({
      name: ['', Validators.required],
      surname: ['', Validators.required],
      voting: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']).then();
    }

    this.votingsService.getAllVotings().subscribe({
      next: (response: Voting[]) => {
        this.votings = response.map((voting) => voting.name);
        this.completeVotings = response;
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
  }

  onVotingChange(event: any) {
    this.selectedVoting = event.value;
    this.resetDropdown();
  }

  resetDropdown() {
    if (this.dropdown) {
      this.allowDropdown = false;
      this.dropdown.overlayVisible = false;
      setTimeout(() => {
        this.allowDropdown = true;
      }, 0);
    }
  }

  onSubmit(): void {
    this.messageService.clear();
    this.displayDialog();
  }

  createDelegation() {
    if (this.delegationForm.valid) {
      const {name, surname, voting} = this.delegationForm.value;
      this.delegationsService.createDelegation(name, surname, this.getVotingId(voting)).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Επιτυχία',
            detail: 'Η ανάθεση ολοκληρώθηκε επιτυχώς'
          });
          this.resetForm();
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
  }

  getVotingId(voting: string) {
    let completeVoting = this.completeVotings.find(v => v.name === voting);
    return completeVoting ? completeVoting.id : -1;
  }

  resetForm() {
    this.delegationForm.reset();
  }

  loadTable() {
    this.loading = true;
    this.delegationsService.getDelegations().subscribe({
      next: (response) => {
        this.delegations = response;
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

  displayDialog() {
    this.confirmationService.confirm({
      acceptLabel: "Ναι",
      rejectLabel: "Όχι",
      message: 'Η ανάθεση ψήφου είναι μη αναστρέψιμη. Θέλετε να συνεχίσετε;',
      header: 'Προσοχή!',
      icon: 'pi pi-exclamation-triangle',
      acceptIcon: "none",
      rejectIcon: "none",
      rejectButtonStyleClass: "p-button-text",
      accept: () => {
        this.createDelegation();
        this.resetConfirmDialog();
      },
      reject: () => {
        this.resetConfirmDialog();
      }
    });
  }

  resetConfirmDialog() {
    this.showConfirmDialog = false;
    setTimeout(() => {
      this.showConfirmDialog = true;
    }, 0);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (this.dropdown && this.dropdown.overlayVisible && !this.dropdown.el.nativeElement.contains(target)) {
      this.resetDropdown();
    }
  }
}
