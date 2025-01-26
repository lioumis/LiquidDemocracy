import {Component, OnInit} from '@angular/core';
import {AuthService} from "../login/auth.service";
import {MessageService} from "primeng/api";
import {PanelModule} from 'primeng/panel';
import {CarouselModule} from "primeng/carousel";
import {Button} from "primeng/button";
import {CardModule} from "primeng/card";
import {TableModule} from "primeng/table";
import {MultiSelectModule} from "primeng/multiselect";
import {FormsModule} from "@angular/forms";
import {ToastModule} from "primeng/toast";
import {Router} from "@angular/router";
import {TooltipModule} from "primeng/tooltip";

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
    TooltipModule
  ],
  providers: [AuthService, MessageService],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  delegations: Delegation[] = [];

  receivedDelegations: ReceivedDelegation[] = [];

  loading: boolean = true;

  votings: Voting[] = [];

  allVotings: string[] = [];

  responsiveOptions: any[] | undefined;

  constructor(private readonly authService: AuthService,
              private readonly router: Router,
              private readonly messageService: MessageService) {
  }

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']).then();
    }

    this.authService.getUserDetails().subscribe({
      next: (response) => {
        localStorage.setItem('username', response.username);
        localStorage.setItem('name', response.name);
        localStorage.setItem('surname', response.surname);
        localStorage.setItem('email', response.email);
        localStorage.setItem('roles', response.roles);
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

    this.authService.getSuggestedVotings().subscribe({
      next: (response) => {
        this.votings = response;
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

    this.authService.getAllVotings().subscribe({
      next: (response: Voting[]) => {
        this.allVotings = response.map((voting) => voting.name);
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

    this.authService.getDelegations().subscribe({
      next: (response) => {
        this.delegations = response;
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

    this.authService.getReceivedDelegations().subscribe({
      next: (response) => {
        this.receivedDelegations = response;
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
    this.router.navigate(['/voting', id]).then();
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
