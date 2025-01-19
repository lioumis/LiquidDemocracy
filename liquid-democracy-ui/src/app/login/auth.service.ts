import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Injectable} from "@angular/core";
import {Router} from "@angular/router";

@Injectable({
  providedIn: 'root'
})
export class AuthService { //TODO: Split to business specific services
  private readonly API_REL_PATH = '..';

  private readonly AUTHENTICATION = '/authenticate';
  private readonly REGISTRATION = '/register';
  private readonly RESET = '/resetPassword';
  private readonly SECURITY_QUESTION = '/getSecurityQuestion';
  private readonly USER_DETAILS = '/getUserDetails';
  private readonly CHANGE_PASSWORD = '/changePassword';

  private readonly VOTINGS = '/votings';
  private readonly SUGGESTED_VOTINGS = '/getSuggestedVotings';
  private readonly ALL_VOTINGS = '/getVotings';
  private readonly VOTING_DETAILS = '/getVotingDetails';
  private readonly DISCUSSION = '/getDiscussion';
  private readonly COMMENT = '/comment';
  private readonly REACT = '/react';
  private readonly VOTE = '/vote';
  private readonly FEEDBACK = '/feedback';

  private readonly TOPICS = '/topics';
  private readonly ALL_TOPICS = '/getTopics';

  private readonly DELEGATIONS = '/delegations';
  private readonly MY_DELEGATIONS = '/getDelegations';
  private readonly RECEIVED_DELEGATIONS = '/getReceivedDelegations';
  private readonly NEW_DELEGATION = '/delegate';

  constructor(private readonly http: HttpClient, private readonly router: Router) {
  }

  login(username: string, password: string): Observable<any> {
    return this.http.post(this.API_REL_PATH + this.AUTHENTICATION, {username, password});
  }

  register(username: string, email: string, name: string, surname: string, password: string, securityQuestion: string,
           securityAnswer: string): Observable<any> {
    return this.http.post(this.API_REL_PATH + this.REGISTRATION, {
      username,
      email,
      name,
      surname,
      password,
      securityQuestion,
      securityAnswer
    });
  }

  changePassword(oldPassword: string, newPassword: string): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.CHANGE_PASSWORD, {oldPassword, newPassword}, httpOptions);
  }

  createDelegation(delegateName: string, delegateSurname: string, topicId: number): Observable<any> {
    const delegator = localStorage.getItem('username');
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.DELEGATIONS + this.NEW_DELEGATION, {
      delegator,
      delegateName,
      delegateSurname,
      topicId
    }, httpOptions);
  }

  reset(username: string, email: string, newPassword: string, securityAnswer: string): Observable<any> {
    return this.http.post(this.API_REL_PATH + this.RESET, {username, email, newPassword, securityAnswer});
  }

  getSecurityQuestion(username: string, email: string): Observable<any> {
    const params = new HttpParams().set('username', username).set('email', email);
    return this.http.get(this.API_REL_PATH + this.SECURITY_QUESTION, {params});
  }

  getUserDetails(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    return this.http.get(this.API_REL_PATH + this.USER_DETAILS, {headers});
  }

  getSuggestedVotings(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    return this.http.get(this.API_REL_PATH + this.VOTINGS + this.SUGGESTED_VOTINGS, {headers});
  }

  getVotings(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    return this.http.get(this.API_REL_PATH + this.VOTINGS + this.ALL_VOTINGS, {headers});
  }

  getVotingDetails(id: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const params = new HttpParams().set('voting', id);
    return this.http.get(this.API_REL_PATH + this.VOTINGS + this.VOTING_DETAILS, {params, headers});
  }

  getDiscussion(id: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const params = new HttpParams().set('voting', id);
    return this.http.get(this.API_REL_PATH + this.VOTINGS + this.DISCUSSION, {params, headers});
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

  getTopics(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    return this.http.get(this.API_REL_PATH + this.TOPICS + this.ALL_TOPICS, {headers});
  }

  getDelegations(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    return this.http.get(this.API_REL_PATH + this.DELEGATIONS + this.MY_DELEGATIONS, {headers});
  }

  getReceivedDelegations(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    return this.http.get(this.API_REL_PATH + this.DELEGATIONS + this.RECEIVED_DELEGATIONS, {headers});
  }

  isAuthenticated(): boolean {
    let token = localStorage.getItem('token');

    if (!token) {
      return false;
    }
    return !this.isTokenExpired(token);
  }

  isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      return payload.exp < currentTime;
    } catch (error) {
      console.error('Error decoding token:', error);
      return true;
    }
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    localStorage.removeItem('name');
    localStorage.removeItem('roles');
    localStorage.removeItem('surname');
    localStorage.removeItem('username');
    this.router.navigate(['/login']);
  }
}
