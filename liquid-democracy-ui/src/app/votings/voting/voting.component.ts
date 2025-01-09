import {Component, OnInit} from '@angular/core';
import {ToastModule} from "primeng/toast";
import {ActivatedRoute} from "@angular/router";
import {AuthService} from "../../login/auth.service";
import {MessageService} from "primeng/api";
import {PanelModule} from "primeng/panel";
import {DataViewModule} from "primeng/dataview";
import {Button} from "primeng/button";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {NgForOf} from "@angular/common";
import {RadioButtonModule} from "primeng/radiobutton";

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
    ReactiveFormsModule
  ],
  providers: [AuthService, MessageService],
  templateUrl: './voting.component.html',
  styleUrl: './voting.component.css'
})
export class VotingComponent implements OnInit {

  formGroup: FormGroup;

  votingId: number | null = null;

  votingDetails: VotingDetails | null = null;

  comments: Comment[] = [];

  newComment: string = '';

  constructor(private readonly route: ActivatedRoute,
              private readonly authService: AuthService,
              private readonly messageService: MessageService,
              private readonly fb: FormBuilder) {
    this.formGroup = this.fb.group({
      vote: [{value: '', disabled: false}, Validators.required]
    });
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
          detail: error.error
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
          detail: error.error
        });
      }
    );
  }

  submitComment() {
    if (this.newComment.trim() && this.votingId) { //TODO: & If the voting is still active
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
            detail: error.error
          });
        }
      );
      this.newComment = '';
    }
  }

  checkComment() {
    return !this.newComment?.trim();
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
      console.log(option);

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
