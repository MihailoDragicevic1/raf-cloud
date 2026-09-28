import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import {Router} from "@angular/router";

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.scss']
})
export class RegistrationComponent implements OnInit, OnDestroy {
  registrationForm: FormGroup;
  isSubmitting: boolean = false;
  errorMessage: string = '';
  email: string = '';
  password: string = '';
  name: string = '';
  lastname: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registrationForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      passwordConfirm: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {}

  ngOnDestroy(): void {}

  onRegister(): void {
    if (!this.email || !this.password || !this.name || !this.lastname) {
      alert('Popunite sva polja!');
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    this.authService.register(this.email, this.password, this.name, this.lastname)
      .subscribe(
        (response: any) => {
          console.log('Registracija uspešna!');

          this.authService.login(this.email, this.password).subscribe(
            (loginResponse: any) => {
              console.log(' Automatski login uspešan!');
              this.isSubmitting = false;

              const user = this.authService.getCurrentUser();
              if (user?.role === 'Administrator') {
                this.router.navigate(['/users']);
              } else {
                this.router.navigate(['/machines']);
              }
            },
            (loginError: any) => {
              console.error(' Automatski login neuspešan:', loginError);
              this.isSubmitting = false;
              alert('Registracija uspešna! Molimo prijavite se.');
              this.router.navigate(['/login']);
            }
          );
        },
        (error: any) => {
          console.error(' Greška pri registraciji:', error);
          this.isSubmitting = false;
          this.errorMessage = error.error?.message || error.message || 'Greška pri registraciji';
          alert('Greška pri registraciji: ' + this.errorMessage);
        }
      );
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
