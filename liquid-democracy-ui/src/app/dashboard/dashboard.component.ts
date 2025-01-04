import {Component, OnInit} from '@angular/core';
import {AuthService} from "../login/auth.service";
import {MessageService} from "primeng/api";
import {PanelModule} from 'primeng/panel';
import {CarouselModule} from "primeng/carousel";
import {Button} from "primeng/button";
import {CardModule} from "primeng/card";

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    PanelModule,
    CarouselModule,
    Button,
    CardModule
  ],
  providers: [AuthService, MessageService],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  votings: Voting[] = [ //TODO: Remove hardcoded values
    {
      id: 1,
      name: "Όνομα Ψηφοφορίας 1",
      topic: "Θεματική Περιοχή 1",
      votes: 12,
      comments: 14
    },
    {
      id: 2,
      name: "Όνομα Ψηφοφορίας 2",
      topic: "Θεματική Περιοχή 2",
      votes: 22,
      comments: 24
    },
    {
      id: 3,
      name: "Όνομα Ψηφοφορίας 3",
      topic: "Θεματική Περιοχή 3",
      votes: 32,
      comments: 34
    },
    {
      id: 4,
      name: "Όνομα Ψηφοφορίας 4",
      topic: "Θεματική Περιοχή 4",
      votes: 42,
      comments: 44
    },
    {
      id: 5,
      name: "Όνομα Ψηφοφορίας 5",
      topic: "Θεματική Περιοχή 5",
      votes: 52,
      comments: 54
    },
    {
      id: 6,
      name: "Όνομα Ψηφοφορίας 6",
      topic: "Θεματική Περιοχή 6",
      votes: 62,
      comments: 64
    }
  ];

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
  }

}

export interface Voting {
  id: number;
  name: string;
  topic: string;
  votes: number;
  comments: number;
}
