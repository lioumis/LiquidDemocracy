import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {AuthService} from "../login/auth.service";
import {ConfirmationService, MessageService} from "primeng/api";
import {PanelModule} from 'primeng/panel';
import {CarouselModule} from "primeng/carousel";
import {Button} from "primeng/button";
import {CardModule} from "primeng/card";
import {TableModule} from "primeng/table";
import {MultiSelect, MultiSelectModule} from "primeng/multiselect";
import {FormsModule} from "@angular/forms";
import {ToastModule} from "primeng/toast";
import {Router} from "@angular/router";
import {TooltipModule} from "primeng/tooltip";
import {ConfirmDialogModule} from 'primeng/confirmdialog';
import {VotingsService} from "../votings/votings.service";
import {DelegationsService} from "../delegations/delegations.service";

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    PanelModule,
    CarouselModule,
    Button,
    CardModule,
    TableModule,
    MultiSelectModule,
    FormsModule,
    ToastModule,
    TooltipModule,
    ConfirmDialogModule
  ],
  providers: [AuthService, VotingsService, DelegationsService, MessageService, ConfirmationService],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  @ViewChild('receivedMultiSelect') receivedMultiSelect!: MultiSelect;
  @ViewChild('sentMultiSelect') sentMultiSelect!: MultiSelect;

  allowReceivedMultiSelect: boolean = true;
  allowSentMultiSelect: boolean = true;

  delegations: Delegation[] = [];

  receivedDelegations: ReceivedDelegation[] = [];

  loading: boolean = true;

  votings: Voting[] = [];

  allVotings: string[] = [];

  responsiveOptions: any[] | undefined;

  showConfirmDialog: boolean = true;

  protected readonly localStorage = localStorage;

  constructor(private readonly authService: AuthService,
              private readonly votingsService: VotingsService,
              private readonly delegationsService: DelegationsService,
              private readonly router: Router,
              private readonly messageService: MessageService,
              private readonly confirmationService: ConfirmationService) {
  }

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']).then();
    }

    this.authService.getUserDetails().subscribe({
      next: (response) => {
        if (!this.hasTheSameRoles(response.roles)) {
          localStorage.setItem('roles', response.roles);
          localStorage.setItem('selectedRole', response.roles[0]);
          this.router.navigate(['/settings']).then(() => this.router.navigate(['/dashboard']).then());
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: error.error
        });
      }
    });

    if (localStorage.getItem('selectedRole') === "Εφορευτική Επιτροπή") {
      this.router.navigate(['/settings']).then(() => this.router.navigate(['/votings']).then());
    }

    if (localStorage.getItem('selectedRole') === "Διαχειριστής Συστήματος") {
      this.router.navigate(['/settings']).then(() => this.router.navigate(['/administration']).then());
    }

    if (localStorage.getItem('selectedRole') === "Ψηφοφόρος") {
      this.votingsService.getSuggestedVotings().subscribe({
        next: (response) => {
          this.votings = response;
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Σφάλμα',
            detail: error.error
          });
        }
      });

      this.votingsService.getAllVotings().subscribe({
        next: (response: Voting[]) => {
          this.allVotings = response.map((voting) => voting.name);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Σφάλμα',
            detail: error.error
          });
        }
      });

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
    }

    if (localStorage.getItem('selectedRole') === "Αντιπρόσωπος") {
      this.delegationsService.getReceivedDelegations().subscribe({
        next: (response) => {
          this.receivedDelegations = response;
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Σφάλμα',
            detail: error.error
          });
        }
      });

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
    }

    this.responsiveOptions = [
      {
        breakpoint: '1199px',
        numVisible: 1,
        numScroll: 1
      },
      {
        breakpoint: '991px',
        numVisible: 2,
        numScroll: 1
      },
      {
        breakpoint: '767px',
        numVisible: 1,
        numScroll: 1
      }
    ];

    this.loading = false;
  }

  selectVoting(id: number) {
    this.messageService.clear();
    this.votingsService.hasAccessToVoting(id).subscribe({
      next: (response) => {
        if (response.isPresent) {
          if (response.hasAccess === null) {
            this.messageService.add({
              severity: 'info',
              summary: 'Πληροφορία',
              detail: 'Η συμμετοχή σας σε αυτή την ψηφοφορία δεν έχει εξεταστεί ακόμα'
            });
          } else if (response.hasAccess === false) {
            this.messageService.add({
              severity: 'warn',
              summary: 'Προσοχή',
              detail: 'Η συμμετοχή σας σε αυτή την ψηφοφορία έχει απορριφθεί'
            });
          } else {
            this.router.navigate(['/voting', id]).then();
          }
        } else {
          this.displayDialog(id);
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: error.error.error
        });
      }
    });
  }

  resetConfirmDialog() {
    this.showConfirmDialog = false;
    setTimeout(() => {
      this.showConfirmDialog = true;
    }, 0);
  }

  displayDialog(id: number) {
    this.confirmationService.confirm({
      acceptLabel: "Ναι",
      rejectLabel: "Όχι",
      message: 'Θέλετε να δημιουργήσετε αίτημα συμμετοχής για αυτή την ψηφοφορία;',
      header: 'Αίτημα συμμετοχής',
      icon: 'pi pi-exclamation-triangle',
      acceptIcon: "none",
      rejectIcon: "none",
      rejectButtonStyleClass: "p-button-text",
      accept: () => {
        this.createAccessRequest(id);
        this.resetConfirmDialog();
      },
      reject: () => {
        this.resetConfirmDialog();
      }
    });
  }

  createAccessRequest(id: number) {
    this.votingsService.requestAccessToVoting(id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'info',
          summary: 'Πληροφορία',
          detail: 'Το αίτημα συμμετοχής δημιουργήθηκε'
        });
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: error.error.error
        });
      }
    });
  }

  private hasTheSameRoles(response: string[]): boolean {
    let storedRoles: string[] = localStorage.getItem('roles')?.split(',') ?? [];

    return storedRoles.length === response.length &&
      storedRoles.every(role => response.includes(role)) &&
      response.every(role => storedRoles.includes(role));
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (this.receivedMultiSelect && this.receivedMultiSelect.overlayVisible && !this.receivedMultiSelect.el.nativeElement.contains(target)) {
      this.resetReceivedMultiSelect();
    }
    if (this.sentMultiSelect && this.sentMultiSelect.overlayVisible && !this.sentMultiSelect.el.nativeElement.contains(target)) {
      this.resetSentMultiSelect();
    }
  }

  resetReceivedMultiSelect() {
    if (this.receivedMultiSelect) {
      this.allowReceivedMultiSelect = false;
      this.receivedMultiSelect.overlayVisible = false;
      setTimeout(() => {
        this.allowReceivedMultiSelect = true;
      }, 0);
    }
  }

  resetSentMultiSelect() {
    if (this.sentMultiSelect) {
      this.allowSentMultiSelect = false;
      this.sentMultiSelect.overlayVisible = false;
      setTimeout(() => {
        this.allowSentMultiSelect = true;
      }, 0);
    }
  }

}

export interface Voting {
  id: number;
  name: string;
  topic: string;
  votes: number;
  comments: number;
}

export interface Delegation {
  name: string;
  surname: string;
  voting: string;
}

export interface Voting {
  id: number;
  name: string;
}

export interface Topic {
  id: number;
  name: string;
}

export interface ReceivedDelegation {
  voting: string;
  votes: number;
}
