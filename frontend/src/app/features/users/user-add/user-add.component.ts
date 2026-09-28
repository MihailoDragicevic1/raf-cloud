import { Component, OnInit, OnDestroy } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { WebSocketService } from '../../../core/services/websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-user-add',
  templateUrl: './user-add.component.html',
  styleUrls: ['./user-add.component.scss']
})
export class UserAddComponent implements OnInit, OnDestroy {
  user = {
    name: '',
    lastname: '',
    email: '',
    role: 'User'
  };

  isSubmitting: boolean = false;

  private userActionSubscription?: Subscription;

  constructor(
    private dialogRef: MatDialogRef<UserAddComponent>,
    private webSocketService: WebSocketService
  ) {}

  ngOnInit(): void {
    this.userActionSubscription = this.webSocketService.userAction.subscribe(
      (actionData: any) => {
        if (!actionData) {
          return;
        }

        const actionType = actionData.actionType || actionData.type;
        if (actionType === 'CREATE' || actionType === 'USER_CREATED') {
          console.log('WebSocket: Novi korisnik je kreiran -', actionData);
          this.closeDialog();
        }
      }
    );
  }

  ngOnDestroy(): void {
    if (this.userActionSubscription) {
      this.userActionSubscription.unsubscribe();
    }
    console.log(' User-add komponenta uništena - WebSocket subscription izbrisana');
  }

  save(): void {
    if (this.user.name && this.user.email && this.user.role) {
      this.isSubmitting = true;
      console.log('Slanje zahteva za kreiranje korisnika:', this.user.email);

      const timeout = setTimeout(() => {
        console.warn(' WebSocket event nije stigao - fallback zatvaranje');
        this.isSubmitting = false;
        this.closeDialog();
      }, 10000);

      const subscription = this.webSocketService.userAction.subscribe(
        (actionData: any) => {
          if (!actionData) {
            return;
          }

          const actionType = actionData.actionType || actionData.type;
          if (actionType === 'CREATE' || actionType === 'USER_CREATED') {
            clearTimeout(timeout);
            subscription.unsubscribe();
            this.isSubmitting = false;
            console.log('Korisnik uspešno kreiran preko WebSocket-a');
            this.closeDialog();
          }
        }
      );

      this.dialogRef.close(this.user);
    }
  }

  cancel(): void {
    this.dialogRef.close();
  }

  private closeDialog(): void {
    this.dialogRef.close(this.user);
  }
}
