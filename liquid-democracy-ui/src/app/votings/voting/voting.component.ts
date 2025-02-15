import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {ToastModule} from "primeng/toast";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../../login/auth.service";
import {ConfirmationService, MenuItem, MessageService} from "primeng/api";
import {PanelModule} from "primeng/panel";
import {DataViewModule} from "primeng/dataview";
import {Button, ButtonDirective} from "primeng/button";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {NgForOf} from "@angular/common";
import {RadioButtonModule} from "primeng/radiobutton";
import {InputTextareaModule} from "primeng/inputtextarea";
import {ChartModule} from "primeng/chart";
import {BreadcrumbModule} from "primeng/breadcrumb";
import {CheckboxModule} from "primeng/checkbox";
import {TableModule} from "primeng/table";
import {MultiSelectModule} from "primeng/multiselect";
import {CalendarModule} from "primeng/calendar";
import {Dropdown, DropdownModule} from "primeng/dropdown";
import {InputNumberModule} from "primeng/inputnumber";
import {InputTextModule} from "primeng/inputtext";
import {Ripple} from "primeng/ripple";
import {ConfirmDialogModule} from "primeng/confirmdialog";

@Component({
  selector: 'app-voting',
  standalone: true,
  imports: [
    ToastModule,
    PanelModule,
    DataViewModule,
    Button,
    FormsModule,
    NgForOf,
    RadioButtonModule,
    CheckboxModule,
    ReactiveFormsModule,
    InputTextareaModule,
    ChartModule,
    ButtonDirective,
    BreadcrumbModule,
    TableModule,
    MultiSelectModule,
    CalendarModule,
    DropdownModule,
    InputNumberModule,
    InputTextModule,
    Ripple,
    ConfirmDialogModule
  ],
  providers: [AuthService, MessageService, ConfirmationService],
  templateUrl: './voting.component.html',
  styleUrl: './voting.component.css'
})
export class VotingComponent implements OnInit {
  @ViewChild('mechanismDropdown') mechanismDropdown: Dropdown | undefined;

  allowMechanismDropdown: boolean = true;

  showConfirmDialog: boolean = true;

  mechanisms: string[] = ['Μοναδική Επιλογή', 'Πολλαπλή Επιλογή'];

  selectedMechanism: string | null = null;

  votingOptions: VotingOption[] = [
    {title: '', details: ''},
    {title: '', details: ''}
  ];

  maxSelections: number | null = null;

  formGroup: FormGroup;

  multipleFormGroup: FormGroup;

  feedbackForm: FormGroup;

  votingId: number | null = null;

  votingDetails: VotingDetails | null = null;

  comments: Comment[] = [];

  allFeedback: string[] = [];

  newComment: string = '';

  feedback: string = '';

  resultData: any;

  resultOptions: any;

  distributionData: any;

  distributionOptions: any;

  requestDetails: ParticipationRequest[] = [];

  selectedRequest: ParticipationRequest | null = null;

  loading: boolean = true;

  items: MenuItem[] | undefined;

  home: MenuItem = {routerLink: ['/dashboard']};

  protected readonly localStorage = localStorage;

  editMode: boolean = false;
  startDate: Date | null = null;
  endDate: Date | null = null;
  information: string | null = null;
  minStartDate: Date;
  maxStartDate: Date | null = null;
  minEndDate: Date;

