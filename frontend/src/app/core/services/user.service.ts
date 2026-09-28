import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { AuthService } from './auth.service';

export interface User {
  id: number;
  name: string;
  lastname: string;
  email: string;
  role: string;
  permissions?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private apiUrl = 'http://localhost:8081/users';
  private users: User[] = [];

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {
    this.loadUsersFromBackend();
  }

  private loadUsersFromBackend(): void {
    const token = this.authService.getToken();
    if (!token) return;

    const headers = { 'Authorization': `Bearer ${token}` };

    this.http.get<User[]>(this.apiUrl, { headers }).subscribe(
      (users) => {
        this.users = users;
        console.log('Korisnici učitani sa backend-a:', users);
      },
      (error) => {
        console.error('Greška pri učitavanju korisnika:', error);
      }
    );
  }

  getUsersObservable(): Observable<User[]> {
    const token = this.authService.getToken();
    const headers = { 'Authorization': `Bearer ${token}` };

    return this.http.get<User[]>(this.apiUrl, { headers }).pipe(
      tap(users => {
        this.users = users;
        console.log('Korisnici učitani kroz Observable:', users);
      })
    );
  }

  getAllUsers(): User[] {
    return this.users;
  }

  refreshUsers(): void {
    this.loadUsersFromBackend();
  }

  addUser(user: User): void {
    const token = this.authService.getToken();
    const headers = { 'Authorization': `Bearer ${token}` };

    const userDTO = {
      email: user.email,
      name: user.name,
      lastname: user.lastname,
      password: user.name || 'defaultPassword123',
      permissions: user.permissions || [],
      role: user.role || 'User'
    };

    this.http.post<User>(this.apiUrl, userDTO, { headers }).subscribe(
      (createdUser) => {
        console.log(' Korisnik kreiran:', createdUser);
        this.users.push(createdUser);
        alert(`Korisnik "${createdUser.name}" je uspešno dodat!`);
        this.refreshUsers();
      },
      (error) => {
        console.error(' Greška pri kreiranju korisnika:', error);
        alert('Greška pri kreiranju korisnika: ' + (error.error?.message || error.message));
      }
    );
  }

  updateUser(updatedUser: User): void {
    const token = this.authService.getToken();
    const headers = { 'Authorization': `Bearer ${token}` };

    const userDTO = {
      email: updatedUser.email,
      name: updatedUser.name,
      lastname: updatedUser.lastname,
      password: updatedUser.name || 'defaultPassword123',
      permissions: updatedUser.permissions || [],
      role: updatedUser.role || 'User'
    };

    this.http.put<User>(`${this.apiUrl}/${updatedUser.id}`, userDTO, { headers }).subscribe(
      (user) => {
        console.log(' Korisnik ažuriran:', user);
        const index = this.users.findIndex(u => u.id === updatedUser.id);
        if (index !== -1) {
          this.users[index] = user;
        }
        alert(`Korisnik "${user.name}" je uspešno ažuriran!`);
        this.refreshUsers();
      },
      (error) => {
        console.error(' Greška pri ažuriranju korisnika:', error);
        alert('Greška pri ažuriranju korisnika: ' + (error.error?.message || error.message));
      }
    );
  }

  deleteUser(id: number): void {
    const token = this.authService.getToken();
    const headers = { 'Authorization': `Bearer ${token}` };

    this.http.delete(`${this.apiUrl}/${id}`, { headers }).subscribe(
      () => {
        console.log(' Korisnik obrisan:', id);
        this.users = this.users.filter(u => u.id !== id);
        alert('Korisnik je uspešno obrisan!');
      },
      (error) => {
        console.error(' Greška pri brisanju korisnika:', error);
        alert('Greška pri brisanju korisnika: ' + (error.error?.message || error.message));
      }
    );
  }
}
