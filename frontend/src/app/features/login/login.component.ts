import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  email: string = '';
  password: string = '';
  isSubmitting: boolean = false;
  loginError: string = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onLogin(): void {
    if (!this.email || !this.password) {
      this.loginError = 'Unesite email i lozinku!';
      return;
    }

    this.isSubmitting = true;
    this.loginError = '';

    this.authService.login(this.email, this.password).subscribe(
      (response: any) => {
        console.log('Login odgovora sa:', response);
        this.isSubmitting = false;

        const user = this.authService.getCurrentUser();
        if (user?.role === 'Admin') {
          this.router.navigate(['/users']);
        } else {
          this.router.navigate(['/machines']);
        }
      },
      (error: any) => {
        console.error('Login greška:', error);
        this.isSubmitting = false;
        this.loginError = error.error?.message || 'Pogrešan email ili lozinka!';
      }
    );
  }
}