  constructor(private readonly route: ActivatedRoute,
              private readonly router: Router,
              private readonly authService: AuthService,
              private readonly confirmationService: ConfirmationService,
              private readonly messageService: MessageService,
              private readonly fb: FormBuilder) {
    this.formGroup = this.fb.group({
      vote: [{value: '', disabled: false}, Validators.required]
    });
    this.multipleFormGroup = this.fb.group({
      vote: [{value: '', disabled: false}, Validators.required]
    });
    this.feedbackForm = this.fb.group({
      feedback: ['', Validators.required]
    })
    const today = new Date();
    const normalizedToday = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    this.minStartDate = normalizedToday;
    this.minEndDate = new Date(normalizedToday.getTime() + 24 * 60 * 60 * 1000);
  }

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']).then();
    }

    this.votingId = Number(this.route.snapshot.paramMap.get('id'));

    this.hasPermission();

    this.loadVotingDetails();
    this.loadComments();
  }

  hasPermission() {
    if (this.votingId) {
      this.authService.hasAccessToVoting(this.votingId).subscribe({
        next: (response) => {
          if (!response.isPresent || !response.hasAccess) {
            this.router.navigate(['/dashboard']).then();
          }
        },
        error: () => {
          this.router.navigate(['/dashboard']).then();
        }
      });
    }
  }

  loadVotingDetails(): void {
    if (this.votingId) {
      this.authService.getVotingDetails(this.votingId).subscribe({
        next: (response) => {
          this.votingDetails = response;

          if (this.votingDetails?.startDate) {
            const backendStartDate = new Date(this.votingDetails.startDate);
            this.minStartDate = new Date(Math.max(backendStartDate.getTime(), this.minStartDate.getTime()));
            this.startDate = new Date(this.votingDetails.startDate);
            this.minEndDate = new Date(this.startDate.getFullYear(), this.startDate.getMonth(), this.startDate.getDate() + 1);
          }

          if (this.votingDetails?.endDate) {
            const backendEndDate = new Date(this.votingDetails.endDate);
            if (backendEndDate >= this.minEndDate) {
              this.endDate = backendEndDate;
            }
          }

          this.information = response.information;

          if (response.votingType === 1) {
            this.selectedMechanism = "Μοναδική Επιλογή";
          } else if (response.votingType === 2) {
            this.selectedMechanism = "Πολλαπλή Επιλογή";
          }

          this.maxSelections = response.voteLimit;

          if (response.results && response.results.length > 0) {
            this.votingOptions = response.results.map((item: any) => JSON.parse(JSON.stringify(item.option)));
          }

          if (this.votingDetails?.votingType === 1) {
            if (this.votingDetails?.userVote) {
              this.formGroup.get('vote')?.disable();
              this.formGroup.get('vote')?.setValue(this.votingDetails?.userVote[0]?.title);
            }
          }

          if (this.votingDetails?.votingType === 2) {
            if (this.votingDetails?.userVote) {
              this.multipleFormGroup.get('vote')?.disable();
              let userVoteTitles = this.votingDetails?.userVote.map(vote => vote.title) || [];
              this.multipleFormGroup.get('vote')?.setValue(userVoteTitles);
            }
          }

          if (this.isExpired()) {
            this.formGroup.get('vote')?.disable();
            this.resultData = this.transformToBarChartData(this.votingDetails);
            this.distributionData = this.transformToPieChartData(this.votingDetails);
          }

          if (this.votingDetails?.feedback) {
            this.feedback = this.votingDetails?.feedback;
          }

          if (this.localStorage.getItem('selectedRole') === 'Εφορευτική Επιτροπή') {
            if (!this.hasStarted()) {
              this.editMode = true;
            }
            this.loadFeedback();
            this.loadTable();
          }

          this.items = [
            {label: 'Ψηφοφορίες', routerLink: ['/votings']},
            {label: this.votingDetails?.name}
          ];

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
    }
  }

  transformToBarChartData(votingData: any): any {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');
    const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');

    this.resultOptions = {
      indexAxis: 'y',
      maintainAspectRatio: false,
      aspectRatio: 0.8,
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        }
      },
      scales: {
        x: {
          ticks: {
            color: textColorSecondary,
            font: {
              weight: 500
            },
            stepSize: 1
          },
          grid: {
            display: false
          }
        },
        y: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            display: false
          }
        }
      }
    };

    return {
      labels: votingData.results.map((result: any) => result.option.title),
      datasets: [
        {
          label: 'Ψήφοι',
          backgroundColor: documentStyle.getPropertyValue('--blue-500'),
          borderColor: documentStyle.getPropertyValue('--blue-500'),
          data: votingData.results.map((result: any) => result.count),
        },
      ],
    };
  }

  transformToPieChartData(votingData: any): any {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');

    this.distributionOptions = {
      plugins: {
        legend: {
          labels: {
            usePointStyle: true,
            color: textColor
          }
        }
      }
    };

    return {
      labels: ['Άμεσες ψήφοι', 'Εξουσιοδοτημένες ψήφοι'],
      datasets: [
        {
          data: [votingData.directVotes, votingData.delegatedVotes],
          backgroundColor: [
            documentStyle.getPropertyValue('--blue-500'),
            documentStyle.getPropertyValue('--yellow-500')
          ],
          hoverBackgroundColor: [
            documentStyle.getPropertyValue('--blue-400'),
            documentStyle.getPropertyValue('--yellow-400')
          ],
        },
      ],
    };
  }

  loadComments(): void {
    if (this.votingId && !this.isExpired()) {
      this.authService.getDiscussion(this.votingId).subscribe({
        next: (response) => {
          this.comments = response;
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
    }
  }

  loadFeedback() {
    if (this.votingId && this.isExpired()) {
      this.authService.getFeedback(this.votingId).subscribe({
        next: (response: Feedback[]) => {
          this.allFeedback = response.map(item => item.feedback);
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
    }
  }

  like(id: number): void {
    let comment = this.comments.find(comment => comment.id === id);
    if (!comment) {
      return;
    }

    this.authService.react(comment.id, true).subscribe({
      next: () => {
        if (comment?.userAction === null) {
          comment.userAction = true;
          comment.likes++;
        } else if (comment?.userAction === true) {
          comment.userAction = null;
          comment.likes--;
        } else if (comment?.userAction === false) {
          comment.userAction = true;
          comment.likes++;
          comment.dislikes--;
        }
      },
      error: (error) => {
        console.error('Σφάλμα:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: error.error.error
        });
      }
    });
  }

  dislike(id: number): void {
    let comment = this.comments.find(comment => comment.id === id);
    if (!comment) {
      return;
    }

    this.authService.react(comment.id, false).subscribe({
      next: () => {
        if (comment?.userAction === null) {
          comment.userAction = false;
          comment.dislikes++;
        } else if (comment?.userAction === true) {
          comment.userAction = false;
          comment.likes--;
          comment.dislikes++;
        } else if (comment?.userAction === false) {
          comment.userAction = null;
          comment.dislikes--;
        }
      },
      error: (error) => {
        console.error('Σφάλμα:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: error.error.error
        });
      }
    });
  }

  submitComment() {
    if (this.newComment.trim() && this.votingId && !this.isExpired()) {
      this.authService.addComment(this.votingId, this.newComment).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Επιτυχία',
            detail: 'Το σχόλιο προστέθηκε με επιτυχία'
          });
          this.loadComments();
          this.newComment = '';
        },
        error: (error) => {
          console.error('Σφάλμα:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Σφάλμα',
            detail: error.error.error
          });
        }
      });
    }
  }

  submitFeedback() {
    if (this.feedback.trim() && this.votingId && this.isExpired()) {
      this.authService.submitFeedback(this.votingId, this.feedback).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Επιτυχία',
            detail: 'H ανατροφοδότηση υποβλήθηκε επιτυχώς'
          });
          this.loadVotingDetails()
        },
        error: (error) => {
          console.error('Σφάλμα:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Σφάλμα',
            detail: error.error.error
          });
        }
      });
      this.newComment = '';
    }
  }

  checkComment() {
    return !this.newComment?.trim();
  }

  checkFeedback() {
    return !this.feedback?.trim() || !!this.votingDetails?.feedback;
  }

  feedbackExists() {
    return !!this.votingDetails?.feedback;
  }

  isExpired() {
    let endDateString = this.votingDetails?.endDate;
    if (endDateString) {
      const currentDate = new Date();
      const votingEndDate = new Date(`${endDateString}T23:59:59`); //TODO: Check if expires at EOD
      return currentDate > votingEndDate
    }
    return false;
  }

  hasStartDate() {
    return !!this.votingDetails?.startDate;
  }

  hasStarted() {
    let startDateString = this.votingDetails?.startDate;
    if (startDateString) {
      const currentDate = new Date();
      const votingStartDate = new Date(`${startDateString}T23:59:59`);
      return currentDate > votingStartDate
    }
    return false;
  }

  onSubmit(): void {
    this.messageService.clear();
    if (this.formGroup.valid && this.votingId) {
      const option = this.formGroup.value;

      this.authService.castVote([option.vote], this.votingId).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Επιτυχία',
            detail: 'Η ψήφος καταχωρήθηκε επιτυχώς'
          });
          this.loadVotingDetails();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Σφάλμα',
            detail: error.error.error || 'Προέκυψε σφάλμα κατά την καταχώρηση της ψήφου'
          });
        }
      });
    }
  }

  getVotingTypeString(votingType: any, voteLimit: any): string {
    if (!votingType) {
      return "";
    }

    switch (votingType) {
      case 1:
        return "Μία επιλογή";
      case 2:
        if (voteLimit) {
          return "Πολλαπλή επιλογή, έως " + voteLimit + " ψήφοι";
        }
        return "Πολλαπλή επιλογή";
      default:
        return "";
    }
  }

  onSubmitMultiple(): void {
    this.messageService.clear();
    if (this.multipleFormGroup.valid && this.votingId) {
      const option = this.multipleFormGroup.value;

      if (this.votingDetails?.voteLimit && this.votingDetails.voteLimit < option.vote.length) {
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: 'Έχετε κάνει παραπάνω επιλογές από τις επιτρεπόμενες'
        });
        return;
      }

      this.authService.castVote(option.vote, this.votingId).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Επιτυχία',
            detail: 'Η ψήφος καταχωρήθηκε επιτυχώς'
          });
          this.loadVotingDetails();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Σφάλμα',
            detail: error.error.error || 'Προέκυψε σφάλμα κατά την καταχώρηση της ψήφου'
          });
        }
      });
    }
  }

  loadTable() {
    if (!this.votingId || !this.hasStartDate() || this.hasStarted()) {
      return;
    }
    this.loading = true;
    this.selectedRequest = null;
    this.authService.getParticipationRequests(this.votingId).subscribe({
      next: (response) => {
        this.requestDetails = response;
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

  processRequest(action: boolean) {
    if (this.selectedRequest) {
      this.authService.processRequest(this.selectedRequest.id, action).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Επιτυχία',
            detail: action ? 'Το αίτημα έγινε δεκτό με επιτυχία' : 'Το αίτημα απορρίφθηκε με επιτυχία'
          });
          this.selectedRequest = null;
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

  saveVoting() {
    if (this.selectedMechanism === 'Πολλαπλή Επιλογή' && this.maxSelections !== null && this.maxSelections > this.votingOptions?.length) {
      this.messageService.add({
        severity: 'error',
        summary: 'Αποτυχία',
        detail: 'Ο μέγιστος αριθμός επιλογών είναι μεγαλύτερος από τον αριθμό επιλογών που έχουν δοθεί'
      });
      return;
    }

    if (this.selectedMechanism === 'Μοναδική Επιλογή') {
      this.maxSelections = null;
    }

    if (this.startDate && this.votingDetails?.startDate === '') {
      this.displayDialog();
    } else {
      this.saveChanges();
    }
  }

  saveChanges() {
    if (!this.votingId) {
      return;
    }
    this.authService.editVoting(this.votingId, this.startDate, this.endDate, this.information, this.selectedMechanism, this.votingOptions, this.maxSelections).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Επιτυχία',
          detail: 'Οι αλλαγές αποθηκεύτηκαν'
        });
        this.loadVotingDetails();
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

  onStartDateSelect(event: Date): void {
    const selectedDate = new Date(event.getFullYear(), event.getMonth(), event.getDate());
    if (selectedDate < this.minStartDate) {
      this.startDate = this.minStartDate;
    } else {
      this.startDate = selectedDate;
    }
    this.minEndDate = new Date(this.startDate.getFullYear(), this.startDate.getMonth(), this.startDate.getDate() + 1);
    if (this.endDate && this.endDate < this.minEndDate) {
      this.endDate = null;
    }
  }

  onEndDateSelect(event: Date): void {
    const selectedDate = new Date(event.getFullYear(), event.getMonth(), event.getDate());
    if (selectedDate < this.minEndDate) {
      this.endDate = this.minEndDate;
    } else {
      this.endDate = selectedDate;
    }
  }

  onMechanismChange(event: any) {
    this.selectedMechanism = event.value;
    this.resetDropdown();
  }

  resetDropdown() {
    if (this.mechanismDropdown) {
      this.allowMechanismDropdown = false;
      this.mechanismDropdown.overlayVisible = false;
      setTimeout(() => {
        this.allowMechanismDropdown = true;
      }, 0);
    }
  }

  canSaveVoting(): boolean {
    if (!this.changesExist()) {
      return false;
    }

    for (const element of this.votingOptions) {
      let option = element;
      if (option.title === null || option.title.trim() === '') {
        return false;
      }
      if (option.details === null || option.details.trim() === '') {
        return false;
      }
    }

    return true;
  }

  private changesExist() {
    let originalStartDate = this.votingDetails?.startDate ? new Date(this.votingDetails?.startDate) : null;
    if ((this.startDate?.getTime() || 0) !== (originalStartDate?.getTime() || 0)) {
      return true;
    }

    let originalEndDate = this.votingDetails?.endDate ? new Date(this.votingDetails?.endDate) : null;
    if ((this.endDate?.getTime() || 0) !== (originalEndDate?.getTime() || 0)) {
      return true;
    }

    if (this.information !== this.votingDetails?.information) {
      return true;
    }

    if (this.selectedMechanism !== this.translateMechanism(this.votingDetails?.votingType)) {
      return true;
    }

    if (this.selectedMechanism === 'Πολλαπλή Επιλογή' && this.maxSelections !== this.votingDetails?.voteLimit) {
      return true;
    }

    return !this.areOptionsSame(this.votingOptions, this.votingDetails.results.map((item: any) => item.option));
  }

  private areOptionsSame(a: VotingOption[], b: VotingOption[]): boolean {
    if (b === null) {
      return false;
    }
    if (a.length !== b.length) return false;
    for (let i = 0; i < a.length; i++) {
      if (a[i].title !== b[i].title || a[i].details !== b[i].details) {
        return false;
      }
    }
    return true;
  }

  translateMechanism(mechanism: number) {
    if (mechanism === 1) {
      return 'Μοναδική Επιλογή';
    }

    if (mechanism === 2) {
      return 'Πολλαπλή Επιλογή';
    }

    return null;
  }

  displayDialog() {
    this.confirmationService.confirm({
      acceptLabel: "Ναι",
      rejectLabel: "Όχι",
      message: 'Αν προστεθεί ημερομηνία έναρξης, η ψηφοφορία θα ενεργοποιηθεί και θα εμφανίζεται στους ψηφοφόρους.<br>Οι διαθέσιμες αλλαγές μετά από αυτή την ενέργεια θα είναι περιορισμένες. Συνέχεια;',
      header: 'Προσοχή!',
      icon: 'pi pi-exclamation-triangle',
      acceptIcon: "none",
      rejectIcon: "none",
      rejectButtonStyleClass: "p-button-text",
      accept: () => {
        this.saveChanges()
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
    if (this.mechanismDropdown && this.mechanismDropdown.overlayVisible && !this.mechanismDropdown.el.nativeElement.contains(target)) {
      this.resetDropdown();
    }
  }

}

export interface VotingDetails {
  name: string;
  topic: string;
  startDate: string;
  endDate: string;
  information: string;
  delegated: boolean | null;
  votingType: number;
  voteLimit: number | null;
  results: VotingResult[];
  userVote: VotingOption[];
  directVotes: number;
  delegatedVotes: number;
  feedback: string;
}

export interface VotingResult {
  option: VotingOption;
  count: number;
}

export interface VotingOption {
  title: string;
  details: string;
}

export interface Comment {
  id: number;
  name: string;
  surname: string;
  message: string;
  likes: number;
  dislikes: number;
  userAction: boolean | null;
}

export interface ParticipationRequest {
  id: number;
  name: string;
  surname: string;
  username: string;
}

export interface Feedback {
  feedback: string;
}
