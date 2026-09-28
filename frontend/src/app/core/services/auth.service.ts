import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { BehaviorSubject } from 'rxjs';

export interface CurrentUser {
  id?: number;
  email: string;
  username?: string;
  name?: string;
  lastname?: string;
  role: string;
  permissions: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private isLoggedInStatus = false;
  private currentUser: CurrentUser | null = null;
  private apiUrl = 'http://localhost:8081/auth';
  private authChangedSubject = new BehaviorSubject<boolean>(this.isLoggedIn());
  public authChanged$ = this.authChangedSubject.asObservable();

  constructor(
    private router: Router,
    private http: HttpClient
  ) {
    const saved = localStorage.getItem('currentUser');
    if (saved) {
      this.currentUser = JSON.parse(saved);
      this.isLoggedInStatus = true;
      this.authChangedSubject.next(true);
    }
  }


  register(email: string, password: string, name: string, lastname: string): Observable<any> {
    const registerRequest = { email, password, name, lastname };

    return this.http.post(`${this.apiUrl}/register`, registerRequest)
      .pipe(
        tap((response: any) => {
          console.log('Registracija uspešna!');
        })
      );
  }

  login(email: string, password: string): Observable<any> {
    const loginRequest = { email, password };

    return this.http.post<any>(`${this.apiUrl}/login`, loginRequest)
      .pipe(
        tap((response: any) => {
          console.log('Login uspešan sa backend-om!');
          this.handleLoginSuccess(response);
        })
      );

  }

  private handleLoginSuccess(response: any): void {
    const user = response.user || response;

    if (!response.token) {
      console.error('Backend nije vratio token!');
      alert('Greška pri login-u - token nije vraćen!');
      return;
    }

    this.currentUser = {
      id: user.id,
      email: user.email,
      username: user.username || user.email.split('@')[0],
      name: user.name,
      lastname: user.lastname,
      role: user.role || 'User',
      permissions: user.permissions || []
    };

    this.isLoggedInStatus = true;

    localStorage.setItem('token', response.token);
    localStorage.setItem('currentUser', JSON.stringify(this.currentUser));
    localStorage.setItem('loggedIn', 'true');

    console.log('Korisnik sačuvan:', this.currentUser);
    console.log(' Token:', response.token);
    console.log(' Permisije:', this.currentUser.permissions);

    this.authChangedSubject.next(true);
  }

  logout(): void {
    this.isLoggedInStatus = false;
    this.currentUser = null;
    localStorage.removeItem('loggedIn');
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');

    this.authChangedSubject.next(false);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return this.isLoggedInStatus || localStorage.getItem('loggedIn') === 'true';
  }

  getCurrentUser(): CurrentUser | null {
    if (!this.currentUser) {
      const saved = localStorage.getItem('currentUser');
      if (saved) {
        this.currentUser = JSON.parse(saved);
      }
    }
    return this.currentUser;
  }

  hasPermission(permission: string): boolean {
    const user = this.getCurrentUser();
    if (!user) return false;
    return user.permissions.includes(permission);
  }

  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return !!(user && user.role.toLowerCase() === role.toLowerCase());
  }

  getUserPermissions(): string[] {
    const user = this.getCurrentUser();
    return user ? user.permissions : [];
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }
}
