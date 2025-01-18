import {Component, OnInit} from '@angular/core';
import {ToastModule} from "primeng/toast";
import {ActivatedRoute} from "@angular/router";
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
    ReactiveFormsModule,
    InputTextareaModule,
    ChartModule,
    ButtonDirective,
    BreadcrumbModule
  ],
  providers: [AuthService, MessageService],
  templateUrl: './voting.component.html',
  styleUrl: './voting.component.css'
})
export class VotingComponent implements OnInit {

  formGroup: FormGroup;

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

  items: MenuItem[] | undefined;

  home: MenuItem = {routerLink: ['/dashboard']};

  constructor(private readonly route: ActivatedRoute,
              private readonly authService: AuthService,
              private readonly messageService: MessageService,
              private readonly fb: FormBuilder) {
    this.formGroup = this.fb.group({
      vote: [{value: '', disabled: false}, Validators.required]
    });
    this.feedbackForm = this.fb.group({
      feedback: ['', Validators.required]
    })
  }

  ngOnInit(): void {
    this.votingId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadVotingDetails();
    this.loadComments();
  }

  loadVotingDetails(): void {
    if (this.votingId) {
      this.authService.getVotingDetails(this.votingId).subscribe(
        (response) => {
          this.votingDetails = response;

          if (this.votingDetails?.userVote) {
            this.formGroup.get('vote')?.disable();
            this.formGroup.get('vote')?.setValue(this.votingDetails?.userVote.title);
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
        (error) => {
          console.error('Σφάλμα:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Σφάλμα',
            detail: error.error
          });
        }
      );
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
    if (this.votingId) { //TODO: & If the voting is still active
      this.authService.getDiscussion(this.votingId).subscribe(
        (response) => {
          this.comments = response;
        },
        (error) => {
          console.error('Σφάλμα:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Σφάλμα',
            detail: error.error
          });
        }
      );
    }
  }

  like(id: number): void {
    let comment = this.comments.find(comment => comment.id === id);
    if (!comment) {
      return;
    }

    this.authService.react(comment.id, true).subscribe(
      () => {
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
      (error) => {
        console.error('Σφάλμα:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: error.error.error
        });
      }
    );
  }

  dislike(id: number): void {
    let comment = this.comments.find(comment => comment.id === id);
    if (!comment) {
      return;
    }

    this.authService.react(comment.id, false).subscribe(
      () => {
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
      (error) => {
        console.error('Σφάλμα:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: error.error.error
        });
      }
    );
  }

  submitComment() {
    if (this.newComment.trim() && this.votingId) { //TODO: If the voting is still active
      this.authService.addComment(this.votingId, this.newComment).subscribe(
        () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Επιτυχία',
            detail: 'Το σχόλιο προστέθηκε με επιτυχία'
          });
          this.loadComments();
        },
        (error) => {
          console.error('Σφάλμα:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Σφάλμα',
            detail: error.error.error
          });
        }
      );
      this.newComment = '';
    }
  }

  submitFeedback() {
    if (this.feedback.trim() && this.votingId) { //TODO: & If the voting is inactive
      this.authService.submitFeedback(this.votingId, this.feedback).subscribe(
        () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Επιτυχία',
            detail: 'H ανατροφοδότηση υποβλήθηκε επιτυχώς'
          });
          this.loadVotingDetails()
        },
        (error) => {
          console.error('Σφάλμα:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Σφάλμα',
            detail: error.error.error
          });
        }
      );
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

}

export interface VotingDetails {
  name: string;
  topic: string;
  startDate: string;
  endDate: string;
  information: string;
  delegated: boolean | null;
  votingType: number;
  results: VotingResult[];
  userVote: VotingOption;
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
