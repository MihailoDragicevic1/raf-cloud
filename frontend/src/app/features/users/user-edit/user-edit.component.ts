import { Component, Inject, OnInit, OnDestroy } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { User } from '../../../core/services/user.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-user-edit',
  templateUrl: './user-edit.component.html',
  styleUrls: ['./user-edit.component.scss']
})
export class UserEditComponent implements OnInit, OnDestroy {
  user: User;
  isSubmitting: boolean = false;

  private userActionSubscription?: Subscription;

  constructor(
    private dialogRef: MatDialogRef<UserEditComponent>,
    @Inject(MAT_DIALOG_DATA) public data: User,
    private webSocketService: WebSocketService
  ) {
    this.user = { ...data };
  }

  ngOnInit(): void {
    this.userActionSubscription = this.webSocketService.userAction.subscribe(
      (actionData: any) => {
        const actionType = actionData.actionType || actionData.type;
        if (actionType === 'UPDATE' || actionType === 'USER_UPDATED') {
          console.log('WebSocket: Korisnik je ažuriran -', actionData);
          this.closeDialog();
        }
      }
    );
  }

  ngOnDestroy(): void {
    if (this.userActionSubscription) {
      this.userActionSubscription.unsubscribe();
    }
    console.log(' User-edit komponenta uništena - WebSocket subscription izbrisana');
  }

  save(): void {
    if (this.user.name && this.user.lastname && this.user.email && this.user.role) {
      this.isSubmitting = true;
      console.log('Slanje zahteva za ažuriranje korisnika:', this.user.id);

      const timeout = setTimeout(() => {
        console.warn('WebSocket event nije stigao - fallback zatvaranje');
        this.isSubmitting = false;
        this.closeDialog();
      }, 10000);

      const subscription = this.webSocketService.userAction.subscribe(
        (actionData: any) => {
          const actionType = actionData.actionType || actionData.type;
          if (actionType === 'UPDATE' || actionType === 'USER_UPDATED') {
            clearTimeout(timeout);
            subscription.unsubscribe();
            this.isSubmitting = false;
            console.log('Korisnik uspešno ažuriran preko WebSocket-a');
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


