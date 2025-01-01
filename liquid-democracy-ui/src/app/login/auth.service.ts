import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Injectable} from "@angular/core";
import {Router} from "@angular/router";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // private static readonly API_REL_PATH = '../authenticate';
  private readonly API_REL_PATH = 'http://localhost:8080'; //TODO: For testing

  private readonly AUTHENTICATION = '/authenticate';
  private readonly REGISTRATION = '/register';
  private readonly RESET = '/resetPassword';
  private readonly SECURITY_QUESTION = '/getSecurityQuestion';
  private readonly USER_DETAILS = '/getUserDetails';

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

  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('identifier');
    localStorage.removeItem('email');
    localStorage.removeItem('name');
    localStorage.removeItem('roles');
    localStorage.removeItem('surname');
    localStorage.removeItem('username');
    this.router.navigate(['/login']);
  }
}
