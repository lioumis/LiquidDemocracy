import {Component, OnInit} from '@angular/core';
import {AuthService} from "../login/auth.service";
import {MessageService} from "primeng/api";
import {PanelModule} from 'primeng/panel';
import {CarouselModule} from "primeng/carousel";
import {Button} from "primeng/button";
import {CardModule} from "primeng/card";
import {Table, TableModule} from "primeng/table";
import {MultiSelectModule} from "primeng/multiselect";
import {FormsModule} from "@angular/forms";

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
    FormsModule
  ],
  providers: [AuthService, MessageService],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  delegations: Delegation[] = [];

  receivedDelegations: ReceivedDelegation[] = [];

  topics: string[] = [];

  loading: boolean = true;

  votings: Voting[] = [];

  responsiveOptions: any[] | undefined;

  constructor(private readonly authService: AuthService, private readonly messageService: MessageService) {
  }

  ngOnInit(): void {
    let identifier = localStorage.getItem('identifier');
    if (identifier) { //TODO: Check if needed
      this.authService.getUserDetails().subscribe(
        (response) => {
          localStorage.setItem('username', response.username);
          localStorage.setItem('name', response.name);
          localStorage.setItem('surname', response.surname);
          localStorage.setItem('email', response.email);
          localStorage.setItem('roles', response.roles);
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

    this.authService.getSuggestedVotings().subscribe(
      (response) => {
        this.votings = response;
      },
      (error) => {
        console.error('Σφάλμα:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: error.error
        });
      }
    )

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
    )

    this.authService.getDelegations().subscribe(
      (response) => {
        this.delegations = response;
      },
      (error) => {
        console.error('Σφάλμα:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: error.error
        });
      }
    )

    this.authService.getReceivedDelegations().subscribe(
      (response) => {
        this.receivedDelegations = response;
      },
      (error) => {
        console.error('Σφάλμα:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Σφάλμα',
          detail: error.error
        });
      }
    )

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

  clear(table: Table) {
    table.clear();
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
  topic: string;
}

export interface Topic {
  id: number;
  name: string;
}

export interface ReceivedDelegation {
  topic: string;
  votes: number;
}
