import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {HttpClient, HttpHeaders} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class DelegationsService {
  private readonly API_REL_PATH = '..';

  private readonly DELEGATIONS = '/delegations';
  private readonly MY_DELEGATIONS = '/getDelegations';
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

  getReceivedDelegations(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    return this.http.get(this.API_REL_PATH + this.DELEGATIONS + this.RECEIVED_DELEGATIONS, {headers});
  }
}
