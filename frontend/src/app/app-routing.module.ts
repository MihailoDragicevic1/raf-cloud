import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './features/login/login.component';

import { UserListComponent } from './features/users/user-list/user-list.component';
import { UserAddComponent } from './features/users/user-add/user-add.component';
import { UserEditComponent } from './features/users/user-edit/user-edit.component';
import { MachineListComponent } from './features/machines/machine-list/machine-list.component';
import { MachineAddComponent } from './features/machines/machine-add/machine-add.component';
import { ErrorsComponent } from './features/errors/errors.component';
import {AuthGuard} from "./core/guards/auth.guard";
import {RegistrationComponent} from "./features/registration/registration.component";

const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegistrationComponent },

  { path: 'users', component: UserListComponent, canActivate: [AuthGuard]},
  { path: 'users/add', component: UserAddComponent, canActivate: [AuthGuard] },
  { path: 'users/edit/:id', component: UserEditComponent, canActivate: [AuthGuard] },
  { path: 'machines', component: MachineListComponent, canActivate: [AuthGuard] },
  { path: 'machines/add', component: MachineAddComponent, canActivate: [AuthGuard] },
  { path: 'errors', component: ErrorsComponent, canActivate: [AuthGuard] },

  { path: '**', redirectTo: 'errors' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
