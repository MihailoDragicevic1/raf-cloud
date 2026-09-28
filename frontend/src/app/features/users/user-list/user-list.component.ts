import { Component, OnInit, OnDestroy } from '@angular/core';
import { User, UserService } from '../../../core/services/user.service';
import { MatDialog } from '@angular/material/dialog';
import { UserAddComponent } from '../user-add/user-add.component';
import { UserEditComponent } from '../user-edit/user-edit.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit, OnDestroy {

  displayedColumns: string[] = ['id', 'name', 'lastname', 'email', 'role', 'actions'];
  users: User[] = [];

  private userActionSubscription?: Subscription;
  wsConnectionStatus: boolean = false;

  constructor(
    private userService: UserService,
    private dialog: MatDialog,
    public authService: AuthService,
    private webSocketService: WebSocketService
  ) {}

  ngOnInit(): void {
    this.loadUsers();

    this.subscribeToWebSocketEvents();

    this.webSocketService.getConnectionStatus().subscribe(status => {
      this.wsConnectionStatus = status;
      console.log('WebSocket status:', status ? 'Povezan' : 'Odvojen');
    });

    setInterval(() => {
      this.loadUsers();
    }, 5000);
  }

  ngOnDestroy(): void {
    if (this.userActionSubscription) {
      this.userActionSubscription.unsubscribe();
    }
    console.log('User-list komponenta uništena - WebSocket subscription izbrisana');
  }

  private subscribeToWebSocketEvents(): void {
    this.userActionSubscription = this.webSocketService.userAction.subscribe(
      (data: any) => {
        if (data) {
          console.log('WebSocket: User akcija -', data);
          this.handleUserAction(data);
        }
      }
    );
  }

  private handleUserAction(actionData: any): void {
    const actionType = actionData.actionType || actionData.type;

    switch (actionType) {
      case 'CREATE':
      case 'USER_CREATED':
        console.log('Novi korisnik dodat');
        this.loadUsers();
        break;

      case 'UPDATE':
      case 'USER_UPDATED':
        console.log('Korisnik ažuriran');
        this.updateUserInList(actionData.user || actionData.data);
        break;

      case 'DELETE':
      case 'USER_DELETED':
        console.log('Korisnik obrisan');
        this.removeUserFromList(actionData.userId || actionData.id);
        break;

      case 'LOGIN':
      case 'USER_LOGGED_IN':
        console.log('Korisnik se prijavio');
        this.loadUsers();
        break;

      case 'LOGOUT':
      case 'USER_LOGGED_OUT':
        console.log('Korisnik se odprijavio');
        this.loadUsers();
        break;

      default:
        console.log('ℹNepoznata user akcija:', actionType);
    }
  }

  private updateUserInList(updatedUser: any): void {
    const index = this.users.findIndex(u => u.id === updatedUser.id);
    if (index !== -1) {
      this.users[index] = { ...this.users[index], ...updatedUser };
      console.log('Korisnik ažuriran u listi:', updatedUser.id);
    }
  }

  private removeUserFromList(userId: number): void {
    const index = this.users.findIndex(u => u.id === userId);
    if (index !== -1) {
      this.users.splice(index, 1);
      console.log('Korisnik uklonjen iz liste:', userId);
    }
  }

  loadUsers(): void {
    this.userService.getUsersObservable().subscribe(
      (users) => {
        this.users = users;
        console.log('Korisnici učitani u komponentu:', users);
      },
      (error) => {
        console.error('Greška pri učitavanju korisnika:', error);
      }
    );
  }

  deleteUser(id: number): void {
    if (!this.authService.hasPermission('can_delete_users')) {
      alert('Nemate dozvolu za brisanje korisnika.');
      return;
    }

    if (confirm('Da li ste sigurni da želite da obrišete ovog korisnika?')) {
      this.userService.deleteUser(id);

      setTimeout(() => {
        this.loadUsers();
      }, 5000);
    }
  }

  openAddUserDialog(): void {
    if (!this.authService.hasPermission('can_create_users')) {
      alert('Nemate dozvolu za dodavanje korisnika.');
      return;
    }
    const dialogRef = this.dialog.open(UserAddComponent, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.userService.addUser(result);

        setTimeout(() => {
          this.loadUsers();
        }, 5000);
      }
    });
  }

  openEditUserDialog(user: User): void {
    if (!this.authService.hasPermission('can_update_users')) {
      alert('Nemate dozvolu za ažuriranje korisnika.');
      return;
    }
    const dialogRef = this.dialog.open(UserEditComponent, {
      width: '400px',
      data: user
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.userService.updateUser(result);

        setTimeout(() => {
          this.loadUsers();
        }, 5000);
      }
    });
  }
}
