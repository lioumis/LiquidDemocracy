import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class DelegationsService {
  private readonly API_REL_PATH = '..';

  private readonly DELEGATIONS = '/delegations';
  private readonly MY_DELEGATIONS = '/getDelegations';
  private readonly DELEGATES = '/getDelegates';
  private readonly POTENTIAL_DELEGATES = '/getPotentialDelegates';
  private readonly ADD_DELEGATE = '/addDelegate';
  private readonly REMOVE_DELEGATE = '/removeDelegate';
  private readonly RECEIVED_DELEGATIONS = '/getReceivedDelegations';
  private readonly NEW_DELEGATION = '/delegate';

  constructor(private readonly http: HttpClient) {
  }

  createDelegation(delegateName: string, delegateSurname: string, votingId: number): Observable<any> {
    const delegator = localStorage.getItem('username');
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.DELEGATIONS + this.NEW_DELEGATION, {
      delegator,
      delegateName,
      delegateSurname,
      votingId
    }, httpOptions);
  }

  getDelegations(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    return this.http.get(this.API_REL_PATH + this.DELEGATIONS + this.MY_DELEGATIONS, {headers});
  }

  getDelegates(id: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const params = new HttpParams().set('voting', id);
    return this.http.get(this.API_REL_PATH + this.DELEGATIONS + this.DELEGATES, {headers, params});
  }

  getPotentialDelegates(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    return this.http.get(this.API_REL_PATH + this.DELEGATIONS + this.POTENTIAL_DELEGATES, {headers});
  }

  addDelegate(delegate: string, votingId: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.DELEGATIONS + this.ADD_DELEGATE, {delegate, votingId}, httpOptions);
  }

  removeDelegate(delegate: string, votingId: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.DELEGATIONS + this.REMOVE_DELEGATE, {delegate, votingId}, httpOptions);
  }

  getReceivedDelegations(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    return this.http.get(this.API_REL_PATH + this.DELEGATIONS + this.RECEIVED_DELEGATIONS, {headers});
  }
}
