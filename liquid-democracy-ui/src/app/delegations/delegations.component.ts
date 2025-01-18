import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {MultiSelectModule} from "primeng/multiselect";
import {PanelModule} from "primeng/panel";
import {MenuItem, MessageService, PrimeTemplate} from "primeng/api";
import {TableModule} from "primeng/table";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {Delegation, Topic} from "../dashboard/dashboard.component";
import {AuthService} from "../login/auth.service";
import {Dropdown, DropdownModule} from "primeng/dropdown";
import {Button} from "primeng/button";
import {ToastModule} from "primeng/toast";
import {BreadcrumbModule} from "primeng/breadcrumb";

@Component({
  selector: 'app-delegations',
  standalone: true,
  imports: [
    MultiSelectModule,
    PanelModule,
    PrimeTemplate,
    TableModule,
    FormsModule,
    ReactiveFormsModule,
    DropdownModule,
    ToastModule,
    Button,
    BreadcrumbModule
  ],
  providers: [AuthService, MessageService, BreadcrumbModule],
  templateUrl: './delegations.component.html',
  styleUrl: './delegations.component.css'
})
export class DelegationsComponent implements OnInit {
  @ViewChild('dropdown') dropdown: Dropdown | undefined;

  delegationForm: FormGroup;

  selectedTopic: string = '';

  delegations: Delegation[] = [];

  topics: string[] = [];

  completeTopics: Topic[] = [];

  loading: boolean = true;

  items: MenuItem[] = [
    {label: 'Αναθέσεις'}
  ];

  home: MenuItem = {routerLink: ['/dashboard']};

  constructor(private readonly authService: AuthService,
              private readonly messageService: MessageService,
              private readonly fb: FormBuilder) {
    this.delegationForm = this.fb.group({
      name: ['', Validators.required],
      surname: ['', Validators.required],
      topic: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.authService.getTopics().subscribe(
      (response: Topic[]) => {
        this.topics = response.map((topic) => topic.name);
        this.completeTopics = response;
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

    this.loadTable();

    this.loading = false;
  }

  onTopicChange(event: any) {
    this.selectedTopic = event.value;
    if (this.dropdown) {
      this.dropdown.overlayVisible = false;
    }
  }

  onSubmit(): void {
    this.messageService.clear();
    if (this.delegationForm.valid) {
      const {name, surname, topic} = this.delegationForm.value;
      this.authService.createDelegation(name, surname, this.getTopicId(topic)).subscribe( //TODO: Verification Pop-up!
        (response) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Επιτυχία',
            detail: 'Η ανάθεση ολοκληρώθηκε επιτυχώς'
          });
          this.resetForm();
          this.loadTable();
        },
        (error) => {
          console.error('Delegation failed', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Αποτυχία',
            detail: error.error.error //TODO: This works here. Check other places.
          });
        }
      );
    }
  }

  getTopicId(topic: string) {
    let completeTopic = this.completeTopics.find(t => t.name === topic);
    return completeTopic ? completeTopic.id : -1;
  }

  resetForm() {
    this.delegationForm.reset();
  }

  loadTable() {
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
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (this.dropdown && !this.dropdown.el.nativeElement.contains(target)) {
      this.dropdown.overlayVisible = true;
      this.dropdown.overlayVisible = false;
    }
  }
}
