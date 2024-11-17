import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Injectable} from "@angular/core";

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly apiUrl = '../authenticate';

    constructor(private readonly http: HttpClient) {
    }

    login(username: string, password: string): Observable<any> {
        return this.http.post(this.apiUrl, {username, password});
    }

    isAuthenticated(): boolean {
        console.log('Local storage token: ', localStorage.getItem('token'))
        return !!localStorage.getItem('token');
    }
}
