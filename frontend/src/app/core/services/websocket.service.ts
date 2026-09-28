import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {

  private ws: WebSocket | null = null;
  private url = 'ws://localhost:8081/ws/events';

  private machineStatusChangedSubject = new BehaviorSubject<any>(null);
  private machineChangedSubject = new BehaviorSubject<any>(null);
  private userActionSubject = new BehaviorSubject<any>(null);
  private errorOccurredSubject = new BehaviorSubject<any>(null);
  private warningOccurredSubject = new BehaviorSubject<any>(null);
  private systemEventSubject = new BehaviorSubject<any>(null);
  private connectionStatusSubject = new BehaviorSubject<boolean>(false);

  public machineStatusChanged = this.machineStatusChangedSubject.asObservable();
  public machineChanged = this.machineChangedSubject.asObservable();
  public userAction = this.userActionSubject.asObservable();
  public errorOccurred = this.errorOccurredSubject.asObservable();
  public warningOccurred = this.warningOccurredSubject.asObservable();
  public systemEvent = this.systemEventSubject.asObservable();
  public connectionStatus = this.connectionStatusSubject.asObservable();

  constructor() { }

  public connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        console.log('Povezujem se na WebSocket:', this.url);

        this.ws = new WebSocket(this.url);

        this.ws.onopen = () => {
          console.log('WebSocket povezan!');
          this.connectionStatusSubject.next(true);
          resolve();
        };

        this.ws.onmessage = (event) => {
          console.log('WebSocket poruka primljena:', event.data);
          this.handleMessage(event.data);
        };

        this.ws.onerror = (error) => {
          console.error('WebSocket greška:', error);
          this.connectionStatusSubject.next(false);
          reject(error);
        };

        this.ws.onclose = () => {
          console.log('WebSocket zatvoren - pokušaj reconect-a za 10s...');
          this.connectionStatusSubject.next(false);

          setTimeout(() => {
            this.connect().catch(err => {
              console.error('Reconect pokušaj neuspešan:', err);
            });
          }, 10000);
        };
      } catch (error) {
        console.error('Greška pri kreiranju WebSocket-a:', error);
        reject(error);
      }
    });
  }

  private handleMessage(message: string): void {
    try {
      const data = JSON.parse(message);
      console.log(`Event: ${data.type}`, data);

      switch (data.type) {
        case 'MACHINE_STATUS_CHANGED':
          console.log('Mašina promenila status');
          this.machineStatusChangedSubject.next(data.data);
          break;

        case 'MACHINE_CHANGED':
          console.log('Mašina promenjena');
          this.machineChangedSubject.next(data.data);
          break;

        case 'USER_ACTION':
          console.log('User akcija');
          this.userActionSubject.next(data.data);
          break;

        case 'ERROR_OCCURRED':
          console.error('Greška se desila');
          this.errorOccurredSubject.next(data.data);
          break;

        case 'WARNING_OCCURRED':
          console.warn('Upozorenje');
          this.warningOccurredSubject.next(data.data);
          break;

        case 'SYSTEM_EVENT':
          console.log('Sistemski događaj');
          this.systemEventSubject.next(data.data);
          break;

        case 'AUTH_FAILED':
          console.error('Autentifikacija neuspešna');
          this.errorOccurredSubject.next(data.data);
          break;

        case 'UNAUTHORIZED_ACCESS':
          console.error('Neovlašćen pristup');
          this.errorOccurredSubject.next(data.data);
          break;

        default:
          console.log('ℹNepoznat tip događaja:', data.type);
      }
    } catch (error) {
      console.error('Greška pri parsiranju WebSocket poruke:', error);
    }
  }

  public send(message: any): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
      console.log('Poruka poslana:', message);
    } else {
      console.warn('WebSocket nije povezan ili nije spreman');
    }
  }

  public disconnect(): void {
    if (this.ws) {
      this.ws.close();
      this.connectionStatusSubject.next(false);
      console.log(' WebSocket zatvoren');
    }
  }

  public isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN;
  }


  public getConnectionStatus(): Observable<boolean> {
    return this.connectionStatus;
  }
}
