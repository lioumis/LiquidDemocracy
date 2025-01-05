import {Component, OnInit} from '@angular/core';
import {ToastModule} from "primeng/toast";
import {ActivatedRoute} from "@angular/router";
import {AuthService} from "../../login/auth.service";
import {MessageService} from "primeng/api";

@Component({
  selector: 'app-voting',
  standalone: true,
  imports: [
    ToastModule
  ],
  providers: [AuthService, MessageService],
  templateUrl: './voting.component.html',
  styleUrl: './voting.component.css'
})
export class VotingComponent implements OnInit {

  votingId: number | null = null;

  votingDetails: VotingDetails | null = null;

  constructor(private readonly route: ActivatedRoute,
              private readonly authService: AuthService,
              private readonly messageService: MessageService) {
  }

  ngOnInit(): void {
    this.votingId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadVotingDetails();
  }

  loadVotingDetails(): void {
    if (this.votingId) {
      this.authService.getVotingDetails(this.votingId).subscribe(
        (response) => {
          this.votingDetails = response;
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

}

export interface VotingDetails {
  name: string;
  topic: string;
  startDate: string;
  endDate: string;
  information: string;
  delegated: boolean;
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
