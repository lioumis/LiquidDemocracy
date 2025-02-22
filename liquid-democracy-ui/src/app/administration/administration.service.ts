import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {HttpClient, HttpHeaders} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class AdministrationService {
  private readonly API_REL_PATH = '..';

  private readonly TOPICS = '/topics';
  private readonly ALL_TOPICS = '/getTopics';
  private readonly CREATE_TOPIC = '/createTopic';

  private readonly ADD_ROLE = '/addRole';
  private readonly REVOKE_ROLE = '/revokeRole';

  constructor(private readonly http: HttpClient) {
  }

  createTopic(name: string): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.TOPICS + this.CREATE_TOPIC, {name}, httpOptions);
  }

  getTopics(): Observable<any> {
    return this.http.get(this.API_REL_PATH + this.TOPICS + this.ALL_TOPICS);
  }

  addRole(role: string, userId: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.ADD_ROLE, {role, userId}, httpOptions);
  }

  revokeRole(role: string, userId: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
    const httpOptions = {headers}
    return this.http.post(this.API_REL_PATH + this.REVOKE_ROLE, {role, userId}, httpOptions);
  }
}
