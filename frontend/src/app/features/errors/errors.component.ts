import { Component, OnInit, OnDestroy } from '@angular/core';
import { ErrorService } from '../../core/services/errors.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { Subscription } from 'rxjs';

interface ErrorNotification {
  id: string;
  message: string;
  severity: string;
  timestamp: Date;
}

@Component({
  selector: 'app-errors',
  templateUrl: './errors.component.html',
  styleUrls: ['./errors.component.scss']
})
export class ErrorsComponent implements OnInit, OnDestroy {
  errors: any[] = [];
  wsErrors: ErrorNotification[] = [];

  private errorOccurredSubscription?: Subscription;
  private warningOccurredSubscription?: Subscription;

  constructor(
    private errorService: ErrorService,
    private webSocketService: WebSocketService
  ) {}

  ngOnInit(): void {
    this.errors = this.errorService.getErrors();

    this.subscribeToWebSocketErrors();
  }

  ngOnDestroy(): void {
    if (this.errorOccurredSubscription) {
      this.errorOccurredSubscription.unsubscribe();
    }
    if (this.warningOccurredSubscription) {
      this.warningOccurredSubscription.unsubscribe();
    }
    console.log(' Errors komponenta uništena - WebSocket subscription-i izbrisani');
  }

  private subscribeToWebSocketErrors(): void {
    this.errorOccurredSubscription = this.webSocketService.errorOccurred.subscribe(
      (errorData: any) => {
        if (errorData) {
          console.error('WebSocket Greška:', errorData);
          this.addErrorNotification(
            errorData.message || 'Došlo je do greške',
            'error',
            errorData
          );
        }
      }
    );

    this.warningOccurredSubscription = this.webSocketService.warningOccurred.subscribe(
      (warningData: any) => {
        if (warningData) {
          console.warn('WebSocket Upozorenje:', warningData);
          this.addErrorNotification(
            warningData.message || 'Upozorenje',
            'warning',
            warningData
          );
        }
      }
    );
  }

  private addErrorNotification(message: string, severity: string, data: any): void {
    const errorNotification: ErrorNotification = {
      id: `error_${Date.now()}`,
      message: message,
      severity: severity,
      timestamp: new Date()
    };

    this.wsErrors.unshift(errorNotification);
    console.log(`Greška dodata u listu (${severity}):`, message);

    setTimeout(() => {
      this.removeError(errorNotification.id);
    }, 10000);
  }


  removeError(errorId: string): void {
    const index = this.wsErrors.findIndex(e => e.id === errorId);
    if (index !== -1) {
      this.wsErrors.splice(index, 1);
      console.log('Greška uklonjena iz liste:', errorId);
    }
  }

  getAllErrors(): any[] {
    return [...this.errors, ...this.wsErrors];
  }

  getWebSocketErrors(): ErrorNotification[] {
    return this.wsErrors;
  }

  getLocalErrors(): any[] {
    return this.errors;
  }

  hasErrors(): boolean {
    return this.errors.length > 0 || this.wsErrors.length > 0;
  }

  clearAllErrors(): void {
    this.errors = [];
    this.wsErrors = [];
    console.log('Sve greške očišćene');
  }
}
