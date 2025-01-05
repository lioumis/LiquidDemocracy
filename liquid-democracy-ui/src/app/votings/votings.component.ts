import {Component, OnInit, ViewChild} from '@angular/core';
import {ToastModule} from "primeng/toast";
import {MultiSelectModule} from "primeng/multiselect";
import {PanelModule} from "primeng/panel";
import {TableModule} from "primeng/table";
import {FormsModule} from "@angular/forms";
import {AuthService} from "../login/auth.service";
import {MessageService} from "primeng/api";
import {Delegation, Topic} from "../dashboard/dashboard.component";
import {Dropdown} from "primeng/dropdown";
import {ButtonDirective} from "primeng/button";
import {Ripple} from "primeng/ripple";
import {DatePipe} from "@angular/common";
import {Router} from "@angular/router";

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
    DatePipe
  ],
  providers: [AuthService, MessageService],
  templateUrl: './votings.component.html',
  styleUrl: './votings.component.css'
})
export class VotingsComponent implements OnInit {

  @ViewChild('dropdown') dropdown: Dropdown | undefined;

  delegations: Delegation[] = [];

  topics: string[] = [];

  votings: Voting[] = [];

  hasVotedOptions = ['Ναι', 'Όχι'];

  loading: boolean = true;

  constructor(private readonly authService: AuthService,
              private readonly router: Router,
              private readonly messageService: MessageService) {
  }

  ngOnInit(): void {
    this.authService.getTopics().subscribe(
      (response: Topic[]) => {
        this.topics = response.map((topic) => topic.name);
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

    this.authService.getVotings().subscribe(
      (response) => {
        this.votings = response.map((voting: Voting) => ({
          ...voting,
          startDate: new Date(voting.startDate),
          endDate: new Date(voting.endDate),
          hasVoted: voting.hasVoted === 'true' ? 'Ναι' : 'Όχι',
        }));
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

    this.loading = false;
  }

  selectVoting(id: number) {
    this.router.navigate(['/voting', id]);
  }

}

export interface Voting {
  id: number;
  name: string;
  topic: string;
  startDate: string;
  endDate: string;
  hasVoted: string;
  votes: number;
}
