import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {VotingOption} from "./voting/voting.component";
import {formatDate} from "@angular/common";

@Injectable({
  providedIn: 'root'
})
export class VotingsService {
  private readonly API_REL_PATH = '..';

  private readonly VOTINGS = '/votings';
  private readonly SUGGESTED_VOTINGS = '/getSuggestedVotings';
  private readonly ALL_VOTINGS = '/getVotings';
  private readonly INACTIVE_VOTINGS = '/getInactiveVotings';
  private readonly VOTING_DETAILS = '/getVotingDetails';
  private readonly INACTIVE_VOTING_DETAILS = '/getInactiveVotingDetails';
  private readonly ALL_VOTING_TITLES = '/getVotingTitles';
  private readonly DISCUSSION = '/getDiscussion';
  private readonly COMMENT = '/comment';
  private readonly REACT = '/react';
  private readonly VOTE = '/vote';
  private readonly FEEDBACK = '/feedback';
  private readonly ALL_FEEDBACK = '/getFeedback';
  private readonly IS_INACTIVE = '/isInactive';
  private readonly HAS_ACCESS = '/hasAccessToVoting';
  private readonly REQUEST_ACCESS = '/requestAccessToVoting';
  private readonly NEW_VOTING = '/initializeVoting';
  private readonly PROCESS_REQUEST = '/processRequest';
  private readonly REQUESTS = '/getRequests';
  private readonly EDIT = '/editVoting';

  constructor(private readonly http: HttpClient) {
  }

  createNewVoting(name: string, topic: string, committee: string[]): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.VOTINGS + this.NEW_VOTING, {name, topic, committee}, httpOptions);
  }

  processRequest(requestId: number, approve: boolean): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.VOTINGS + this.PROCESS_REQUEST, {requestId, approve}, httpOptions);
  }

  getParticipationRequests(id: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const params = new HttpParams().set('voting', id);
    return this.http.get(this.API_REL_PATH + this.VOTINGS + this.REQUESTS, {params, headers});
  }

  hasAccessToVoting(id: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const params = new HttpParams().set('voting', id);
    return this.http.get(this.API_REL_PATH + this.VOTINGS + this.HAS_ACCESS, {params, headers});
  }

  isVotingInactive(id: number): Observable<any> {
    const params = new HttpParams().set('voting', id);
    return this.http.get(this.API_REL_PATH + this.VOTINGS + this.IS_INACTIVE, {params});
  }

  requestAccessToVoting(votingId: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.VOTINGS + this.REQUEST_ACCESS, {votingId}, httpOptions);
  }

  getSuggestedVotings(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    return this.http.get(this.API_REL_PATH + this.VOTINGS + this.SUGGESTED_VOTINGS, {headers});
  }

  getVotings(selectedRole: string): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const params = new HttpParams().set('selectedRole', selectedRole);
    return this.http.get(this.API_REL_PATH + this.VOTINGS + this.ALL_VOTINGS, {params, headers});
  }

  getInactiveVotings(): Observable<any> {
    return this.http.get(this.API_REL_PATH + this.VOTINGS + this.INACTIVE_VOTINGS);
  }

  getVotingDetails(id: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const params = new HttpParams().set('voting', id);
    return this.http.get(this.API_REL_PATH + this.VOTINGS + this.VOTING_DETAILS, {params, headers});
  }

  getInactiveVotingDetails(id: number): Observable<any> {
    const params = new HttpParams().set('voting', id);
    return this.http.get(this.API_REL_PATH + this.VOTINGS + this.INACTIVE_VOTING_DETAILS, {params});
  }

  getDiscussion(id: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const params = new HttpParams().set('voting', id);
    return this.http.get(this.API_REL_PATH + this.VOTINGS + this.DISCUSSION, {params, headers});
  }

  getFeedback(id: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const params = new HttpParams().set('voting', id);
    return this.http.get(this.API_REL_PATH + this.VOTINGS + this.ALL_FEEDBACK, {params, headers});
  }

  addComment(votingId: number, message: string): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.VOTINGS + this.COMMENT, {votingId, message}, httpOptions);
  }

  submitFeedback(votingId: number, message: string): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.VOTINGS + this.FEEDBACK, {votingId, message}, httpOptions);
  }

  react(messageId: number, action: boolean): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.VOTINGS + this.REACT, {messageId, action}, httpOptions);
  }

  castVote(votes: string[], votingId: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.VOTINGS + this.VOTE, {votes, votingId}, httpOptions);
  }

  editVoting(id: number, startDateValue: Date | null, endDateValue: Date | null, description: string | null, mechanism: string | null,
             options: VotingOption[], voteLimit: number | null): Observable<any> {
    const startDate = startDateValue ? formatDate(startDateValue, 'yyyy-MM-dd', 'en-US') : null;
    const endDate = endDateValue ? formatDate(endDateValue, 'yyyy-MM-dd', 'en-US') : null;

    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.VOTINGS + this.EDIT, {
      id,
      startDate,
      endDate,
      description,
      mechanism,
      options,
      voteLimit
    }, httpOptions);
  }

  getAllVotings(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    return this.http.get(this.API_REL_PATH + this.VOTINGS + this.ALL_VOTING_TITLES, {headers});
  }
}
