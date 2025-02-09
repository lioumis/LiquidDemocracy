import {Component, OnInit} from '@angular/core';
import {ToastModule} from "primeng/toast";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../../login/auth.service";
import {MenuItem, MessageService} from "primeng/api";
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
    MultiSelectModule
  ],
  providers: [AuthService, MessageService],
  templateUrl: './voting.component.html',
  styleUrl: './voting.component.css'
})
export class VotingComponent implements OnInit {

  formGroup: FormGroup;

  multipleFormGroup: FormGroup;

  feedbackForm: FormGroup;

  votingId: number | null = null;

  votingDetails: VotingDetails | null = null;

  comments: Comment[] = [];

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

  constructor(private readonly route: ActivatedRoute,
              private readonly router: Router,
              private readonly authService: AuthService,
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
  }

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']).then();
    }

    // TODO: Check if allowed participant. If not, send back.

    this.votingId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadVotingDetails();
    this.loadComments();

    if (this.localStorage.getItem('selectedRole') === 'Εφορευτική Επιτροπή') {
      this.loadTable();
    }
  }

  loadVotingDetails(): void {
    if (this.votingId) {
      this.authService.getVotingDetails(this.votingId).subscribe({
        next: (response) => {
          this.votingDetails = response;


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
    if (!this.votingId) {
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
