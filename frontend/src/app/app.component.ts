import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { WebSocketService } from './core/services/websocket.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'raf-cloud-app';

  constructor(
    public authService: AuthService,
    private router: Router,
    private webSocketService: WebSocketService
  ) {}

  ngOnInit() {
    this.webSocketService.connect()
      .then(() => {
        console.log('Aplikacija je uspešno povezana na WebSocket!');
      })
      .catch(error => {
        console.error('Greška pri konekciji na WebSocket:', error);
      });
  }

  ngOnDestroy() {
    this.webSocketService.disconnect();
    console.log('WebSocket konekcija zatvorena');
  }

  logout() {
    this.authService.logout();
    this.webSocketService.disconnect();
    this.router.navigate(['/login']);
  }
}
