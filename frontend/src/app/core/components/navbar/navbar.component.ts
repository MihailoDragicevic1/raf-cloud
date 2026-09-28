import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  isLoggedIn = false;
  userRole = '';
  userName = '';

  constructor(public authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.authService.authChanged$.subscribe(isLoggedIn => {
      this.isLoggedIn = isLoggedIn;

      if (isLoggedIn) {
        const user = this.authService.getCurrentUser();
        this.userRole = user?.role || '';
        this.userName = user?.name || user?.username || user?.email || '';
      } else {
        this.userRole = '';
        this.userName = '';
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
