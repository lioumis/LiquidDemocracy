import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {ToastModule} from "primeng/toast";
import {MultiSelect, MultiSelectModule} from "primeng/multiselect";
import {PanelModule} from "primeng/panel";
import {TableModule} from "primeng/table";
import {FormsModule} from "@angular/forms";
import {AuthService} from "../login/auth.service";
import {ConfirmationService, MenuItem, MessageService} from "primeng/api";
import {Delegation, Topic} from "../dashboard/dashboard.component";
import {ButtonDirective} from "primeng/button";
import {Ripple} from "primeng/ripple";
import {DatePipe} from "@angular/common";
import {Router} from "@angular/router";
import {BreadcrumbModule} from "primeng/breadcrumb";
import {ConfirmDialogModule} from "primeng/confirmdialog";
import {VotingsService} from "./votings.service";
import {AdministrationService} from "../administration/administration.service";
import {Calendar, CalendarModule} from "primeng/calendar";

@Component({
  selector: 'app-votings',
  standalone: true,
  imports: [
    ToastModule,
    MultiSelectModule,
    PanelModule,
    TableModule,
    FormsModule,
    ButtonDirective,
    Ripple,
    DatePipe,
    BreadcrumbModule,
    ConfirmDialogModule,
    CalendarModule
  ],
  providers: [AuthService, VotingsService, AdministrationService, MessageService, ConfirmationService],
  templateUrl: './votings.component.html',
  styleUrl: './votings.component.css'
})
export class VotingsComponent implements OnInit {

  @ViewChild('topicMultiSelect') topicMultiSelect!: MultiSelect;
  @ViewChild('hasVotedMultiSelect') hasVotedMultiSelect!: MultiSelect;
  @ViewChild('startCalendar') startCalendar: Calendar | undefined;
  @ViewChild('endCalendar') endCalendar: Calendar | undefined;

  allowTopicMultiSelect: boolean = true;
  allowHasVotedMultiSelect: boolean = true;
  allowStartCalendar: boolean = true;
  allowEndCalendar: boolean = true;

  delegations: Delegation[] = [];

  topics: string[] = [];

  votings: Voting[] = [];

  hasVotedOptions = ['Ναι', 'Όχι'];

  loading: boolean = true;

  items: MenuItem[] = [
    {label: 'Ψηφοφορίες'}
  ];

  home: MenuItem = {routerLink: ['/dashboard']};

  showConfirmDialog: boolean = true;

  showOnlyInactive: boolean = false;

  constructor(private readonly authService: AuthService,
              private readonly votingsService: VotingsService,
              private readonly administrationService: AdministrationService,
              private readonly router: Router,
              private readonly messageService: MessageService,
              private readonly confirmationService: ConfirmationService) {
  }

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.showOnlyInactive = true;
    }

    this.administrationService.getTopics().subscribe({
      next: (response: Topic[]) => {
        this.topics = response.map((topic) => topic.name);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: error.error
        });
      }
    });

    let role = localStorage.getItem('selectedRole');
    if (role) {
      this.votingsService.getVotings(role).subscribe({
        next: (response) => {
          this.votings = response.map((voting: Voting) => ({
            ...voting,
            startDate: voting.startDate ? new Date(voting.startDate) : null,
            endDate: voting.endDate ? new Date(voting.endDate) : null,
            hasVoted: voting.hasVoted ? 'Ναι' : 'Όχι',
          }));
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Σφάλμα',
            detail: error.error
          });
        }
      });
    } else {
      this.votingsService.getInactiveVotings().subscribe({
        next: (response) => {
          this.votings = response.map((voting: Voting) => ({
            ...voting,
            startDate: voting.startDate ? new Date(voting.startDate) : null,
            endDate: voting.endDate ? new Date(voting.endDate) : null,
          }));
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
    this.loading = false;
  }

  selectVoting(voting: Voting) {
    this.messageService.clear();

    if (this.isExpired(voting)) {
      this.router.navigate(['/voting', voting.id]).then();
      return;
    }
    this.votingsService.hasAccessToVoting(voting.id).subscribe({
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
            this.router.navigate(['/voting', voting.id]).then();
          }
        } else {
          this.displayDialog(voting.id);
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

  isExpired(voting: Voting) {
    if (!this.isValid(voting)) {
      return true;
    }

    if (voting?.endDate) {
      const currentDate = new Date();
      const votingEndDate = new Date(voting.endDate);
      votingEndDate.setHours(23, 59, 59, 0);
      return currentDate > votingEndDate;
    }
    return false;
  }

  isValid(voting: Voting): boolean {
    return !!(voting?.valid && voting?.valid === true);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (this.topicMultiSelect && this.topicMultiSelect.overlayVisible && !this.topicMultiSelect.el.nativeElement.contains(target)) {
      this.resetTopicMultiSelect();
    }
    if (this.hasVotedMultiSelect && this.hasVotedMultiSelect.overlayVisible && !this.hasVotedMultiSelect.el.nativeElement.contains(target)) {
      this.resetHasVotedMultiSelect();
    }
    if (this.startCalendar && this.startCalendar.overlayVisible && !this.startCalendar.el.nativeElement.contains(target)) {
      const calendarOverlay = document.querySelector('.p-datepicker-group');

      try {
        if (calendarOverlay && !calendarOverlay.contains(target) &&
          !target.className.includes('p-datepicker') && !target.className.includes('p-monthpicker') && !target.className.includes('p-yearpicker')) {
          this.resetStartCalendar();
        }
      } catch (e) {
      }
    }
    if (this.endCalendar && this.endCalendar.overlayVisible && !this.endCalendar.el.nativeElement.contains(target)) {
      const calendarOverlay = document.querySelector('.p-datepicker-group');

      try {
        if (calendarOverlay && !calendarOverlay.contains(target) &&
          !target.className.includes('p-datepicker') && !target.className.includes('p-monthpicker') && !target.className.includes('p-yearpicker')) {
          this.resetEndCalendar();
        }
      } catch (e) {
      }
    }
  }

  resetTopicMultiSelect() {
    if (this.topicMultiSelect) {
      this.allowTopicMultiSelect = false;
      this.topicMultiSelect.overlayVisible = false;
      setTimeout(() => {
        this.allowTopicMultiSelect = true;
      }, 0);
    }
  }

  resetHasVotedMultiSelect() {
    if (this.hasVotedMultiSelect) {
      this.allowHasVotedMultiSelect = false;
      this.hasVotedMultiSelect.overlayVisible = false;
      setTimeout(() => {
        this.allowHasVotedMultiSelect = true;
      }, 0);
    }
  }

  resetStartCalendar() {
    if (this.startCalendar) {
      this.allowStartCalendar = false;
      this.startCalendar.overlayVisible = false;
      setTimeout(() => {
        this.allowStartCalendar = true;
      }, 0);
    }
  }

  resetEndCalendar() {
    if (this.endCalendar) {
      this.allowEndCalendar = false;
      this.endCalendar.overlayVisible = false;
      setTimeout(() => {
        this.allowEndCalendar = true;
      }, 0);
    }
  }
}

export interface Voting {
  id: number;
  name: string;
  topic: string;
  startDate: string;
  endDate: string;
  valid: boolean;
  hasVoted: string;
  votes: number;
}
